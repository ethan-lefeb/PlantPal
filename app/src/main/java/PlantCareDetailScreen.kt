package com.example.plantpal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantCareDetailScreen(
    plant: PlantProfile,
    onBack: () -> Unit
) {
    val plantRepository = remember { PlantRepository() }
    var currentPlant by remember { mutableStateOf(plant) }
    var isWatering by remember { mutableStateOf(false) }
    var isFertilizing by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showAvatarCustomization by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentPlant.health, currentPlant.lastWatered) {
        val updatedConfig = AvatarGenerator.updateAvatarForPlantState(
            currentConfig = currentPlant.avatarConfig,
            health = currentPlant.health,
            lastWatered = currentPlant.lastWatered,
            wateringFrequency = currentPlant.wateringFrequency
        )
        if (updatedConfig != currentPlant.avatarConfig) {
            currentPlant = currentPlant.copy(avatarConfig = updatedConfig)
            plantRepository.updatePlant(currentPlant)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPlant.commonName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Plant")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .size(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PlantAvatar(
                        avatarConfig = currentPlant.avatarConfig,
                        health = currentPlant.health,
                        size = 180.dp,
                        animated = true
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showAvatarCustomization = true },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("🎨 Customize Avatar")
            }
            
            Spacer(Modifier.height(24.dp))

            // --- Plant Basic Info ---
            Text(
                text = currentPlant.commonName,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = currentPlant.scientificName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (currentPlant.health) {
                        "healthy" -> MaterialTheme.colorScheme.primaryContainer
                        "warning" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Health Status",
                        style = MaterialTheme.typography.titleSmall
                    )
                    val healthEmoji = when (currentPlant.health) {
                        "healthy" -> "😊"
                        "warning" -> "😐"
                        "critical" -> "😢"
                        else -> "🌱"
                    }
                    Text(
                        text = "$healthEmoji ${currentPlant.health.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Care Information ---
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Care Information",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("☀️ Sunlight:")
                        Text(currentPlant.sunlight)
                    }
                    Spacer(Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💧 Watering:")
                        Text("Every ${currentPlant.wateringFrequency} days")
                    }
                    Spacer(Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🌿 Fertilizer:")
                        Text("Every ${currentPlant.fertilizerFrequency} days")
                    }
                    
                    if (currentPlant.careInfo.careLevel.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📊 Care Level:")
                            Text(currentPlant.careInfo.careLevel.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }

            if (currentPlant.notes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(currentPlant.notes)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- Care Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isWatering = true
                            val result = plantRepository.waterPlant(currentPlant.plantId)
                            if (result.isSuccess) {
                                currentPlant = currentPlant.copy(lastWatered = System.currentTimeMillis())
                            }
                            isWatering = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isWatering
                ) {
                    if (isWatering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("💧 Water")
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isFertilizing = true
                            val result = plantRepository.fertilizePlant(currentPlant.plantId)
                            if (result.isSuccess) {
                                currentPlant = currentPlant.copy(lastFertilized = System.currentTimeMillis())
                            }
                            isFertilizing = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isFertilizing
                ) {
                    if (isFertilizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("🌿 Fertilize")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentPlant.lastWatered > 0) {
                    val daysSinceWatered = (System.currentTimeMillis() - currentPlant.lastWatered) / (1000 * 60 * 60 * 24)
                    Text(
                        text = "Last watered: $daysSinceWatered days ago",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (currentPlant.lastFertilized > 0) {
                    val daysSinceFertilized = (System.currentTimeMillis() - currentPlant.lastFertilized) / (1000 * 60 * 60 * 24)
                    Text(
                        text = "Last fertilized: $daysSinceFertilized days ago",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // --- Edit Dialog ---
    if (showEdit) {
        EditPlantDialog(
            plant = currentPlant,
            saving = saving,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                scope.launch {
                    saving = true
                    val result = plantRepository.updatePlant(updated)
                    saving = false
                    if (result.isSuccess) {
                        currentPlant = updated
                        showEdit = false
                    } else {
                        println("Failed to update plant: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        )
    }
    
    // --- Avatar Customization Dialog ---
    if (showAvatarCustomization) {
        AvatarCustomizationScreen(
            currentConfig = currentPlant.avatarConfig,
            plantName = currentPlant.commonName,
            onSave = { newConfig ->
                scope.launch {
                    saving = true
                    val updated = currentPlant.copy(avatarConfig = newConfig)
                    val result = plantRepository.updatePlant(updated)
                    saving = false
                    if (result.isSuccess) {
                        currentPlant = updated
                        showAvatarCustomization = false
                    }
                }
            },
            onBack = { showAvatarCustomization = false }
        )
    }
}

@Composable
private fun EditPlantDialog(
    plant: PlantProfile,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (PlantProfile) -> Unit
) {
    var commonName by remember { mutableStateOf(plant.commonName) }
    var scientificName by remember { mutableStateOf(plant.scientificName) }
    var notes by remember { mutableStateOf(plant.notes) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Edit Plant") },
        text = {
            Column {
                OutlinedTextField(
                    value = commonName,
                    onValueChange = { commonName = it },
                    label = { Text("Common Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = scientificName,
                    onValueChange = { scientificName = it },
                    label = { Text("Scientific Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    enabled = !saving
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && commonName.isNotBlank(),
                onClick = {
                    onSave(
                        plant.copy(
                            commonName = commonName.trim(),
                            scientificName = scientificName.trim(),
                            notes = notes.trim()
                        )
                    )
                }
            ) {
                if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Save")
            }
        },
        dismissButton = {
            TextButton(enabled = !saving, onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
