package com.example.myapplication.entity

object Carrito {
    val items = mutableListOf<Product>()

    fun total(): Double {
        return items.sumOf { it.precio }
    }

    fun eliminar(product: Product) {
        items.remove(product)
    }
}