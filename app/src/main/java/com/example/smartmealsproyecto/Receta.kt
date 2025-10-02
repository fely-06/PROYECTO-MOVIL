package com.example.smartmealsproyecto

data class Receta(
    var id: Int,
    var nombre: String,
    var tiempoMinutos: Int,
    var descripcion: String,
    val ingredientes: MutableList<Ingrediente>
)
