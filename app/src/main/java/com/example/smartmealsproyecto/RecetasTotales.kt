package com.example.smartmealsproyecto

object RecetasTotales {
    // Todas las recetas disponibles (catálogo)
    val recetasGlobales = mutableListOf<Receta2>()
    val todasLasRecetas = mutableListOf<Receta2>()

    // Solo las recetas seleccionadas por el usuario ("Mis Recetas")
    val misRecetas = mutableListOf<Receta2>()

    var nextId = 1
}