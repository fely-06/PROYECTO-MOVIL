package com.example.smartmealsproyecto

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    private val myContext: Context = context

    companion object {
        private const val DATABASE_NAME = "SMARTMEAL.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    @Throws(IOException::class)
    fun createDatabase() {
        if (!checkDataBase()) {
            this.writableDatabase.close()
            copyDataBase()
        }
    }

    private fun checkDataBase(): Boolean {
        return try {
            val dbPath = myContext.getDatabasePath(DATABASE_NAME)
            SQLiteDatabase.openDatabase(dbPath.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use {
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        myContext.assets.open(DATABASE_NAME).use { inputStream ->
            val outFileName = myContext.getDatabasePath(DATABASE_NAME).absolutePath
            FileOutputStream(outFileName).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) = Unit
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) = Unit
}