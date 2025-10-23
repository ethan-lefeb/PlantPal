package com.example.plantpal

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

@Composable
fun AddPlantCaptureScreen(
    currentUserId: String,
    apiKey: String,
    onSaved: (plantId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val plantRepository = remember { PlantRepository() }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var identifiedPlant by remember { mutableStateOf<Suggestion?>(null) }
    var savedPlantId by remember { mutableStateOf<String?>(null) }

    var customName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showSaveButton by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selected ->
            imageUri = selected
            scope.launch {
                handlePlantIdentification(
                    context = context,
                    uri = selected,
                    apiKey = apiKey,
                    setLoading = { isLoading = it },
                    setSuggestion = { suggestion ->
                        identifiedPlant = suggestion
                        customName = suggestion?.plant_name ?: "Unknown Plant"
                        showSaveButton = true
                    },
                    setError = { error = it }
                )
            }
        }
    }

    val tempPhotoFile = remember {
        File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg").apply { createNewFile() }
    }
    val tempUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tempPhotoFile
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempUri
            scope.launch {
                handlePlantIdentification(
                    context = context,
                    uri = tempUri,
                    apiKey = apiKey,
                    setLoading = { isLoading = it },
                    setSuggestion = { suggestion ->
                        identifiedPlant = suggestion
                        customName = suggestion?.plant_name ?: "Unknown Plant"
                        showSaveButton = true
                    },
                    setError = { error = it }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add a new plant", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Pick from Gallery")
                    }
                    TextButton(onClick = { cameraLauncher.launch(tempUri) }) {
                        Text("Take Photo")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()

            identifiedPlant != null && !showSaveButton -> {
                CircularProgressIndicator()
            }

            identifiedPlant != null && showSaveButton -> {
                val suggestion = identifiedPlant!!

                Text(
                    "🌿 Identified!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    suggestion.plant_details?.scientific_name ?: "Unknown Species",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Confidence: ${((suggestion.probability ?: 0.0) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                suggestion.plant_details?.let { details ->
                    Spacer(Modifier.height(8.dp))

                    if (details.watering != null) {
                        Text(
                            "💧 Water every ${details.watering.min}-${details.watering.max} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (details.common_names?.isNotEmpty() == true) {
                        Text(
                            "Also known as: ${details.common_names.take(3).joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Plant Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(Modifier.height(16.dp))

                if (savedPlantId != null) {
                    Text(
                        "Saved to your collection ✅",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { onSaved(savedPlantId!!) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View My Plants")
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                handlePlantSaving(
                                    context = context,
                                    uri = imageUri!!,
                                    plantName = customName.trim(),
                                    scientificName = suggestion.plant_details?.scientific_name ?: "Unknown Species",
                                    confidence = suggestion.probability ?: 0.0,
                                    notes = notes.trim(),
                                    userId = currentUserId,
                                    plantRepository = plantRepository,
                                    suggestion = suggestion,
                                    onSaved = { savedId -> savedPlantId = savedId },
                                    setLoading = { isLoading = it },
                                    setError = { error = it }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = customName.isNotBlank()
                    ) {
                        Text("Save Plant")
                    }
                }
            }

            error != null -> {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        imageUri = null
                        error = null
                        identifiedPlant = null
                        showSaveButton = false
                        customName = ""
                        notes = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}

private suspend fun handlePlantIdentification(
    context: Context,
    uri: Uri,
    apiKey: String,
    setLoading: (Boolean) -> Unit,
    setSuggestion: (Suggestion?) -> Unit,
    setError: (String?) -> Unit
) {
    try {
        setLoading(true)
        setError(null)

        val response = identifyPlantSuspend(context, uri, apiKey)
        val suggestion = response?.suggestions?.firstOrNull()

        if (suggestion == null) {
            setError("Could not identify plant. Try a clearer photo or check your API key.")
            setSuggestion(null)
        } else {
            setSuggestion(suggestion)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        setError("An error occurred: ${e.message}")
        setSuggestion(null)
    } finally {
        setLoading(false)
    }
}

private suspend fun handlePlantSaving(
    context: Context,
    uri: Uri,
    plantName: String,
    scientificName: String,
    confidence: Double,
    notes: String,
    userId: String,
    plantRepository: PlantRepository,
    suggestion: Suggestion,
    onSaved: (String) -> Unit,
    setLoading: (Boolean) -> Unit,
    setError: (String?) -> Unit
) {
    try {
        setLoading(true)
        setError(null)

        var downloadUrl = ""
        try {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                .child("plants/${java.util.UUID.randomUUID()}.jpg")
            storageRef.putFile(uri).await()
            downloadUrl = storageRef.downloadUrl.await()?.toString() ?: ""
        } catch (storageException: Exception) {
            android.util.Log.w("AddPlant", "Storage upload failed, saving plant without remote photo: ${storageException.message}")
            downloadUrl = uri.toString()
        }

        val careInfo = suggestion.toCareInfo().copy(
            cycle = PlantCareDefaults.getCycle(suggestion.plant_details?.taxonomy?.family),
            watering = PlantCareDefaults.getWateringFrequency(suggestion.plant_details?.taxonomy?.family),
            indoor = PlantCareDefaults.isIndoorPlant(suggestion.plant_details?.taxonomy?.family),
            careLevel = PlantCareDefaults.getCareLevel(suggestion.plant_details?.taxonomy?.family),
            droughtTolerant = PlantCareDefaults.isDroughtTolerant(suggestion.plant_details?.taxonomy?.family)
        )

        val wateringFrequency = if (careInfo.wateringMaxDays != null) {
            careInfo.wateringMaxDays
        } else {
            PlantCareDefaults.getWateringFrequencyDays(careInfo.family, careInfo.genus)
        }
        val sunlightReq = PlantCareDefaults.getSunlightRequirement(careInfo.family, careInfo.genus)
        val fertilizerFreq = PlantCareDefaults.getFertilizerFrequency(careInfo.family)
        val newPlant = PlantProfile(
            userId = userId,
            commonName = plantName,
            scientificName = scientificName,
            confidence = confidence,
            notes = notes,
            photoUrl = downloadUrl,
            careInfo = careInfo,
            wateringFrequency = wateringFrequency,
            sunlight = sunlightReq,
            fertilizerFrequency = fertilizerFreq,
            careProfile = CareProfile(
                wateringFrequency = wateringFrequency,
                sunlight = sunlightReq,
                fertilizerFrequency = fertilizerFreq
            )
        )

        val result = plantRepository.addPlant(newPlant)
        result.onSuccess { id ->
            onSaved(id)
        }.onFailure { e ->
            setError(e.message ?: "Error saving to Firebase")
        }

    } catch (e: Exception) {
        e.printStackTrace()
        setError("An error occurred: ${e.message}")
    } finally {
        setLoading(false)
    }
}