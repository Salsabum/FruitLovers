package com.example.myapplication.ui

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.CarritoActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.HistorialAdapter
import com.example.myapplication.data.AppDatabaseHelper
import com.example.myapplication.entity.Carrito
import com.example.myapplication.entity.DetalleLista
import com.example.myapplication.entity.ListaCompras
import com.example.myapplication.entity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialFragment : Fragment(R.layout.fragment_historial) {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var historialAdapter: HistorialAdapter
    private val listaCompras = mutableListOf<ListaCompras>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvHistorial = view.findViewById(R.id.rvHistorial)
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        historialAdapter = HistorialAdapter(listaCompras)
        rvHistorial.adapter = historialAdapter

        historialAdapter.setOnItemClickListener { listaSeleccionada ->
            mostrarDetalleLista(listaSeleccionada.id)
        }
        historialAdapter.setOnRepeatClickListener { listaSeleccionada ->
            repetirPedido(listaSeleccionada.id)
        }

        cargarListas()
    }

    private fun cargarListas() {
        lifecycleScope.launch {
            val listas = withContext(Dispatchers.IO) {
                val tempList = mutableListOf<ListaCompras>()
                val dbHelper = AppDatabaseHelper(requireContext())
                val db = dbHelper.readableDatabase
                val cursor: Cursor = db.rawQuery(
                    """
                    SELECT lc.id_lista_compras, lc.fecha, lc.id_usuario,
                           CASE
                               WHEN COALESCE(lc.fecha_entrega, substr(lc.fecha, 1, 10)) = date('now','localtime') THEN 'En camino'
                               WHEN COALESCE(lc.fecha_entrega, substr(lc.fecha, 1, 10)) > date('now','localtime') THEN 'En proceso'
                               ELSE 'Entregado'
                           END AS estado,
                           COALESCE(SUM(dl.precio_pagado), 0) AS subtotal
                    FROM lista_compras lc
                    LEFT JOIN detalle_lista dl
                        ON dl.id_lista_compras = lc.id_lista_compras
                    GROUP BY lc.id_lista_compras, lc.fecha, lc.id_usuario, lc.fecha_entrega
                    ORDER BY lc.fecha DESC
                    """.trimIndent(),
                    null
                )

                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow("id_lista_compras"))
                        val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                        val idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"))
                        val estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                        val subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal"))

                        val fechaFormateada = try {
                            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val date = parser.parse(fecha) ?: throw IllegalArgumentException()
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE")).format(date)
                        } catch (e: Exception) {
                            try {
                                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val date = parser.parse(fecha) ?: throw IllegalArgumentException()
                                SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")).format(date)
                            } catch (_: Exception) {
                                fecha
                            }
                        }

                        tempList.add(
                            ListaCompras(
                                id = id,
                                fecha = fechaFormateada,
                                estado = estado,
                                idUsuario = idUsuario,
                                subtotal = subtotal
                            )
                        )
                    } while (cursor.moveToNext())
                }

                cursor.close()
                db.close()
                tempList
            }

            listaCompras.clear()
            listaCompras.addAll(listas)
            historialAdapter.notifyDataSetChanged()
        }
    }

    private fun mostrarDetalleLista(idLista: Int) {
        lifecycleScope.launch {
            val detalles = obtenerDetallesDeLista(idLista)

            if (detalles.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Sin productos")
                    .setMessage("Esta lista no tiene productos guardados.")
                    .setPositiveButton("Cerrar", null)
                    .show()
            } else {
                val detalleTexto = StringBuilder()
                var total = 0.0
                for (item in detalles) {
                    val pesoTexto = if (item.unidadMedida == "1/2 kg") {
                        val pesoKg = item.cantidad * 0.5
                        val esEntero = pesoKg % 1.0 == 0.0
                        if (esEntero) "${pesoKg.toInt()} kg" else "${String.format("%.1f", pesoKg)} kg"
                    } else {
                        "${item.cantidad} ${item.unidadMedida}"
                    }
                    detalleTexto.append("- $pesoTexto de ${item.producto}\n")
                    detalleTexto.append("P. Unit.: S/ ${item.precioUnitario} | Pagado: S/ ${item.precioPagado}\n\n")
                    total += item.precioPagado
                }
                val mensajeFinal = "$detalleTexto\nTotal pagado: S/ ${String.format("%.2f", total)}"
                AlertDialog.Builder(requireContext())
                    .setTitle("Productos de la lista #${String.format("%03d", idLista)}")
                    .setMessage(mensajeFinal)
                    .setNegativeButton("Cerrar", null)
                    .setPositiveButton("Repetir pedido") { _, _ ->
                        repetirPedido(idLista)
                    }
                    .show()
            }
        }
    }

    private fun repetirPedido(idLista: Int) {
        lifecycleScope.launch {
            val detalles = obtenerDetallesDeLista(idLista)

            if (detalles.isEmpty()) {
                Toast.makeText(requireContext(), "Esta compra no tiene productos para repetir", Toast.LENGTH_SHORT).show()
                return@launch
            }

            detalles.forEach { detalle ->
                val producto = productoDesdeDetalle(detalle)
                repeat(detalle.cantidad) {
                    Carrito.items.add(producto)
                }
            }

            Toast.makeText(requireContext(), "Pedido agregado al carrito", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), CarritoActivity::class.java))
        }
    }

    private suspend fun obtenerDetallesDeLista(idLista: Int): List<DetalleLista> {
        return withContext(Dispatchers.IO) {
            val tempDetalles = mutableListOf<DetalleLista>()
            val dbHelper = AppDatabaseHelper(requireContext())
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM detalle_lista WHERE id_lista_compras = ?",
                arrayOf(idLista.toString())
            )

            if (cursor.moveToFirst()) {
                do {
                    tempDetalles.add(
                        DetalleLista(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id_detalle_lista")),
                            producto = cursor.getString(cursor.getColumnIndexOrThrow("producto")),
                            unidadMedida = cursor.getString(cursor.getColumnIndexOrThrow("unidad_medida")),
                            cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad")),
                            precioUnitario = cursor.getDouble(cursor.getColumnIndexOrThrow("precio_unitario")),
                            precioPagado = cursor.getDouble(cursor.getColumnIndexOrThrow("precio_pagado")),
                            idListaCompras = cursor.getInt(cursor.getColumnIndexOrThrow("id_lista_compras"))
                        )
                    )
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            tempDetalles
        }
    }

    private fun productoDesdeDetalle(detalle: DetalleLista): Product {
        val nombre = detalle.producto.trim()
        val nombreNormalizado = normalizarNombre(nombre)
        val imagen = when (nombreNormalizado) {
            "manzana" -> R.drawable.manzana
            "mandarina" -> R.drawable.mandarina
            "naranja" -> R.drawable.naranja
            "sandia" -> R.drawable.sandia
            "durazno" -> R.drawable.durazno
            "mamey" -> R.drawable.mamey
            "fresa" -> R.drawable.fresa
            "pina" -> R.drawable.pina
            "pitahaya" -> R.drawable.pitahaya
            "platano" -> R.drawable.platano
            else -> R.drawable.manzana
        }

        val id = nombreNormalizado.hashCode().let { if (it == Int.MIN_VALUE) 1 else kotlin.math.abs(it) }
        return Product(
            id = id,
            nombre = nombre,
            precio = detalle.precioUnitario,
            descripcion = "1/2 kg - Repetido",
            imagen = imagen
        )
    }

    private fun normalizarNombre(nombre: String): String {
        return nombre
            .lowercase(Locale.getDefault())
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
    }
}
