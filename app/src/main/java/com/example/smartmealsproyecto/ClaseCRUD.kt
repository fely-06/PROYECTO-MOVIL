package com.example.smartmealsproyecto

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ClaseCRUD(private val context: Context) {
    val dbHelper = DatabaseHelper.getInstance(context)

    fun iniciarBD(){
        try {
            dbHelper.createDatabase()
        } catch (e: IOException) {
            Toast.makeText(context, "Error al cargar la base de datos", Toast.LENGTH_LONG).show()
            return
        }
    }

    // ============ READ ============
    suspend fun obtenerRecetasGlobales(
        recetasList: MutableList<Receta2>,
        recetasListOriginal: MutableList<Receta2>
    ) = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT idReceta, idUsuario, nombre, descripcion, tiempoPreparacion, esGlobal, favorita FROM Receta WHERE esGlobal = 1",
            null
        )

        val tempList = mutableListOf<Receta2>()

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

                    tempList.add(Receta2(id, idUsuario, nombre, descripcion, tiempo, esGlobal, favorita))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()

        // Actualizar en el hilo principal
        withContext(Dispatchers.Main) {
            recetasList.clear()
            recetasList.addAll(tempList)
            recetasListOriginal.clear()
            recetasListOriginal.addAll(tempList)
        }
    }

    // ============ CREATE ============
    suspend fun crearReceta(receta: Receta2, ingredientes: List<Ingrediente>): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase

        try {
            db.beginTransaction()

            val valuesReceta = ContentValues().apply {
                put("idUsuario", receta.idUsuario)
                put("nombre", receta.nombre)
                put("descripcion", receta.descripcion ?: "")
                put("tiempoPreparacion", receta.tiempoPreparacion)
                put("esGlobal", if (receta.esGlobal) 1 else 0)
                put("favorita", if (receta.favorita) 1 else 0)
            }

            val idReceta = db.insert("Receta", null, valuesReceta)
            if (idReceta == -1L) {
                throw SQLException("No se pudo insertar la receta")
            }

            for (ing in ingredientes) {
                val valuesIng = ContentValues().apply {
                    put("idReceta", idReceta)
                    put("nombre", ing.nombre)
                    put("cantidad", ing.cantidad)
                    put("unidad", ing.unidad)
                }

                if (db.insert("Ingrediente", null, valuesIng) == -1L) {
                    throw SQLException("Error al insertar ingrediente: ${ing.nombre}")
                }
            }

            db.setTransactionSuccessful()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Receta creada exitosamente", Toast.LENGTH_SHORT).show()
            }

            idReceta

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al crear receta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            -1L
        } finally {
            db.endTransaction()
        }
    }

    // ============ UPDATE ============
    suspend fun actualizarReceta(receta: Receta2, ingredientes: List<Ingrediente>): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase

        try {
            db.beginTransaction()

            val valuesReceta = ContentValues().apply {
                put("nombre", receta.nombre)
                put("descripcion", receta.descripcion ?: "")
                put("tiempoPreparacion", receta.tiempoPreparacion)
                put("esGlobal", if (receta.esGlobal) 1 else 0)
                put("favorita", if (receta.favorita) 1 else 0)
            }

            val rowsReceta = db.update(
                "Receta",
                valuesReceta,
                "idReceta = ?",
                arrayOf(receta.id.toString())
            )

            if (rowsReceta == 0) {
                throw SQLException("No se encontró la receta con ID: ${receta.id}")
            }

            db.delete("Ingrediente", "idReceta = ?", arrayOf(receta.id.toString()))

            for (ing in ingredientes) {
                val valuesIng = ContentValues().apply {
                    put("idReceta", receta.id)
                    put("nombre", ing.nombre)
                    put("cantidad", ing.cantidad)
                    put("unidad", ing.unidad)
                }

                if (db.insert("Ingrediente", null, valuesIng) == -1L) {
                    throw SQLException("Error al actualizar ingrediente: ${ing.nombre}")
                }
            }

            db.setTransactionSuccessful()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Receta actualizada exitosamente", Toast.LENGTH_SHORT).show()
            }

            true

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        } finally {
            db.endTransaction()
        }
    }

    // ============ DELETE ============
    suspend fun eliminarReceta(idReceta: Int): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase

        try {
            db.beginTransaction()

            db.delete("Ingrediente", "idReceta = ?", arrayOf(idReceta.toString()))

            val rowsReceta = db.delete("Receta", "idReceta = ?", arrayOf(idReceta.toString()))

            if (rowsReceta == 0) {
                throw SQLException("No se encontró la receta con ID: $idReceta")
            }

            db.setTransactionSuccessful()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Receta eliminada exitosamente", Toast.LENGTH_SHORT).show()
            }

            true

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        } finally {
            db.endTransaction()
        }
    }

    // ============ MÉTODOS AUXILIARES ============
    suspend fun actualizarFavorita(idReceta: Int, esFavorita: Boolean): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase

        try {
            val values = ContentValues().apply {
                put("favorita", if (esFavorita) 1 else 0)
            }

            val rows = db.update("Receta", values, "idReceta = ?", arrayOf(idReceta.toString()))
            rows > 0

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al actualizar favorita: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}