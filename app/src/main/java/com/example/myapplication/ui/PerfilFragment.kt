package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.AccesoActivity
import com.example.myapplication.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class PerfilFragment : Fragment(R.layout.fragment_perfil) {
    private lateinit var tietNombresPerfil: TextInputEditText
    private lateinit var tietApellidoPaternoPerfil: TextInputEditText
    private lateinit var tietApellidoMaternoPerfil: TextInputEditText
    private lateinit var tietDniPerfil: TextInputEditText
    private lateinit var tietCelularPerfil: TextInputEditText
    private lateinit var tietCorreoPerfil: TextInputEditText
    private lateinit var btnGuardarPerfil: Button
    private lateinit var btnCerrarSesion: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tietNombresPerfil = view.findViewById(R.id.tietNombresPerfil)
        tietApellidoPaternoPerfil = view.findViewById(R.id.tietApellidoPaternoPerfil)
        tietApellidoMaternoPerfil = view.findViewById(R.id.tietApellidoMaternoPerfil)
        tietDniPerfil = view.findViewById(R.id.tietDniPerfil)
        tietCelularPerfil = view.findViewById(R.id.tietCelularPerfil)
        tietCorreoPerfil = view.findViewById(R.id.tietCorreoPerfil)
        btnGuardarPerfil = view.findViewById(R.id.btnGuardarPerfil)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)

        cargarPerfil()

        btnGuardarPerfil.setOnClickListener {
            guardarCambiosPerfil()
        }

        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), AccesoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun cargarPerfil() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val refUsuario = FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .child(user.uid)

        refUsuario.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    cargarCamposDesdeSnapshot(snapshot.child("nombres").getValue(String::class.java),
                        snapshot.child("apellido_paterno").getValue(String::class.java),
                        snapshot.child("apellido_materno").getValue(String::class.java),
                        snapshot.child("dni").getValue(String::class.java),
                        snapshot.child("celular").getValue(String::class.java),
                        snapshot.child("correo").getValue(String::class.java) ?: user.email)
                } else {
                    buscarPerfilPorCorreo(user.email)
                }
            }
            .addOnFailureListener {
                buscarPerfilPorCorreo(user.email)
            }
    }

    private fun buscarPerfilPorCorreo(correo: String?) {
        if (correo.isNullOrBlank()) {
            cargarCamposDesdeSnapshot("", "", "", "", "", "")
            return
        }

        FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .orderByChild("correo")
            .equalTo(correo)
            .limitToFirst(1)
            .get()
            .addOnSuccessListener { data ->
                val item = data.children.firstOrNull()
                if (item != null) {
                    cargarCamposDesdeSnapshot(
                        item.child("nombres").getValue(String::class.java),
                        item.child("apellido_paterno").getValue(String::class.java),
                        item.child("apellido_materno").getValue(String::class.java),
                        item.child("dni").getValue(String::class.java),
                        item.child("celular").getValue(String::class.java),
                        item.child("correo").getValue(String::class.java) ?: correo
                    )
                } else {
                    cargarCamposDesdeSnapshot("", "", "", "", "", correo)
                    Handler(Looper.getMainLooper()).postDelayed({
                        recargarPerfilSilencioso()
                    }, 1200)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
                cargarCamposDesdeSnapshot("", "", "", "", "", correo)
            }
    }

    private fun cargarCamposDesdeSnapshot(
        nombres: String?,
        apellidoPaterno: String?,
        apellidoMaterno: String?,
        dni: String?,
        celular: String?,
        correo: String?
    ) {
        tietNombresPerfil.setText(nombres ?: "")
        tietApellidoPaternoPerfil.setText(apellidoPaterno ?: "")
        tietApellidoMaternoPerfil.setText(apellidoMaterno ?: "")
        tietDniPerfil.setText(dni ?: "")
        tietCelularPerfil.setText(celular ?: "")
        tietCorreoPerfil.setText(correo ?: "")
    }

    private fun recargarPerfilSilencioso() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .child(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    cargarCamposDesdeSnapshot(
                        snapshot.child("nombres").getValue(String::class.java),
                        snapshot.child("apellido_paterno").getValue(String::class.java),
                        snapshot.child("apellido_materno").getValue(String::class.java),
                        snapshot.child("dni").getValue(String::class.java),
                        snapshot.child("celular").getValue(String::class.java),
                        snapshot.child("correo").getValue(String::class.java) ?: user.email
                    )
                }
            }
    }

    private fun guardarCambiosPerfil() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = tietNombresPerfil.text.toString().trim()
        val apellidoPaterno = tietApellidoPaternoPerfil.text.toString().trim()
        val apellidoMaterno = tietApellidoMaternoPerfil.text.toString().trim()

        if (nombres.isEmpty() || apellidoPaterno.isEmpty() || apellidoMaterno.isEmpty()) {
            Toast.makeText(requireContext(), "Completa nombres y apellidos", Toast.LENGTH_SHORT).show()
            return
        }

        val cambios = mapOf(
            "nombres" to nombres,
            "apellido_paterno" to apellidoPaterno,
            "apellido_materno" to apellidoMaterno
        )

        FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .child(user.uid)
            .updateChildren(cambios)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "No se pudo actualizar el perfil", Toast.LENGTH_SHORT).show()
            }
    }
}
