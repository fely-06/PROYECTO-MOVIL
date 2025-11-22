package com.example.smartmealsproyecto

import android.R
import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.absoluteValue

class ClaseCRUD(private val context: Context) {
    val dbHelper = DatabaseHelper.getInstance(context)
    var idusuario: Int = 0
    var nombreUsuario: String = ""
    var contrasenaUser: String = ""

    val itemsIngFaltantes = mutableListOf<ItemListaCompra>()

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
            "SELECT a.idAgenda, ar.idAgendaReceta, a.tipoComida,r.nombre,ar.hora,ar.notas FROM Agenda a " +
                    "INNER JOIN AgendaReceta ar ON a.idAgenda = ar.idAgenda INNER JOIN Receta r ON ar.idReceta = r.idReceta " +
                    "WHERE a.idUsuario = ? AND a.fecha = ? " +
                    "ORDER BY ar.hora",
            arrayOf(ClaseUsuario.iduser.toString(), fecha)
        )

        val tempList = mutableListOf<ClassDetAgenda>()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val idAg = getInt(getColumnIndexOrThrow("idAgenda"))
                    val id = getInt(getColumnIndexOrThrow("idAgendaReceta"))
                    val tipoC = getString(getColumnIndexOrThrow("tipoComida"))
                    val nombre = getString(getColumnIndexOrThrow("nombre"))
                    val hora = getString(getColumnIndexOrThrow("hora"))
                    val notas = getString(getColumnIndexOrThrow("notas"))?: ""
                    tempList.add(ClassDetAgenda(idAg,id, tipoC, nombre, hora, notas))
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
    suspend fun insertarRecetaAgenda(idReceta: Int, hora: String, notas: String, fecha: String, tipoC: String): Int{
        var db: SQLiteDatabase? = null
        var resultado: Long = -1
        var v: Int = 0
        try {
            db = dbHelper.writableDatabase
            val valores = ContentValues().apply {
                put("idUsuario", ClaseUsuario.iduser)
                put("fecha", fecha)
                put("tipoComida", tipoC)
            }
            resultado = db.insertOrThrow("Agenda", null, valores)
            if(resultado!=-1L) {
                val values = ContentValues().apply {
                    put("idAgenda", resultado)
                    put("idReceta", idReceta)
                    put("hora", hora)
                    put("notas", notas)
                }
                resultado = db.insertOrThrow("AgendaReceta", null, values)
                Toast.makeText(context, "Receta Guardada", Toast.LENGTH_SHORT).show()
                v = 1
            }
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Error SQLite al guardar receta: ${e.message}", Toast.LENGTH_SHORT).show()
            resultado = -1

        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
            resultado = -1

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

    data class ComidasXdia(
        val dia: String,
        val cant: Int
    )
    suspend fun obtenerCantComidasXdia(): List<ComidasXdia> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<ComidasXdia>()
        try {
            val cursor = db.rawQuery(
                """WITH RECURSIVE dias(dia) AS (
                        SELECT date('now')
                        UNION ALL 
                        SELECT date(dia, '+1 day')
                        FROM dias
                        WHERE dia < date('now', '+6 days')
                        )
                        SELECT 
                        dias.dia AS fecha,
                        COALESCE(COUNT(Agenda.idAgenda), 0) AS cantidadComidas
                        FROM dias
                        LEFT JOIN Agenda 
                        ON dias.dia = Agenda.fecha
                        AND Agenda.idUsuario = ?
                        GROUP BY dias.dia
                        ORDER BY dias.dia ASC;
                """,
                arrayOf(ClaseUsuario.iduser.toString())
            )
            lista.clear()
            with(cursor) {
                if (moveToFirst()) {
                    do {
                        val fecha = getString(0)
                        val cant = getInt(1)
                        lista.add(ComidasXdia(fecha.substring(5), cant))
                    } while (moveToNext())
                }
                close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        lista
    }

    ////////////////////////// Listas de compras /////////////////////////

    data class ingredientesNec(
        var nombre: String = "",
        var cantidad: Double = 0.0,
        var unidad: String = "")

    data class MapeoIngrediente(
        val nombreReceta: String,
        val nombreInventario: String
    )

    suspend fun IngredientesRequeridos(fechaIn: String, fechaFin: String): MutableList<ingredientesNec>{
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT i.nombre,sum(i.cantidad) as cantidad, i.unidad\n" +
                    "                    FROM Agenda a\n" +
                    "                    JOIN AgendaReceta ar ON a.idAgenda = ar.idAgenda\n" +
                    "                    JOIN Receta r ON ar.idReceta = r.idReceta\n" +
                    "                    JOIN Ingrediente i ON r.idReceta = i.idReceta\n" +
                    "                    WHERE a.idUsuario = ? AND a.fecha BETWEEN ? AND ? \n" +
                    "group by i.nombre, i.unidad",
            arrayOf(ClaseUsuario.iduser.toString(), fechaIn, fechaFin)
        )

        val tempList = mutableListOf<ingredientesNec>()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val nombre = getString(getColumnIndexOrThrow("nombre"))
                    val cantidad = getDouble(getColumnIndexOrThrow("cantidad"))
                    val unidad = getString(getColumnIndexOrThrow("unidad"))
                    tempList.add(ingredientesNec(nombre, cantidad, unidad))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return tempList
    }
    suspend fun IngredientesEnInventario(): MutableList<ingredientesNec>{
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT nombre, cantidad, unidad FROM Inventario WHERE idUsuario = ?",
            arrayOf(ClaseUsuario.iduser.toString())
        )

        val tempList = mutableListOf<ingredientesNec>()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val nombre = getString(getColumnIndexOrThrow("nombre"))
                    val cantidad = getDouble(getColumnIndexOrThrow("cantidad"))
                    val unidad = getString(getColumnIndexOrThrow("unidad"))
                    tempList.add(ingredientesNec(nombre, cantidad, unidad))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return tempList
    }
    suspend fun IngredientesMapeados(): MutableList<MapeoIngrediente>{
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT nombreReceta, nombreInventario FROM MapeoIngredienteUsuario WHERE idUsuario = ?",
            arrayOf(ClaseUsuario.iduser.toString())
        )

        val tempList = mutableListOf<MapeoIngrediente>()

        with(cursor) {
            if (moveToFirst()) {
                do {
                    val nombreR = getString(getColumnIndexOrThrow("nombreReceta"))
                    val nombreInv = getString(getColumnIndexOrThrow("nombreInventario"))
                    tempList.add(MapeoIngrediente(nombreR, nombreInv))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return tempList
    }

    data class IngredienteParaMapear(
        val nombreReceta: String,
        val candidatos: List<String>  // nombres tal como están en inventario
    )

    val conversiones = mapOf(
        // Volumen
        "litros" to 1000.0,      // 1 litro = 1000 ml
        "mililitros" to 1.0,     // 1 ml = 1 ml (base)

        // Peso
        "kilo(s)" to 1000.0,       // 1 kg = 1000 g
        "gramos" to 1.0,         // 1 g = 1 g (base)

        // Cantidad
        "unidad(es)" to 1.0,
        "dientes" to 1.0,
        "pieza(s)" to 1.0,
        "rebanada(s)" to 1.0,
        "cucharada(s)" to 5.0,///5 ml
        "cucharadita" to 3.0, //3 g
        "ramita" to 1.0
    )
    fun convertirAUnidadBase(cantidad: Double, unidad: String): Double? {
        val factor = conversiones[unidad.lowercase().trim()]
        return if (factor != null) {
            cantidad * factor
        } else {
            null
        }
    }

    fun verificarInventario(cantidadNecesaria: Double, unidadNecesaria: String, cantidadDisponible: Double, unidadDisponible: String): ResultadoVerificacion {

        val baseNecesaria = convertirAUnidadBase(cantidadNecesaria, unidadNecesaria)
        val baseDisponible = convertirAUnidadBase(cantidadDisponible, unidadDisponible)

        if (baseNecesaria == null || baseDisponible == null) {
            return ResultadoVerificacion.Error("Unidad no soportada")
        }

        val faltante = baseNecesaria - baseDisponible

        return if (faltante <= 0.0) {
            ResultadoVerificacion.Suficiente(faltante.absoluteValue)
        } else {
            ResultadoVerificacion.Insuficiente(faltante)
        }
    }

    sealed class ResultadoVerificacion {
        data class Suficiente(val sobrante: Double) : ResultadoVerificacion()
        data class Insuficiente(val faltante: Double) : ResultadoVerificacion()
        data class Error(val mensaje: String) : ResultadoVerificacion()
    }

    data class ItemListaCompra(
        val nombreIngrediente: String,
        val cantidad: Double,
        val unidad: String,
        val idLista: Long? = null,
        val idItem: Long? = null,
        val comprado: Boolean = false
    )
    object ListaCompraTemporal {
        var idUsuario: Int = 0
        var nombre: String = ""
        var items: List<ItemListaCompra> = emptyList()
    }


    suspend fun generarSugerenciasMapeo(fechaInicio: String, fechaFin: String): List<IngredienteParaMapear> {
        val ingredientesRequeridos = IngredientesRequeridos(fechaInicio, fechaFin)
        val inventarioNombres = IngredientesEnInventario().map { it.nombre.lowercase().trim() }
        //var inventario = IngredientesEnInventario().associateBy { it.nombre.lowercase().trim() }
        var inventario: MutableMap<String, ingredientesNec> =
            IngredientesEnInventario()
                .associateByTo(mutableMapOf()) { it.nombre.lowercase().trim() }
        val mapeos = IngredientesMapeados().associateBy { it.nombreReceta.lowercase().trim() }
        //val mapeosInv = IngredientesMapeados().associateBy { it.nombreInventario.lowercase().trim() }

        // ingredientes que NO están en inventario ni tienen mapeo
        val paraMapear = mutableListOf<IngredienteParaMapear>()

        for (nombreReceta in ingredientesRequeridos) {
            var key: String = nombreReceta.nombre.lowercase().trim()
            val uni = nombreReceta.unidad.lowercase().trim()
            val cant= nombreReceta.cantidad

            val candidatos = inventarioNombres.filter { candidato ->
                tieneCoincidenciaPlausible(key, candidato)
            }
            // tiene mapeo?
            if (mapeos.containsKey(key)) {
                val nombreEnInventario = mapeos[key]!!.nombreInventario.lowercase().trim()
                key = nombreEnInventario
                 ////consultar el nombre en inventario con el que esta mapeado
            }
            // directamente en inventario?
            if (inventarioNombres.contains(key)) {
                val unidadI = inventario[key]!!.unidad.lowercase().trim()
                var cantidadI: Double = inventario[key]!!.cantidad

                //val ingrediente = inventario[key]!!

                when (val resultado = verificarInventario(
                    cant, uni,
                    cantidadI, unidadI
                )) {
                    is ResultadoVerificacion.Suficiente -> {
                        // Restar de inventario
                        val cantidadEnBase =
                            convertirAUnidadBase(cant, uni)!!
                        val nuevaCantidadEnBase = convertirAUnidadBase(
                            cantidadI,
                            unidadI
                        )!! - cantidadEnBase
                        // Convertir de vuelta a la unidad original del inventario
                        inventario[key]?.cantidad =
                            nuevaCantidadEnBase / conversiones[unidadI.lowercase()
                                .trim()]!!
                    }

                    is ResultadoVerificacion.Insuficiente -> {
                        //calcula faltante
                        val cantidadNece =
                            convertirAUnidadBase(cant, uni)!!
                        val cantidadFalta = cantidadNece - convertirAUnidadBase(
                            cantidadI,
                            unidadI
                        )!!

                        val cantidadConver =
                            cantidadFalta / conversiones[unidadI.lowercase()
                                .trim()]!!
                        ///agrega a lista de compras
                        val item = ItemListaCompra(
                            nombreIngrediente = key,
                            cantidad = cantidadConver,
                            unidad = unidadI.lowercase().trim(),
                            comprado = false
                        )
                        itemsIngFaltantes.add(item)
                    }
                    is ResultadoVerificacion.Error -> {
                        println("Error: ${resultado.mensaje}")
                    }
                }
                continue
            }
            // Solo sugerir mapeo si hay candidatos
            if (candidatos.isNotEmpty()) {
                paraMapear.add(
                    IngredienteParaMapear(
                        nombreReceta = nombreReceta.nombre,
                        candidatos = candidatos
                    )
                )
                ///dialogo para mapear
            }
            else {
                // Si no hay candidatos agregar directo a lista de compras (sin mapeo)
                val item = ItemListaCompra(
                    nombreIngrediente = key,
                    cantidad = cant,
                    unidad = uni.lowercase().trim(),
                    comprado = false
                )
                itemsIngFaltantes.add(item)
            }
        }
        ListaCompraTemporal.idUsuario = ClaseUsuario.iduser
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        ListaCompraTemporal.nombre = "Lista del ${LocalDate.parse(fechaInicio, formatter)}/${LocalDate.parse(fechaFin, formatter)}"
        ListaCompraTemporal.items = itemsIngFaltantes

        return paraMapear
    }

    suspend fun obtenerLista(): MutableList<ItemListaCompra>{
        return itemsIngFaltantes
    }
    suspend fun obtenerDatosLita(): ListaCompraTemporal{
        return ListaCompraTemporal
    }
    fun guardarListaCompraBD(
        lista: ListaCompraTemporal,
        onReemplazoNecesario: (confirmar: () -> Unit) -> Unit
    ) {
        // Verificar si ya existe una lista con el mismo nombre y usuario
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "ListaCompra",
            arrayOf("idLista"),
            "idUsuario = ? AND nombre = ?",
            arrayOf(lista.idUsuario.toString(), lista.nombre),
            null, null, null
        )

        val existeLista = cursor.count > 0
        cursor.close()

        if (existeLista) {

            onReemplazoNecesario {
                reemplazarListaCompraBD(lista)
            }
        } else {
            insertarNuevaListaBD(lista)
        }
    }

    private fun insertarNuevaListaBD(lista: ListaCompraTemporal): Long {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val fecha = System.currentTimeMillis().toString()
            val idLista = db.insert(
                "ListaCompra",
                null,
                ContentValues().apply {
                    put("idUsuario", lista.idUsuario)
                    put("fechaCreacion", fecha)
                    put("nombre", lista.nombre)
                    put("estado", "pendiente")
                }
            )

            if (idLista == -1L) {
                throw SQLException("No se pudo crear la lista")
            }
            else{
                Toast.makeText(context, "Lista Guardada", Toast.LENGTH_SHORT).show()
            }

            for (item in lista.items) {
                db.insert(
                    "ListaCompraIngrediente",
                    null,
                    ContentValues().apply {
                        put("idLista", idLista)
                        put("nombreIngrediente", item.nombreIngrediente)
                        put("cantidad", item.cantidad)
                        put("unidad", item.unidad)
                        put("comprado", if (item.comprado) 1 else 0)
                    }
                )
            }

            db.setTransactionSuccessful()
            return idLista
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error al insertar nueva lista", e)
        } finally {
            db.endTransaction()
        }
    }

    private fun reemplazarListaCompraBD(lista: ListaCompraTemporal): Long {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(
                "ListaCompraIngrediente",
                "idLista IN (SELECT idLista FROM ListaCompra WHERE idUsuario = ? AND nombre = ?)",
                arrayOf(lista.idUsuario.toString(), lista.nombre)
            )
            val rowsDeleted = db.delete(
                "ListaCompra",
                "idUsuario = ? AND nombre = ?",
                arrayOf(lista.idUsuario.toString(), lista.nombre)
            )

            val idLista = insertarNuevaListaBD(lista)

            db.setTransactionSuccessful()
            return idLista
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error al reemplazar lista", e)
        } finally {
            db.endTransaction()
        }
    }

    fun tieneCoincidenciaPlausible(a: String, b: String): Boolean {
        if (a == b) return true
        if (a.contains(b, ignoreCase = true)) return true
        if (b.contains(a, ignoreCase = true)) return true

        // Coincidencia por primeras 3 letras
        val minLen = minOf(a.length, b.length)
        if (minLen >= 3) {
            val prefijoA = a.take(3)
            val prefijoB = b.take(3)
            if (prefijoA == prefijoB) return true
        }

        return false
    }

    fun obtenerNombresListasPorUsuario(idUsuario: Int): List<String> {
        val db = dbHelper.readableDatabase
        val nombres = mutableListOf<String>()

        val cursor = db.query(
            "ListaCompra",
            arrayOf("nombre"),
            "idUsuario = ?",
            arrayOf(idUsuario.toString()),
            null, null,
            "fechaCreacion DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                getString(0)?.let { if (it.isNotBlank()) nombres.add(it) }
            }
            close()
        }

        return nombres
    }
    fun obtenerItemsDeListaPorNombre(idUsuario: Int, nombreLista: String): List<ItemListaCompra> {
        val db = dbHelper.readableDatabase
        val items = mutableListOf<ItemListaCompra>()

        val query = """
        SELECT i.nombreIngrediente, i.cantidad, i.unidad, i.comprado
        FROM ListaCompraIngrediente i
        JOIN ListaCompra l ON i.idLista = l.idLista
        WHERE l.idUsuario = ? AND l.nombre = ?
        """.trimIndent()

        db.rawQuery(query, arrayOf(idUsuario.toString(), nombreLista)).use { cursor ->
            while (cursor.moveToNext()) {
                val nombre = cursor.getString(0)
                val cantidad = cursor.getDouble(1)
                val unidad = cursor.getString(2)
                val comprado = cursor.getInt(3) == 1

                items.add(ItemListaCompra(nombre, cantidad, unidad, comprado = comprado))
            }
        }

        return items
    }



}
