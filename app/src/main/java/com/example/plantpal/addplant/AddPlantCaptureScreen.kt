package com.example.plantpal

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun AddPlantCaptureScreen(
    onSaved: (Uri) -> Unit,
    viewModel: AddPlantViewModel = viewModel()
) {
    val context = LocalContext.current
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
            if (bmp != null) {
                previewBitmap = bmp
                val uri = saveBitmapToCache(context, bmp)
                previewUri = uri
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) cameraLauncher.launch(null)
            else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add new plant via photo", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val permission = android.Manifest.permission.CAMERA
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(context, permission) -> cameraLauncher.launch(null)
                else -> permissionLauncher.launch(permission)
            }
        }) { Text("Take Photo") }

        Spacer(Modifier.height(16.dp))

        if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap!!.asImageBitmap(),
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(12.dp))

            Row {
                Button(
                    onClick = {
                        previewBitmap = null
                        previewUri = null
                    }
                ) { Text("Retake") }

                Spacer(Modifier.width(12.dp))

                Button(
                    enabled = previewUri != null,
                    onClick = {
                        previewUri?.let {
                            viewModel.addPhoto(it)
                            onSaved(it)
                            Toast.makeText(context, "Plant photo saved", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Save") }
            }
        } else {
            Text("No photo yet. Tap 'Take Photo'.")
        }
    }
}

private fun saveBitmapToCache(context: Context, bmp: Bitmap): Uri {
    val file = File(context.cacheDir, "plant_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}