package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entity.ListaCompras

class HistorialAdapter(var listaCompras : List<ListaCompras>) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {
    private var onItemClickListener : ((ListaCompras) -> Unit)? = null
    private var onRepeatClickListener : ((ListaCompras) -> Unit)? = null

    fun setOnItemClickListener(listener : (ListaCompras) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnRepeatClickListener(listener: (ListaCompras) -> Unit) {
        onRepeatClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): HistorialViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder : HistorialViewHolder, position : Int) {
        val lista : ListaCompras = listaCompras[position]
        holder.tvIdLista.text = "Lista #${String.format("%03d", lista.id)}"
        holder.tvFecha.text = "Fecha: ${lista.fecha}"
        holder.tvEstado.text = "Estado: ${lista.estado}"
        val subtotal = lista.subtotal
        holder.tvSubtotal.text = "Total: S/ ${String.format("%.2f", subtotal)}"
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(lista)
        }
        holder.btnRepetirPedido.setOnClickListener {
            onRepeatClickListener?.invoke(lista)
        }
    }

    override fun getItemCount() : Int {
        return listaCompras.size
    }

    inner class HistorialViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var tvIdLista : TextView = itemView.findViewById(R.id.tvIdLista)
        var tvSubtotal : TextView = itemView.findViewById(R.id.tvSubtotal)
        var tvFecha : TextView = itemView.findViewById(R.id.tvFecha)
        var tvEstado : TextView = itemView.findViewById(R.id.tvEstado)
        var btnRepetirPedido: Button = itemView.findViewById(R.id.btnRepetirPedido)
    }

}
