package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entity.Product

class ProductAdapter(
    private val items: List<Product>,
    private val onAddClick: (Product) -> Unit
) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val p = items[position]

        holder.tvTitle.text = p.nombre
        holder.tvPrice.text = "S/ %.2f".format(p.precio)
        holder.tvCategory.text = p.descripcion

        holder.ivImage.setImageResource(p.imagen)
        holder.itemView.setOnClickListener {
            onAddClick(p)
        }
    }

    override fun getItemCount() = items.size

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivFotoProducto)
        val tvTitle: TextView = itemView.findViewById(R.id.tvNombreProducto)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrecioProducto)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoriaProducto)
    }
}
