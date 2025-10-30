package com.example.smartmealsproyecto

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
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
    }fun crearReceta(receta: Receta2, ingredientes: List<Ingrediente>): Long {
        val db = dbHelper.writableDatabase
        return try {
            db.beginTransaction() // 👈 inicia la transacción

            // 1. Insertar receta
            val valuesReceta = ContentValues()
            valuesReceta.put("idUsuario", receta.idUsuario)
            valuesReceta.put("nombre", receta.nombre)
            valuesReceta.put("descripcion", receta.descripcion ?: "")
            valuesReceta.put("tiempoPreparacion", receta.tiempoPreparacion)
            valuesReceta.put("esGlobal", if (receta.esGlobal) 1 else 0)
            valuesReceta.put("favorita", if (receta.favorita) 1 else 0)

            val idReceta = db.insert("Receta", null, valuesReceta)
            if (idReceta == -1L) {
                throw SQLException("No se pudo insertar la receta")
            }

            // 2. Insertar ingredientes
            for (ing in ingredientes) {
                val valuesIng = ContentValues()
                valuesIng.put("idReceta", idReceta)
                valuesIng.put("nombre", ing.nombre)
                valuesIng.put("cantidad", ing.cantidad)
                valuesIng.put("unidad", ing.unidad)

                if (db.insert("Ingrediente", null, valuesIng) == -1L) {
                    throw SQLException("Error al insertar ingrediente")
                }
            }
 db.setTransactionSuccessful()
            idReceta

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            -1L
        } finally {
            db.endTransaction()
        }
    }
}