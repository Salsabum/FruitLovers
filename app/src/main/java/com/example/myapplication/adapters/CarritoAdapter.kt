package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entity.Product

class CarritoAdapter(
    private val items: MutableList<Product>,
    private val actualizar: () -> Unit
)
    : RecyclerView.Adapter<CarritoAdapter.ViewHolder>() {

    private data class CarritoUiItem(
        val product: Product,
        val cantidad: Int
    )

    private val uiItems = mutableListOf<CarritoUiItem>()

    init {
        refreshFromItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = uiItems[position]
        val p = item.product

        holder.tvNombre.text = p.nombre
        holder.tvPrecio.text = "S/ %.2f por 1/2 kg | Subtotal: S/ %.2f".format(p.precio, p.precio * item.cantidad)
        holder.tvCantidad.text = "${formatearPesoKg(item.cantidad)} kg"
        holder.ivProducto.setImageResource(p.imagen)

        holder.btnMas.setOnClickListener {
            items.add(p)
            refreshFromItems()
            actualizar()
        }

        holder.btnMenos.setOnClickListener {
            val index = items.indexOfFirst { it.id == p.id }
            if (index >= 0) {
                items.removeAt(index)
                refreshFromItems()
                actualizar()
            }
        }

        holder.btnQuitar.setOnClickListener {
            items.removeAll { it.id == p.id }
            refreshFromItems()
            actualizar()
        }
    }

    override fun getItemCount() = uiItems.size

    fun refreshFromItems() {
        val agrupados = linkedMapOf<Int, MutablePair>()
        items.forEach { product ->
            val actual = agrupados[product.id]
            if (actual == null) {
                agrupados[product.id] = MutablePair(product, 1)
            } else {
                actual.cantidad += 1
            }
        }
        uiItems.clear()
        uiItems.addAll(agrupados.values.map { CarritoUiItem(it.product, it.cantidad) })
        notifyDataSetChanged()
    }

    private data class MutablePair(
        val product: Product,
        var cantidad: Int
    )

    private fun formatearPesoKg(cantidadMediosKg: Int): String {
        val pesoKg = cantidadMediosKg * 0.5
        val esEntero = pesoKg % 1.0 == 0.0
        return if (esEntero) {
            pesoKg.toInt().toString()
        } else {
            String.format("%.1f", pesoKg)
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivProducto: ImageView = v.findViewById(R.id.ivProducto)
        val tvNombre: TextView = v.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = v.findViewById(R.id.tvPrecio)
        val tvCantidad: TextView = v.findViewById(R.id.tvCantidad)
        val btnMenos: Button = v.findViewById(R.id.btnMenos)
        val btnMas: Button = v.findViewById(R.id.btnMas)
        val btnQuitar: Button = v.findViewById(R.id.btnQuitar)
    }
}
