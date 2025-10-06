package com.example.plantpal

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object LocalPhotoStore {
    fun saveCopyToAppStorage(context: Context, source: Uri): Uri {
        val dir = File(context.filesDir, "plants").apply {
            if (!exists()) mkdirs()
        }

        val timestamp = System.currentTimeMillis()
        val dst = File(dir, "plant_$timestamp.jpg")

        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                dst.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Unable to read source image")
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                dst
            )
        } catch (e: Exception) {
            if (dst.exists()) dst.delete()
            throw e
        }
    }

    fun deletePhoto(context: Context, photoUri: String) {
        try {
            val uri = Uri.parse(photoUri)
            val file = File(uri.path ?: return)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            android.util.Log.w("LocalPhotoStore", "Failed to delete photo: ${e.message}")
        }
    }
}