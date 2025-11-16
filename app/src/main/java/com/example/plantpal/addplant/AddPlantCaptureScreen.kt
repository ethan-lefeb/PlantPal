
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

annotation class AddPlantCaptureScreen
