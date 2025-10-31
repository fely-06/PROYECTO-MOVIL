package com.example.smartmealsproyecto

data class Ingrediente(
    val nombre: String,
    val cantidad: String,
    val unidad: String
) {
    // Constructor secundario para compatibilidad (si lo necesitas en algún lado)
    constructor(
        idIngrediente: Int?,
        idReceta: Int,
        nombre: String,
        cantidad: String,
        unidad: String
    ) : this(nombre, cantidad, unidad)

    override fun toString(): String {
        return "$cantidad $unidad de $nombre"
    }
}