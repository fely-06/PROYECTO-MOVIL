package com.example.smartmealsproyecto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageHelper {

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("Pictures")
        return File.createTempFile("RECIPE_${timeStamp}_", ".jpg", storageDir)
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri, recetaId: Int): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Comprimir y redimensionar
            val scaledBitmap = scaleBitmap(bitmap, 800, 800)

            // Crear directorio si no existe
            val directory = File(context.filesDir, "recetas_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Guardar archivo
            val fileName = "receta_${recetaId}_${System.currentTimeMillis()}.jpg"
            val file = File(directory, fileName)

            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            bitmap.recycle()
            scaledBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )

        if (scale >= 1) return bitmap

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun loadImageFromPath(path: String?): Bitmap? {
        if (path.isNullOrEmpty()) return null
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun deleteImage(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            val file = File(path)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}