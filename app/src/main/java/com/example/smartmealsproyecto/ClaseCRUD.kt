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

    suspend fun guardarRecetaGlobal(idReceta: Int): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        try {
            // Verificar que la receta sea global
            val cursorReceta = db.rawQuery(
                "SELECT esGlobal FROM Receta WHERE idReceta = ?",
                arrayOf(idReceta.toString())
            )

            var esGlobal = false
            if (cursorReceta.moveToFirst()) {
                esGlobal = cursorReceta.getInt(0) == 1
            }
            cursorReceta.close()

            if (!esGlobal) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Solo puedes guardar recetas globales", Toast.LENGTH_SHORT).show()
                }
                return@withContext false
            }

            // Verificar si ya está guardada
            val cursorExiste = db.rawQuery(
                "SELECT COUNT(*) FROM RecetaGuardada WHERE idUsuario = ? AND idReceta = ?",
                arrayOf(ClaseUsuario.iduser.toString(), idReceta.toString())
            )

            var yaExiste = false
            if (cursorExiste.moveToFirst()) {
                yaExiste = cursorExiste.getInt(0) > 0
            }
            cursorExiste.close()

            if (yaExiste) {
                /*withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Esta receta ya está guardada", Toast.LENGTH_SHORT).show()
                }*/
                return@withContext false
            }

            // Insertar en RecetaGuardada
            val values = ContentValues().apply {
                put("idUsuario", ClaseUsuario.iduser)
                put("idReceta", idReceta)
            }

            val resultado = db.insert("RecetaGuardada", null, values)

            if (resultado > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Receta guardada exitosamente", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al guardar receta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    suspend fun eliminarRecetaGuardada(idReceta: Int): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        try {
            val filasEliminadas = db.delete(
                "RecetaGuardada",
                "idUsuario = ? AND idReceta = ?",
                arrayOf(ClaseUsuario.iduser.toString(), idReceta.toString())
            )

            if (filasEliminadas > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Receta eliminada de tus guardadas", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al eliminar receta guardada: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    // ============ OBTENER RECETAS GLOBALES ============

    suspend fun obtenerRecetasGlobales(
        recetasList: MutableList<Receta2>,
        recetasListOriginal: MutableList<Receta2>
    ) = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT 
                r.idReceta, 
                r.idUsuario, 
                r.nombre, 
                r.descripcion, 
                r.tiempoPreparacion, 
                r.esGlobal,
                CASE 
                    WHEN rg.idReceta IS NOT NULL THEN 1 
                    ELSE 0 
                END AS favorita
            FROM Receta r
            LEFT JOIN RecetaGuardada rg 
                ON r.idReceta = rg.idReceta 
                AND rg.idUsuario = ?
            WHERE r.esGlobal = 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(ClaseUsuario.iduser.toString()))
        val tempList = mutableListOf<Receta2>()

        try {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("idReceta"))
                val idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")) ?: ""
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")) ?: ""
                val tiempo = cursor.getInt(cursor.getColumnIndexOrThrow("tiempoPreparacion"))
                val esGlobal = cursor.getInt(cursor.getColumnIndexOrThrow("esGlobal")) == 1
                val favorita = cursor.getInt(cursor.getColumnIndexOrThrow("favorita")) == 1

                tempList.add(Receta2(id, idUsuario, nombre, descripcion, tiempo, esGlobal, favorita))
            }
        } finally {
            cursor.close()
        }

        withContext(Dispatchers.Main) {
            recetasList.clear()
            recetasList.addAll(tempList)
            recetasListOriginal.clear()
            recetasListOriginal.addAll(tempList)
        }
    }

    // ============ OBTENER MIS RECETAS ============

    suspend fun obtenerMisRecetas(
        recetasList: MutableList<Receta2>,
        recetasListOriginal: MutableList<Receta2>
    ) = withContext(Dispatchers.IO) {
        val userId = ClaseUsuario.iduser
        val db = dbHelper.readableDatabase

        // Query corregido: Recetas propias + globales favoritas
        val query = """
            SELECT DISTINCT r.idReceta, r.idUsuario, r.nombre,r.descripcion,r.tiempoPreparacion,r.esGlobal,
            CASE 
                WHEN rg.idReceta IS NOT NULL THEN 1 
                ELSE 0 
            END AS favorita 
            FROM Receta r
            LEFT JOIN RecetaGuardada rg 
        ON r.idReceta = rg.idReceta AND rg.idUsuario = ?
        WHERE 
        (r.idUsuario = ?  and r.esGlobal = 0)           
        OR rg.idUsuario = ?;
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(ClaseUsuario.iduser.toString(),ClaseUsuario.iduser.toString(),ClaseUsuario.iduser.toString()))
        val tempList = mutableListOf<Receta2>()

        try {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("idReceta"))
                val idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")) ?: ""
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")) ?: ""
                val tiempo = cursor.getInt(cursor.getColumnIndexOrThrow("tiempoPreparacion"))
                val esGlobal = cursor.getInt(cursor.getColumnIndexOrThrow("esGlobal")) == 1
                val favorita = cursor.getInt(cursor.getColumnIndexOrThrow("favorita")) == 1

                tempList.add(Receta2(id, idUsuario, nombre, descripcion, tiempo, esGlobal, favorita))
            }
        } finally {
            cursor.close()
        }

        withContext(Dispatchers.Main) {
            recetasList.clear()
            recetasList.addAll(tempList)
            recetasListOriginal.clear()
            recetasListOriginal.addAll(tempList)
        }
    }
    suspend fun crearReceta(receta: Receta2, ingredientes: List<Ingrediente>): Long =
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            try {
                db.beginTransaction()

                val valuesReceta = ContentValues().apply {
                    put("idUsuario", receta.idUsuario)
                    put("nombre", receta.nombre)
                    put("descripcion", receta.descripcion ?: "")
                    put("tiempoPreparacion", receta.tiempoPreparacion)
                    put("esGlobal", if (receta.esGlobal) 1 else 0)
                    // ✅ NO incluir 'favorita'
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

    suspend fun actualizarReceta(receta: Receta2, ingredientes: List<Ingrediente>): Boolean =
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            try {
                db.beginTransaction()

                val valuesReceta = ContentValues().apply {
                    put("nombre", receta.nombre)
                    put("descripcion", receta.descripcion ?: "")
                    put("tiempoPreparacion", receta.tiempoPreparacion)
                    put("esGlobal", if (receta.esGlobal) 1 else 0)
                    // ✅ NO incluir 'favorita'
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

    suspend fun eliminarReceta(idReceta: Int): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        try {
            db.beginTransaction()

            // ✅ Eliminar de RecetaGuardada primero (si existe)
            db.delete("RecetaGuardada", "idReceta = ?", arrayOf(idReceta.toString()))

            // Eliminar ingredientes
            db.delete("Ingrediente", "idReceta = ?", arrayOf(idReceta.toString()))

            // Eliminar receta
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
    suspend fun consultarDetalleAgendaPorDia(fecha: String): MutableList<ClassDetAgenda>{
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ar.idAgendaReceta, a.tipoComida,r.nombre,ar.hora,ar.notas FROM Agenda a " +
                    "INNER JOIN AgendaReceta ar ON a.idAgenda = ar.idAgenda INNER JOIN Receta r ON ar.idReceta = r.idReceta " +
                    "WHERE a.idUsuario = ? AND a.fecha = ? " +
                    "ORDER BY ar.hora",
            arrayOf(ClaseUsuario.iduser.toString(), fecha)
        )

        val tempList = mutableListOf<ClassDetAgenda>()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val id = getInt(getColumnIndexOrThrow("idAgendaReceta"))
                    val tipoC = getString(getColumnIndexOrThrow("tipoComida"))
                    val nombre = getString(getColumnIndexOrThrow("nombre"))
                    val hora = getString(getColumnIndexOrThrow("hora"))
                    val notas = getString(getColumnIndexOrThrow("notas"))?: ""
                    tempList.add(ClassDetAgenda(id, tipoC, nombre, hora, notas))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return tempList
    }

    suspend fun eliminarRecetaDeAgenda(idDetalleAgenda: Int): Boolean{
        var db: SQLiteDatabase? = null
        var v: Boolean = false
        try {
            db = dbHelper.writableDatabase
            val filasEliminadas = db.delete(
                "AgendaReceta",
                "idAgendaReceta = ?",
                arrayOf(idDetalleAgenda.toString())
            )
            if(filasEliminadas > 0)  //devuelve true
            {
                v = true
            }
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al eliminar receta del plan: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            Toast.makeText(context, "Error inesperado al eliminar receta del plan: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        } finally {
        }
        return v
    }

    suspend fun actualizarDetalleAgenda(idDetalleAgenda: Int, hora: String, notas: String): Boolean{
        var db: SQLiteDatabase? = null
        var v: Boolean = false
        try {
            db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("hora", hora)
                put("notas", notas)
            }
            val filasActualizadas = db.update(
                "AgendaReceta",
                values,
                "idAgendaReceta = ?",
                arrayOf(idDetalleAgenda.toString())
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

    /////////////////////////////Grafica////////////////////////////////////

    suspend fun obtenerIngredientesMasUsados(idUsuario: Int,limite: Int = 5): List<IngredienteEstadistica> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<IngredienteEstadistica>()
        try {
            val cursor = db.rawQuery(
                """
                SELECT i.nombre, COUNT(i.nombre) as usos
            FROM Ingrediente i
            INNER JOIN Receta r ON i.idReceta = r.idReceta
            WHERE (r.idUsuario = ? AND r.esGlobal = 0)
               OR (r.esGlobal = 1 AND r.favorita = 1)
            GROUP BY i.nombre
            ORDER BY usos DESC
            LIMIT ?
                """,
                arrayOf(idUsuario.toString(),limite.toString())
            )

            with(cursor) {
                if (moveToFirst()) {
                    do {
                        val nombre = getString(0)
                        val usos = getInt(1)
                        lista.add(IngredienteEstadistica(nombre, usos))
                    } while (moveToNext())
                }
                close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        lista
    }
}
