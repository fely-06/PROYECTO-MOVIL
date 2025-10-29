package com.example.smartmealsproyecto

import android.content.Context
import android.widget.Toast
import java.io.IOException

class ClaseCRUD(private val context: Context) {
    val dbHelper = DatabaseHelper.getInstance(context)
    fun iniciarBD(){
        try {
            dbHelper.createDatabase() // Asegura que la BD esté copiada
        } catch (e: IOException) {
            Toast.makeText(context, "Error al cargar la base de datos", Toast.LENGTH_LONG).show()
            return
        }
    }
    fun obtenerRecetasGlobales(recetasList: MutableList<Receta2>, recetasListOriginal: MutableList<Receta2>){
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT idReceta, idUsuario, nombre, descripcion, tiempoPreparacion, esGlobal, " +
                "favorita FROM Receta WHERE esGlobal = 1", null)

        recetasList.clear()
        recetasListOriginal.clear()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val id = getInt(getColumnIndexOrThrow("idReceta"))
                    val idUsuario = getInt(getColumnIndexOrThrow("idUsuario"))
                    val nombre = getString(getColumnIndexOrThrow("nombre"))
                    val descripcion = getString(getColumnIndexOrThrow("descripcion")) ?: ""
                    val tiempo = getInt(getColumnIndexOrThrow("tiempoPreparacion"))
                    val esGlobal = getInt(getColumnIndexOrThrow("esGlobal")) == 1
                    val favorita = getInt(getColumnIndexOrThrow("favorita")) == 1

                    val receta = Receta2(id, idUsuario, nombre, descripcion, tiempo, esGlobal, favorita)
                    recetasList.add(receta)
                    recetasListOriginal.add(receta)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
    }

}