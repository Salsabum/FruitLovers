package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConfirmacionCompraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_compra)

        val tvNumeroOrden = findViewById<TextView>(R.id.tvNumeroOrden)
        val tvDetallePedido = findViewById<TextView>(R.id.tvDetallePedido)
        val tvTotalPedido = findViewById<TextView>(R.id.tvTotalPedido)
        val tvEntrega = findViewById<TextView>(R.id.tvEntrega)
        val btnVolverInicio = findViewById<Button>(R.id.btnVolverInicio)

        val numeroOrden = intent.getStringExtra("numero_orden") ?: "#FL-2026-00000"
        val detallePedido = intent.getStringExtra("detalle_pedido") ?: "Sin detalle"
        val totalPedido = intent.getStringExtra("total_pedido") ?: "S/ 0.00"
        val fechaEntrega = intent.getStringExtra("fecha_entrega") ?: "Pronto"

        tvNumeroOrden.text = "Numero de orden: $numeroOrden"
        tvDetallePedido.text = detallePedido
        tvTotalPedido.text = "Total: $totalPedido"
        tvEntrega.text = "Entrega estimada: $fechaEntrega"

        btnVolverInicio.setOnClickListener {
            val intentInicio = Intent(this, InicioActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intentInicio)
            finish()
        }
    }
}