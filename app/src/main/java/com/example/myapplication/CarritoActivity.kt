package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.CarritoAdapter
import com.example.myapplication.entity.Carrito

class CarritoActivity : AppCompatActivity() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnAtras: Button
    private lateinit var btnComprar: Button
    private lateinit var adapter: CarritoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        rvCarrito = findViewById(R.id.rvCarrito)
        tvTotal = findViewById(R.id.tvTotal)
        btnAtras = findViewById(R.id.btnAtras)
        btnComprar = findViewById(R.id.btnComprar)

        rvCarrito.layoutManager = LinearLayoutManager(this)

        adapter = CarritoAdapter(Carrito.items) {
            actualizarTotal()
        }

        rvCarrito.adapter = adapter

        btnAtras.setOnClickListener {
            finish()
        }

        btnComprar.setOnClickListener {
            if (Carrito.items.isEmpty()) {
                Toast.makeText(this, "Tu carrito esta vacio", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, ResumenCompraActivity::class.java))
            }
        }

        actualizarTotal()
    }

    override fun onResume() {
        super.onResume()
        adapter.refreshFromItems()
        actualizarTotal()
    }

    private fun actualizarTotal() {
        tvTotal.text = "Total: S/ %.2f".format(Carrito.total())
    }
}
