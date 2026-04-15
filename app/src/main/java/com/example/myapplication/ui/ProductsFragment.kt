package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.CarritoActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.ProductAdapter
import com.example.myapplication.entity.Carrito
import com.example.myapplication.entity.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductsFragment : Fragment(R.layout.fragment_products) {

    private lateinit var rvProductos: RecyclerView
    private lateinit var fabIrCarrito: FloatingActionButton
    private val productos = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvProductos = view.findViewById(R.id.recyclerViewProductos)
        fabIrCarrito = view.findViewById(R.id.fabIrCarrito)

        rvProductos.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(productos) { producto ->
            Carrito.items.add(producto)
            Toast.makeText(
                requireContext(),
                producto.nombre + " agregado al carrito",
                Toast.LENGTH_SHORT
            ).show()
        }
        rvProductos.adapter = adapter

        fabIrCarrito.setOnClickListener {
            startActivity(Intent(requireContext(), CarritoActivity::class.java))
        }

        cargarProductosLocales()
    }

    private fun cargarProductosLocales() {
        productos.clear()

        productos.addAll(
            listOf(
                Product(1, "Manzana", 4.5, "1/2 kg - Chilena", R.drawable.manzana),
                Product(2, "Mandarina", 3.0, "1/2 kg - Sin pepa", R.drawable.mandarina),
                Product(3, "Naranja", 2.8, "1/2 kg - Huando", R.drawable.naranja),
                Product(4, "Sandia", 6.2, "1/2 kg - Isla", R.drawable.sandia),
                Product(5, "Durazno", 5.6, "1/2 kg - Kenwood", R.drawable.durazno),
                Product(6, "Mamey", 4.9, "1/2 kg - Importado", R.drawable.mamey),
                Product(7, "Fresa", 9.5, "1/2 kg - Limbo", R.drawable.fresa),
                Product(8, "Pina", 3.9, "1/2 kg - Golden", R.drawable.pina),
                Product(9, "Pitahaya", 4.2, "1/2 kg - Mortal", R.drawable.pitahaya),
                Product(10, "Platano", 2.4, "1/2 kg - Bellaco", R.drawable.platano)
            )
        )

        adapter.notifyDataSetChanged()
    }
}
