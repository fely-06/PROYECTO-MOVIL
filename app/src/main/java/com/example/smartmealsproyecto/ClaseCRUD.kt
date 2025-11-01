package com.example.smartmealsproyecto

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ClaseCRUD(private val context: Context) {
    val dbHelper = DatabaseHelper.getInstance(context)
    var idusuario: Int = 0
    var nombreUsuario: String = ""
    var contrasenaUser: String = ""

    fun iniciarBD(){
        try {
            dbHelper.createDatabase()
        } catch (e: IOException) {
            Toast.makeText(context, "Error al cargar la base de datos", Toast.LENGTH_LONG).show()
            return
        }
    }
    //////////////////////////////RECETAS/////////////////////////////////////
    // ============ READ ============
    suspend fun obtenerRecetasGlobales(recetasList: MutableList<Receta2>, recetasListOriginal: MutableList<Receta2>
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

    //////////////////////////////USUARIOS/////////////////////////////////////
    suspend fun insertarUsuario(nombre: String, contraseña: String): Int{
        var db: SQLiteDatabase? = null
        var resultado: Long = -1
        var v: Int = 0
        try {
            db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Usuario WHERE nombre = ?",
                arrayOf(nombre)
            )
            var existe = false
            if (cursor.moveToFirst()) {
                existe = cursor.getInt(0) > 0
            }
            cursor.close()

            if (existe) {
                Toast.makeText(context, "Nombre de usuario ya existente", Toast.LENGTH_SHORT).show()
                resultado = -2
            } else {
                db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put("nombre", nombre)
                    put("contrasena", contraseña)
                    put("fotoPerfil", "")
                    put("notificacionesActivas", false)
                }

                resultado = db.insertOrThrow("Usuario", null, values)
                Toast.makeText(context, "Usuario Creado. Inicia Sesion", Toast.LENGTH_SHORT).show()
                v = 1
            }
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al insertar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            resultado = -1

        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
            resultado = -1

        } finally {

        }
        return v
    }
    suspend fun eliminarUsuario(): Boolean{
        var db: SQLiteDatabase? = null
        var v: Boolean = false
        try {
            db = dbHelper.writableDatabase
            val filasEliminadas = db.delete(
                "Usuario",
                "idUsuario = ?",
                arrayOf(ClaseUsuario.iduser.toString())
            )
            if(filasEliminadas > 0)  //devuelve true
            {
                v = true
            }
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al eliminar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            Toast.makeText(context, "Error inesperado al eliminar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } finally {
        }
        return v
    }
    suspend fun actualizarContrasenaUsuario(nuevacontraseña: String): Boolean = withContext(Dispatchers.IO){
            var db: SQLiteDatabase? = null
            var v: Boolean = false
            try {
                db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put("contrasena", nuevacontraseña)
                }
                val filasActualizadas = db.update(
                    "Usuario",
                    values,
                    "idUsuario = ?",
                    arrayOf(ClaseUsuario.iduser.toString())
                )
                v = filasActualizadas > 0  // true si se actualizó
            } catch (e: SQLiteException) {
                Toast.makeText(context, "Error SQLite al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                false
            } catch (e: Exception) {
                Toast.makeText(context, "Error inesperado al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                false
            } finally {
            }
        return@withContext v
    }
    suspend fun login(nombre: String, contraseña: String): Int{
        var db: SQLiteDatabase? = null
        var v: Int = 0
        try {
            db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT idUsuario, nombre, contrasena FROM Usuario WHERE nombre = ? and contrasena = ?",
                arrayOf(nombre, contraseña)
            )
            if (cursor.moveToFirst()) {
                idusuario = cursor.getInt(0)
                nombreUsuario = cursor.getString(1)
                contrasenaUser = cursor.getString(2)
                ClaseUsuario.iduser = idusuario
                ClaseUsuario.nombre = nombreUsuario
                ClaseUsuario.contras = contrasenaUser
                v = 1
            }
            else{
                Toast.makeText(context, "Usuario o contraseña no validos", Toast.LENGTH_SHORT).show()
                ClaseUsuario.iduser = 0
                ClaseUsuario.nombre = ""
                ClaseUsuario.contras = ""
            }
            cursor.close()
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al buscar usuario: ${e.message}", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
        } finally {

        }
        return v
    }

    ///////////////////////////INVENTARIO/PRODUCTOS///////////////////////////////////
    suspend fun consultarInventario(productosUser: MutableList<Producto>){
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT idProducto, nombre, cantidad, unidad, codigoBarras FROM Inventario WHERE idUsuario = ?",
            arrayOf(ClaseUsuario.iduser.toString())
        )

        val tempList = mutableListOf<Producto>()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val id = getInt(getColumnIndexOrThrow("idProducto"))
                    val nombre = getString(getColumnIndexOrThrow("nombre"))
                    val cantidad = getDouble(getColumnIndexOrThrow("cantidad"))
                    val unidad = getString(getColumnIndexOrThrow("unidad"))
                    val codigoBarras = getString(getColumnIndexOrThrow("codigoBarras"))?: ""
                    tempList.add(Producto(id, nombre, cantidad, unidad, codigoBarras))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        withContext(Dispatchers.Main) {
            productosUser.clear()
            productosUser.addAll(tempList)
        }
    }

    suspend fun eliminarProducto(idProd: Int): Boolean{
        var db: SQLiteDatabase? = null
        var v: Boolean = false
        try {
            db = dbHelper.writableDatabase
            val filasEliminadas = db.delete(
                "Inventario",
                "idProducto = ?",
                arrayOf(idProd.toString())
            )
            if(filasEliminadas > 0)  //devuelve true
            {
                v = true
            }
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al eliminar producto: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            Toast.makeText(context, "Error inesperado al eliminar producto: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } finally {
        }
        return v
    }

    suspend fun insertarProducto(nombre: String, unidad: String, cantidad: Double, codBarras: String): Int{
        var db: SQLiteDatabase? = null
        var resultado: Long = -1
        var v: Int = 0
        try {
            db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Inventario WHERE nombre = ?",
                arrayOf(nombre)
            )
            var existe = false
            if (cursor.moveToFirst()) {
                existe = cursor.getInt(0) > 0
            }
            cursor.close()

            if (existe == false) {
                db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put("idUsuario", ClaseUsuario.iduser)
                    put("nombre", nombre)
                    put("cantidad", cantidad)
                    put("unidad", unidad)
                    put("codigoBarras", codBarras)
                }

                resultado = db.insertOrThrow("Inventario", null, values)
                Toast.makeText(context, "Producto Guardado", Toast.LENGTH_SHORT).show()
                v = 1
            }
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al guardar producto: ${e.message}", Toast.LENGTH_SHORT).show()
            resultado = -1

        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
            resultado = -1

        } finally {

        }
        return v
    }
    suspend fun actualizarProducto(idExistente: Int, unidad: String, cantidad: Double,cantidadA: Double ): Boolean{
        var db: SQLiteDatabase? = null
        var v: Boolean = false
        try {
            db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("unidad", unidad)
                put("cantidad", cantidad + cantidadA)
            }
            val filasActualizadas = db.update(
                "Inventario",
                values,
                "idProducto = ?",
                arrayOf(idExistente.toString())
            )
            v = filasActualizadas > 0  // true si se actualizó
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            Toast.makeText(context, "Error inesperado al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } finally {
        }
        return v
    }

    /////////////////////////////AGENDA/DETALLE_AGENDA////////////////////////////////////
    suspend fun consultarDetalleAgenda(){

    }
}