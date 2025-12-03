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
{ fun puedeEditar(idUsuarioActual: Int): Boolean {
        // Puede editar si:
        // 1. Es el creador de la receta
        // 2. La receta es global Y ya la tiene guardada (favorita)
        return idUsuario == idUsuarioActual || (esGlobal && favorita)
    }
  fun esCreador(idUsuarioActual: Int): Boolean {
        return idUsuario == idUsuarioActual
    }
}