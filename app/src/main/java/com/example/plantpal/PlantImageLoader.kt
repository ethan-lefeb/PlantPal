package com.example.plantpal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.io.InputStream

fun loadPlantPhoto(context: Context, photoUrl: String): Bitmap? {
    return try {
        val uri = Uri.parse(photoUrl)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use {
            BitmapFactory.decodeStream(it)
        }
    } catch (e: Exception) {
        android.util.Log.e("PlantImageLoader", "Failed to load photo: ${e.message}")
        null
    }
}

@Composable
fun rememberPlantBitmap(photoUrl: String): Bitmap? {
    val context = LocalContext.current
    var bitmap by remember(photoUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUrl) {
        bitmap = loadPlantPhoto(context, photoUrl)
    }

    return bitmap
}