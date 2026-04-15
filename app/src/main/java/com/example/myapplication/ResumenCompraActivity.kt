package com.example.myapplication

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.AppDatabaseHelper
import com.example.myapplication.entity.Carrito
import com.example.myapplication.entity.Product
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ResumenCompraActivity : AppCompatActivity() {
    private companion object {
        const val DELIVERY_COST = 8.0
    }

    private data class CompraGuardada(
        val numeroOrden: String,
        val detallePedido: String,
        val fechaEntrega: String,
        val totalPedido: String
    )

    private lateinit var tvDetalleCompra: TextView
    private lateinit var tvTotalCompra: TextView
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etCelular: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etReferenciaDireccion: EditText
    private lateinit var etFecha: EditText
    private lateinit var rgRecibe: RadioGroup
    private lateinit var layoutOtraPersona: LinearLayout
    private lateinit var etRecibeNombreCompleto: EditText
    private lateinit var etRecibeDni: EditText
    private lateinit var spMetodoPago: Spinner
    private lateinit var btnProcesarCompra: Button
    private lateinit var metodosPago: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen_compra)

        tvDetalleCompra = findViewById(R.id.tvDetalleCompra)
        tvTotalCompra = findViewById(R.id.tvTotalCompra)
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etCelular = findViewById(R.id.etCelular)
        etDireccion = findViewById(R.id.etDireccion)
        etReferenciaDireccion = findViewById(R.id.etReferenciaDireccion)
        etFecha = findViewById(R.id.etFecha)
        rgRecibe = findViewById(R.id.rgRecibe)
        layoutOtraPersona = findViewById(R.id.layoutOtraPersona)
        etRecibeNombreCompleto = findViewById(R.id.etRecibeNombreCompleto)
        etRecibeDni = findViewById(R.id.etRecibeDni)
        spMetodoPago = findViewById(R.id.spMetodoPago)
        btnProcesarCompra = findViewById(R.id.btnProcesarCompra)

        mostrarResumen()
        prellenarDatosUsuario()
        configurarOpcionesRecepcion()
        configurarMetodoPago()

        etFecha.setOnClickListener { mostrarDatePicker() }
        btnProcesarCompra.setOnClickListener { procesarCompra() }
    }

    private fun mostrarResumen() {
        if (Carrito.items.isEmpty()) {
            tvDetalleCompra.text = "Sin productos en el carrito"
            tvTotalCompra.text = "Total: S/ 0.00"
            return
        }

        val agrupado = agruparCarritoPorProducto()
        val subtotal = subtotalProductos(agrupado)
        tvDetalleCompra.text = "${construirDetallePedido(agrupado)}\n\nDelivery: S/ %.2f".format(DELIVERY_COST)
        tvTotalCompra.text = "Total: S/ %.2f".format(subtotal + DELIVERY_COST)
    }

    private fun prellenarDatosUsuario() {
        val user = FirebaseAuth.getInstance().currentUser
        etNombre.setText(user?.displayName ?: "")
        etCorreo.setText(user?.email ?: "")
    }

    private fun configurarOpcionesRecepcion() {
        rgRecibe.setOnCheckedChangeListener { _, checkedId ->
            val otraPersona = checkedId == R.id.rbRecibeOtraPersona
            layoutOtraPersona.visibility = if (otraPersona) LinearLayout.VISIBLE else LinearLayout.GONE
            if (!otraPersona) {
                etRecibeNombreCompleto.setText("")
                etRecibeDni.setText("")
            }
        }
    }

    private fun configurarMetodoPago() {
        metodosPago = listOf("Selecciona metodo de pago", "Efectivo", "Yape / Plin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, metodosPago)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMetodoPago.adapter = adapter
        spMetodoPago.setSelection(0)
    }

    private fun mostrarDatePicker() {
        val cal = Calendar.getInstance()
        val picker = DatePickerDialog(this, { _, y, m, d ->
            val fecha = Calendar.getInstance()
            fecha.set(y, m, d)
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etFecha.setText(formato.format(fecha.time))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        picker.datePicker.minDate = cal.timeInMillis
        picker.show()
    }

    private fun procesarCompra() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val celular = etCelular.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()
        val referenciaDireccion = etReferenciaDireccion.text.toString().trim()
        val fecha = etFecha.text.toString().trim()
        val recibeOtraPersona = rgRecibe.checkedRadioButtonId == R.id.rbRecibeOtraPersona
        val nombreRecibe = etRecibeNombreCompleto.text.toString().trim()
        val dniRecibe = etRecibeDni.text.toString().trim()
        val metodoSeleccionado = spMetodoPago.selectedItemPosition

        if (Carrito.items.isEmpty()) {
            Toast.makeText(this, "El carrito esta vacio", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (nombre.isEmpty() || correo.isEmpty() || celular.isEmpty() || direccion.isEmpty() || referenciaDireccion.isEmpty() || fecha.isEmpty() || metodoSeleccionado == 0) {
            Toast.makeText(this, "Completa todos los datos para continuar", Toast.LENGTH_SHORT).show()
            return
        }

        if (recibeOtraPersona && (nombreRecibe.isEmpty() || dniRecibe.length != 8)) {
            Toast.makeText(this, "Ingresa nombre completo y DNI (8 digitos) de quien recibira", Toast.LENGTH_SHORT).show()
            return
        }

        val agrupado = agruparCarritoPorProducto()
        val detallePedido = construirDetallePedido(agrupado)
        val metodoPago = obtenerMetodoPago(metodoSeleccionado)

        val compraGuardada = guardarCompraEnSQLite(
            agrupado = agrupado,
            detallePedido = detallePedido,
            direccion = direccion,
            referenciaDireccion = referenciaDireccion,
            recibeOtraPersona = recibeOtraPersona,
            nombreRecibe = nombreRecibe,
            dniRecibe = dniRecibe,
            fechaEntregaInput = fecha,
            metodoPago = metodoPago
        )

        Carrito.items.clear()

        val intent = Intent(this, ConfirmacionCompraActivity::class.java).apply {
            putExtra("numero_orden", compraGuardada.numeroOrden)
            putExtra("detalle_pedido", compraGuardada.detallePedido)
            putExtra("fecha_entrega", compraGuardada.fechaEntrega)
            putExtra("total_pedido", compraGuardada.totalPedido)
        }
        startActivity(intent)
        finish()
    }

    private fun obtenerMetodoPago(posicionSeleccionada: Int): String {
        return metodosPago.getOrElse(posicionSeleccionada) { "No definido" }
    }

    private fun guardarCompraEnSQLite(
        agrupado: Map<Product, Int>,
        detallePedido: String,
        direccion: String,
        referenciaDireccion: String,
        recibeOtraPersona: Boolean,
        nombreRecibe: String,
        dniRecibe: String,
        fechaEntregaInput: String,
        metodoPago: String
    ): CompraGuardada {
        val dbHelper = AppDatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val fechaCompra = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)
        val fechaEntrega = formatearFechaEntregaParaBD(fechaEntregaInput)
        val receptor = if (recibeOtraPersona) {
            "Recibe: $nombreRecibe (DNI: $dniRecibe)"
        } else {
            "Recibe: titular de la compra"
        }
        val direccionCompleta = "$direccion | Ref: $referenciaDireccion | $receptor"

        val valoresLista = ContentValues().apply {
            put("fecha", fechaCompra)
            put("fecha_entrega", fechaEntrega)
            put("direccion", direccionCompleta)
            put("metodo_pago", metodoPago)
            put("id_usuario", 1)
        }

        val idLista = db.insert("lista_compras", null, valoresLista)
        val numeroOrden = generarNumeroOrden(idLista)

        agrupado.forEach { (producto, cantidad) ->
            val precioPagado = producto.precio * cantidad
            val valoresDetalle = ContentValues().apply {
                put("producto", producto.nombre)
                put("unidad_medida", "1/2 kg")
                put("cantidad", cantidad)
                put("precio_unitario", producto.precio)
                put("precio_pagado", precioPagado)
                put("id_lista_compras", idLista)
            }
            db.insert("detalle_lista", null, valoresDetalle)
        }

        db.close()
        val subtotal = subtotalProductos(agrupado)
        return CompraGuardada(
            numeroOrden = numeroOrden,
            detallePedido = detallePedido,
            fechaEntrega = fechaEntregaInput,
            totalPedido = "S/ %.2f".format(subtotal + DELIVERY_COST)
        )
    }

    private fun agruparCarritoPorProducto(): Map<Product, Int> {
        val agrupado = linkedMapOf<Int, Pair<Product, Int>>()
        Carrito.items.forEach { product ->
            val existente = agrupado[product.id]
            if (existente == null) {
                agrupado[product.id] = Pair(product, 1)
            } else {
                agrupado[product.id] = Pair(existente.first, existente.second + 1)
            }
        }
        return agrupado.values.associate { it.first to it.second }
    }

    private fun construirDetallePedido(agrupado: Map<Product, Int>): String {
        val detalle = StringBuilder()
        agrupado.forEach { (producto, cantidad) ->
            val peso = formatearPesoKg(cantidad)
            val subtotal = producto.precio * cantidad
            detalle.append("- ${producto.nombre}: $cantidad x 1/2 kg ($peso) - S/ %.2f".format(subtotal)).append("\n")
        }
        return detalle.toString().trim()
    }

    private fun subtotalProductos(agrupado: Map<Product, Int>): Double {
        return agrupado.entries.sumOf { (producto, cantidad) -> producto.precio * cantidad }
    }

    private fun formatearFechaEntregaParaBD(fechaInput: String): String {
        return try {
            val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = parser.parse(fechaInput) ?: return fechaInput
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            fechaInput
        }
    }

    private fun formatearPesoKg(cantidadMediosKilos: Int): String {
        val kilos = cantidadMediosKilos * 0.5
        return if (kilos % 1.0 == 0.0) {
            "${kilos.toInt()} kg"
        } else {
            "%.1f kg".format(Locale.US, kilos)
        }
    }

    private fun generarNumeroOrden(idLista: Long): String {
        val correlativo = (idLista - 1).coerceAtLeast(0).toInt()
        return String.format(Locale.US, "#FL-2026-%05d", correlativo)
    }
}

