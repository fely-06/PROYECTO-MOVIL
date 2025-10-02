package com.example.smartmealsproyecto

data class Ingrediente(
    val nombre: String,
    val cantidad: String,
    val unidad: String
) {
    override fun toString(): String {
        return "$cantidad $unidad de $nombre"
    }
}