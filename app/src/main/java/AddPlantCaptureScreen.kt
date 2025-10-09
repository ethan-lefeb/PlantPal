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

    // pick from gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selected ->
            imageUri = selected
            scope.launch {
                handlePlantProcessing(
                    context = context,
                    uri = selected,
                    apiKey = apiKey,
                    userId = currentUserId,
                    plantRepository = plantRepository,
                    onSaved = { savedId -> savedPlantId = savedId; onSaved(savedId) },
                    setLoading = { isLoading = it },
                    setSuggestion = { identifiedPlant = it },
                    setError = { error = it }
                )
            }
        }
    }

    // camera capture
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
                handlePlantProcessing(
                    context = context,
                    uri = tempUri,
                    apiKey = apiKey,
                    userId = currentUserId,
                    plantRepository = plantRepository,
                    onSaved = { savedId -> savedPlantId = savedId; onSaved(savedId) },
                    setLoading = { isLoading = it },
                    setSuggestion = { identifiedPlant = it },
                    setError = { error = it }
                )
            }
        }
    }

    // UI
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add a new plant", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier.height(220.dp).fillMaxWidth(),
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
            identifiedPlant != null -> {
                val suggestion = identifiedPlant!!
                Text("🌿 ${suggestion.plant_name ?: "Unknown"}", style = MaterialTheme.typography.titleLarge)
                Text(suggestion.plant_details?.scientific_name ?: "Unknown Species", style = MaterialTheme.typography.bodyMedium)
                Text("Confidence: ${(suggestion.probability ?: 0.0 * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (savedPlantId != null) "Saved to your collection ✅" else "Identified successfully!",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}


private suspend fun handlePlantProcessing(
    context: Context,
    uri: Uri,
    apiKey: String,
    userId: String,
    plantRepository: PlantRepository,
    onSaved: (String) -> Unit,
    setLoading: (Boolean) -> Unit,
    setSuggestion: (Suggestion?) -> Unit,
    setError: (String?) -> Unit
) {
    try {
        setLoading(true)
        setError(null)

        // identifies plant
        val response = identifyPlantSuspend(context, uri, apiKey)
        val suggestion = response?.suggestions?.firstOrNull()

        if (suggestion == null) {
            setError("Could not identify plant. Try a clearer photo or check your API key.")
            return
        }

        setSuggestion(suggestion)

        // uploads image to firebase
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            .child("plants/${java.util.UUID.randomUUID()}.jpg")
        storageRef.putFile(uri).await()
        val downloadUrl = storageRef.downloadUrl.await()?.toString() ?: ""

        // creates PlantProfile
        val newPlant = PlantProfile(
            userId = userId,
            commonName = suggestion.plant_name ?: "Unknown",
            scientificName = suggestion.plant_details?.scientific_name ?: "Unknown Species",
            confidence = suggestion.probability ?: 0.0,
            photoUrl = downloadUrl
        )

        // saves under user collection
        val result = plantRepository.addPlant(newPlant)
        result.onSuccess { id ->

            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("plants")
                .document(id)
                .set(newPlant.copy(plantId = id))
                .await()
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
