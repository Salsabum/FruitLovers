package com.example.myapplication.entity

data class ListaCompras (
    val id : Int,
    val fecha : String,
    val estado : String,
    val idUsuario : Int,
    val subtotal : Double = 0.0
)
