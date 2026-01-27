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
import androidx.compose.material3.FilterChipDefaults
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
import com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud.getPlantDetailsById
import com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud.identifyPlantSuspend
import com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud.searchPlantCareDetails
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud.searchPlantCareDetails
import com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud.getPlantDetailsById
import com.example.plantpal.com.example.plantpal.systems.helpers.PlantRepository
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
                        label = { Text("Identify from Photo") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2E7D32),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,

                            containerColor = Color(0xFFE8F5E9),
                            labelColor = Color(0xFF2E7D32)
                        )
                    )

                    FilterChip(
                        selected = mode == "manual",
                        onClick = { mode = "manual" },
                        label = { Text("Add Manually") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2E7D32),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,

                            containerColor = Color(0xFFE8F5E9),
                            labelColor = Color(0xFF2E7D32)
                        )
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
                options = listOf(
                    "Full sun (6+ hours)",
                    "Partial shade (3-6 hours)",
                    "Bright indirect",
                    "Low light"
                ),
                selected = sunlight,
                onSelected = { sunlight = it }
            )

            Spacer(Modifier.height(16.dp))

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    scope.launch {
                        saving = true
                        val plant = PlantProfile(
                            userId = currentUserId,
                            commonName = name.trim(),
                            scientificName = "Unknown species",
                            confidence = 1.0,
                            notes = notes.trim(),
                            photoUrl = "",
                            avatarConfig = avatarConfig,
                            wateringFrequency = water,
                            sunlight = sunlight,
                            fertilizerFrequency = fertilizer,
                            careProfile = CareProfile(
                                wateringFrequency = water,
                                sunlight = sunlight,
                                fertilizerFrequency = fertilizer
                            )
                        )
                        repo.addPlant(plant).onSuccess { id ->
                            snackbarHostState.showSnackbar("🌱 Plant added!")
                            onSaved(id)
                        }.onFailure { e ->
                            error = e.message
                        }
                        saving = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && !saving
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                } else {
                    Text("Save Plant")
                }
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
    var suggestion by remember { mutableStateOf<Suggestion?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var customName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var savedPlantId by remember { mutableStateOf<String?>(null) }

    val tempFile = remember {
        File(context.cacheDir, "plant_photo_${System.currentTimeMillis()}.jpg").also {
            it.createNewFile()
        }
    }

    val tempUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempUri
            scope.launch {
                handlePlantIdentification(
                    context = context,
                    uri = tempUri,
                    apiKey = apiKey,
                    setLoading = { isLoading = it },
                    setSuggestion = { suggestion = it },
                    setError = { error = it }
                )
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            scope.launch {
                handlePlantIdentification(
                    context = context,
                    uri = it,
                    apiKey = apiKey,
                    setLoading = { isLoading = it },
                    setSuggestion = { suggestion = it },
                    setError = { error = it }
                )
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(tempUri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission denied")
            }
        }
    }

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
            if (imageUri == null) {
                Text("Take or select a photo of your plant", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            val permission = Manifest.permission.CAMERA
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(context, permission) -> {
                                    cameraLauncher.launch(tempUri)
                                }
                                else -> {
                                    permissionLauncher.launch(permission)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📷 Camera")
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🖼️ Gallery")
                    }
                }
            } else {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Plant Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = {
                    imageUri = null
                    suggestion = null
                    error = null
                    customName = ""
                    notes = ""
                }) {
                    Text("Retake Photo")
                }

                if (isLoading) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Text("Identifying plant...")
                }

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }

                if (suggestion != null) {
                    Spacer(Modifier.height(16.dp))

                    if (customName.isBlank()) {
                        customName = suggestion!!.plant_name ?: "Unknown Plant"
                    }

                    Text(
                        "Identified: ${suggestion!!.plant_name ?: "Unknown"}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (suggestion!!.plant_details?.scientific_name != null) {
                        Text(
                            suggestion!!.plant_details!!.scientific_name!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (suggestion!!.probability != null) {
                        Text(
                            "Confidence: ${((suggestion!!.probability ?: 0.0) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

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
                                        scientificName = suggestion!!.plant_details?.scientific_name
                                            ?: "Unknown species",
                                        confidence = suggestion!!.probability ?: 0.0,
                                        notes = notes.trim(),
                                        userId = currentUserId,
                                        plantRepository = plantRepository,
                                        suggestion = suggestion!!,
                                        onSaved = { savedPlantId = it },
                                        setLoading = { isLoading = it },
                                        setError = { error = it }
                                    )
                                    snackbarHostState.showSnackbar("🌱 Plant added with enhanced care info!")
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

        // Upload image to Firebase Storage
        var downloadUrl = ""
        try {
            val storageRef = FirebaseStorage.getInstance()
                .reference.child("plants/${UUID.randomUUID()}.jpg")

            storageRef.putFile(uri).await()
            downloadUrl = storageRef.downloadUrl.await()?.toString() ?: ""
        } catch (_: Exception) {
            downloadUrl = uri.toString()
        }

        // Get basic info from Plant.id
        var careInfo = suggestion.toCareInfo().copy(
            cycle = PlantCareDefaults.getCycle(suggestion.plant_details?.taxonomy?.family),
            watering = PlantCareDefaults.getWateringFrequency(suggestion.plant_details?.taxonomy?.family),
            indoor = PlantCareDefaults.isIndoorPlant(suggestion.plant_details?.taxonomy?.family),
            careLevel = PlantCareDefaults.getCareLevel(suggestion.plant_details?.taxonomy?.family),
            droughtTolerant = PlantCareDefaults.isDroughtTolerant(suggestion.plant_details?.taxonomy?.family)
        )

        val USE_PERENUAL_API = true
        
        if (USE_PERENUAL_API) {
            try {
                val searchName = scientificName.ifBlank { plantName }
                val perenualResponse = searchPlantCareDetails(
                    plantName = searchName,
                    apiKey = PerenualSecret.API_KEY
                )

                var perenualData = perenualResponse?.data?.firstOrNull()

                if (perenualData?.id != null) {
                    perenualData = getPlantDetailsById(
                        plantId = perenualData.id,
                        apiKey = PerenualSecret.API_KEY
                    )
                }

                if (perenualData != null) {
                    careInfo = careInfo.copy(
                        wikiDescription = perenualData.description?.takeIf { it.isNotBlank() } 
                            ?: careInfo.wikiDescription,

                        commonNames = (perenualData.common_name?.let { listOf(it) } ?: emptyList()) 
                            + careInfo.commonNames,

                        sunlight = perenualData.sunlight?.takeIf { it.isNotEmpty() } 
                            ?: careInfo.sunlight,

                        watering = perenualData.watering?.takeIf { it.isNotBlank() } 
                            ?: careInfo.watering,

                        careLevel = perenualData.care_level?.takeIf { it.isNotBlank() } 
                            ?: careInfo.careLevel,

                        growthRate = perenualData.growth_rate?.takeIf { it.isNotBlank() } 
                            ?: careInfo.growthRate,

                        maintenance = perenualData.maintenance?.takeIf { it.isNotBlank() } 
                            ?: careInfo.maintenance,

                        indoor = perenualData.indoor ?: careInfo.indoor,

                        droughtTolerant = perenualData.drought_tolerant ?: careInfo.droughtTolerant,

                        flowers = perenualData.flowers ?: careInfo.flowers,

                        propagationMethods = perenualData.propagation?.takeIf { it.isNotEmpty() } 
                            ?: careInfo.propagationMethods,

                        attracts = perenualData.attracts?.takeIf { it.isNotEmpty() } 
                            ?: careInfo.attracts,

                        poisonousToPets = (perenualData.poisonous_to_pets == 1),
                        poisonousToHumans = (perenualData.poisonous_to_humans == 1)
                    )
                }
            } catch (e: Exception) {
                println("Perenual API call failed (non-critical): ${e.message}")
            }
        }

        val watering = careInfo.wateringMaxDays
            ?: PlantCareDefaults.getWateringFrequencyDays(careInfo.family, careInfo.genus)

        val sunlightReq = careInfo.sunlight.firstOrNull()
            ?: PlantCareDefaults.getSunlightRequirement(careInfo.family, careInfo.genus)

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
