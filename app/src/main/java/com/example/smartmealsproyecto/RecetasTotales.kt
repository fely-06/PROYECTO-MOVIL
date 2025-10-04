package com.example.smartmealsproyecto

object RecetasTotales {
    // Todas las recetas disponibles (catálogo)
    val recetasGlobales = mutableListOf<Receta>()
    val todasLasRecetas = mutableListOf<Receta>()

    // Solo las recetas seleccionadas por el usuario ("Mis Recetas")
    val misRecetas = mutableListOf<Receta>()

    var nextId = 1

    fun inicializarRecetas() {
        if (todasLasRecetas.isEmpty()) {
            todasLasRecetas.apply {
                add(Receta(
                    nextId++,
                    "Ensalada César",
                    15,
                    "Mezcla lechuga romana con aderezo césar, crutones y queso parmesano.",
                    mutableListOf(
                        Ingrediente("Lechuga romana", "1", "pieza"),
                        Ingrediente("Aderezo césar", "100", "ml"),
                        Ingrediente("Crutones", "50", "g"),
                        Ingrediente("Queso parmesano", "30", "g")
                    ),
                    true
                ))
                add(Receta(
                    nextId++,
                    "Pollo a la Brasa",
                    45,
                    "Pollo marinado con especias y cocido al horno hasta dorar.",
                    mutableListOf(
                        Ingrediente("Pollo entero", "1", "pieza"),
                        Ingrediente("Paprika", "2", "cucharadas"),
                        Ingrediente("Ajo", "4", "dientes"),
                        Ingrediente("Aceite", "50", "ml")
                    ),
                    false
                ))
                add(Receta(
                    nextId++,
                    "Pasta Carbonara",
                    30,
                    "Pasta con salsa de huevo, queso parmesano y tocino.",
                    mutableListOf(
                        Ingrediente("Pasta", "400", "g"),
                        Ingrediente("Tocino", "150", "g"),
                        Ingrediente("Huevos", "3", "piezas"),
                        Ingrediente("Queso parmesano", "100", "g")
                    ),
                    false
                ))
            }
        }
        if(recetasGlobales.isEmpty()){
            recetasGlobales.addAll(todasLasRecetas)
        }
    }
}