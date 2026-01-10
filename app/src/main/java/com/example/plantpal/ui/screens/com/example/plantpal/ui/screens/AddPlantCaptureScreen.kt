package com.example.plantpal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import androidx.compose.ui.tooling.preview.Preview
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.CareProfile
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantCareDefaults
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantCareInfo
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.toCareInfo
import com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud.Suggestion
import com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud.identifyPlantSuspend
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.ui.theme.ForestGradientBalanced

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantCaptureScreen(
    currentUserId: String,
    apiKey: String,
    onSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf("photo") }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(ForestGradientBalanced)
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Add Plant") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = mode == "photo",
                        onClick = { mode = "photo" },
                        label = { Text("Identify from Photo") }
                    )
                    FilterChip(
                        selected = mode == "manual",
                        onClick = { mode = "manual" },
                        label = { Text("Add Manually") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                when (mode) {
                    "photo" -> AddPlantPhotoMode(
                        currentUserId = currentUserId,
                        apiKey = apiKey,
                        snackbarHostState = snackbarHostState,
                        onSaved = onSaved
                    )

                    "manual" -> AddPlantManualMode(
                        currentUserId = currentUserId,
                        snackbarHostState = snackbarHostState,
                        onSaved = onSaved
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantManualMode(
    currentUserId: String,
    snackbarHostState: SnackbarHostState,
    onSaved: (String) -> Unit
) {
    val repo = remember { PlantRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var water by remember { mutableStateOf(5) }
    var fertilizer by remember { mutableStateOf(30) }
    var sunlight by remember { mutableStateOf("Bright indirect") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val avatarConfig = remember { AvatarGenerator.generateRandomAvatar() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            PlantAvatar(
                avatarConfig = avatarConfig,
                health = "healthy",
                size = 130.dp
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(Modifier.height(16.dp))

            Text("Water every $water days")
            Slider(
                value = water.toFloat(),
                onValueChange = { water = it.toInt() },
                valueRange = 1f..14f
            )

            Spacer(Modifier.height(12.dp))

            Text("Fertilize every $fertilizer days")
            Slider(
                value = fertilizer.toFloat(),
                onValueChange = { fertilizer = it.toInt() },
                valueRange = 7f..60f
            )

            Spacer(Modifier.height(12.dp))

            Text("Sunlight Requirement")
            DropdownMenuBox(
                options = listOf("Low", "Medium", "Bright indirect", "Direct sun"),
                selected = sunlight,
                onSelected = { sunlight = it }
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    saving = true
                    error = null

                    scope.launch {
                        val plant = PlantProfile(
                            userId = currentUserId,
                            commonName = name,
                            scientificName = "",
                            confidence = 1.0,
                            notes = notes,
                            careInfo = PlantCareInfo(
                                family = "",
                                genus = "",
                                wateringMinDays = water,
                                wateringMaxDays = water,
                                sunlight = listOf(sunlight)
                            ),
                            avatarConfig = avatarConfig,
                            wateringFrequency = water,
                            fertilizerFrequency = fertilizer,
                            sunlight = sunlight,
                            careProfile = CareProfile(
                                wateringFrequency = water,
                                sunlight = sunlight,
                                fertilizerFrequency = fertilizer
                            )
                        )

                        repo.addPlant(plant)
                            .onSuccess { id ->
                                saving = false

                                snackbarHostState.showSnackbar(
                                    message = "🌱 Plant added!",
                                    withDismissAction = true
                                )
                                onSaved(id)
                            }
                            .onFailure { e ->
                                saving = false
                                error = e.message
                            }
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (saving) "Saving..." else "Save Plant")
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantPhotoMode(
    currentUserId: String,
    apiKey: String,
    snackbarHostState: SnackbarHostState,
    onSaved: (String) -> Unit
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
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var shouldLaunchCamera by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            scope.launch {
                handlePlantIdentification(
                    context = context,
                    uri = uri,
                    apiKey = apiKey,
                    setLoading = { isLoading = it },
                    setSuggestion = {
                        identifiedPlant = it
                        customName = it?.plant_name ?: "Unknown Plant"
                        showSaveButton = true
                    },
                    setError = { error = it }
                )
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        shouldLaunchCamera = false
        if (success && tempUri != null) {
            imageUri = tempUri
            scope.launch {
                handlePlantIdentification(
                    context = context,
                    uri = tempUri!!,
                    apiKey = apiKey,
                    setLoading = { isLoading = it },
                    setSuggestion = {
                        identifiedPlant = it
                        customName = it?.plant_name ?: "Unknown Plant"
                        showSaveButton = true
                    },
                    setError = { error = it }
                )
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) shouldLaunchCamera = true
        else error = "Camera permission required."
    }

    LaunchedEffect(shouldLaunchCamera, hasCameraPermission) {
        if (shouldLaunchCamera && hasCameraPermission) {
            val tempPhoto = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            tempPhoto.createNewFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempPhoto
            )
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Pick from Gallery")
                    }
                    TextButton(onClick = {
                        if (hasCameraPermission) shouldLaunchCamera = true
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Take Photo")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()

            error != null -> {
                Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
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
                ) { Text("Try Again") }
            }

            identifiedPlant != null && showSaveButton -> {
                val suggestion = identifiedPlant!!

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text("🌿 Identified!", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        Text(
                            suggestion.plant_details?.scientific_name ?: "Unknown species",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            "Confidence: ${((suggestion.probability ?: 0.0) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Plant Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        if (savedPlantId != null) {
                            Text("Saved! 🌱")
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        handlePlantSaving(
                                            context = context,
                                            uri = imageUri!!,
                                            plantName = customName.trim(),
                                            scientificName = suggestion.plant_details?.scientific_name
                                                ?: "Unknown species",
                                            confidence = suggestion.probability ?: 0.0,
                                            notes = notes.trim(),
                                            userId = currentUserId,
                                            plantRepository = plantRepository,
                                            suggestion = suggestion,
                                            onSaved = { savedPlantId = it },
                                            setLoading = { isLoading = it },
                                            setError = { error = it }
                                        )
                                        snackbarHostState.showSnackbar("🌱 Plant added!")
                                        onSaved(savedPlantId!!)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = customName.isNotBlank()
                            ) {
                                Text("Save Plant")
                            }
                        }
                    }
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
            setError("Could not identify plant.")
            setSuggestion(null)
        } else {
            setSuggestion(suggestion)
        }
    } catch (e: Exception) {
        setError("Error: ${e.message}")
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
            val storageRef = FirebaseStorage.getInstance()
                .reference.child("plants/${UUID.randomUUID()}.jpg")

            storageRef.putFile(uri).await()
            downloadUrl = storageRef.downloadUrl.await()?.toString() ?: ""
        } catch (_: Exception) {
            downloadUrl = uri.toString()
        }

        val careInfo = suggestion.toCareInfo().copy(
            cycle = PlantCareDefaults.getCycle(suggestion.plant_details?.taxonomy?.family),
            watering = PlantCareDefaults.getWateringFrequency(suggestion.plant_details?.taxonomy?.family),
            indoor = PlantCareDefaults.isIndoorPlant(suggestion.plant_details?.taxonomy?.family),
            careLevel = PlantCareDefaults.getCareLevel(suggestion.plant_details?.taxonomy?.family),
            droughtTolerant = PlantCareDefaults.isDroughtTolerant(suggestion.plant_details?.taxonomy?.family)
        )

        val watering = careInfo.wateringMaxDays
            ?: PlantCareDefaults.getWateringFrequencyDays(careInfo.family, careInfo.genus)

        val sunlightReq = PlantCareDefaults.getSunlightRequirement(careInfo.family, careInfo.genus)
        val fertFreq = PlantCareDefaults.getFertilizerFrequency(careInfo.family)

        val avatarConfig = AvatarGenerator.generateAvatarForPlant(
            family = careInfo.family,
            genus = careInfo.genus,
            commonName = plantName,
            scientificName = scientificName
        )

        val newPlant = PlantProfile(
            userId = userId,
            commonName = plantName,
            scientificName = scientificName,
            confidence = confidence,
            notes = notes,
            photoUrl = downloadUrl,
            careInfo = careInfo,
            avatarConfig = avatarConfig,
            wateringFrequency = watering,
            sunlight = sunlightReq,
            fertilizerFrequency = fertFreq,
            careProfile = CareProfile(
                wateringFrequency = watering,
                sunlight = sunlightReq,
                fertilizerFrequency = fertFreq
            )
        )

        plantRepository.addPlant(newPlant).onSuccess { id ->
            onSaved(id)
        }.onFailure { e ->
            setError(e.message)
        }

    } catch (e: Exception) {
        setError("Error: ${e.message}")
    } finally {
        setLoading(false)
    }
}


@Composable
fun DropdownMenuBox(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            label = { Text("Select") }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPlantManualModePreview() {
    MaterialTheme {
        AddPlantManualMode(
            currentUserId = "preview-user",
            snackbarHostState = SnackbarHostState(),
            onSaved = {}
        )
    }
}

