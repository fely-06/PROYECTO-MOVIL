package com.example.smartmealsproyecto

data class Receta2(
    val id: Int,
    val idUsuario: Int,
    val nombre: String,
    val descripcion: String? = null,
    val tiempoPreparacion: Int,
    val esGlobal: Boolean,
    val favorita: Boolean = false,
    val imagenRuta: String? = null
)