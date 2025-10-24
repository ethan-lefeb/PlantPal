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
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                .padding(16.dp)
        ) {
            // --- Plant Image ---
            if (currentPlant.photoUrl.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(currentPlant.photoUrl),
                        contentDescription = currentPlant.commonName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

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
            Spacer(Modifier.height(12.dp))

            Text("Health: ${currentPlant.health}")
            Text("Sunlight: ${currentPlant.sunlight}")
            Text("Water every ${currentPlant.wateringFrequency} days")
            Text("Fertilize every ${currentPlant.fertilizerFrequency} days")

            if (currentPlant.notes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Notes: ${currentPlant.notes}")
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
