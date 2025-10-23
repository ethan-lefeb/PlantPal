package com.example.plantpal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@Composable
fun PlantDetailScreen(
    plantId: String,
    userId: String,
    viewModel: PlantDetailViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var plant by remember { mutableStateOf<PlantProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(plantId) {
        scope.launch {
            val result = viewModel.getPlant(userId, plantId)
            result.onSuccess { plant = it }
                .onFailure { error = it.message }
            isLoading = false
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        }

        plant != null -> {
            PlantProfileView(
                plant = plant!!,
                isEditing = isEditing,
                onEditToggle = { isEditing = !isEditing },
                onPlantChange = { plant = it },
                onSave = { updatedPlant ->
                    scope.launch {
                        val saveResult = viewModel.updatePlant(updatedPlant)
                        saveResult.onSuccess {
                            plant = updatedPlant
                            isEditing = false
                        }.onFailure { error = it.message }
                    }
                },
                onBack = onBack
            )
        }
    }
}

@Composable
fun PlantProfileView(
    plant: PlantProfile,
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    onPlantChange: (PlantProfile) -> Unit,
    onSave: (PlantProfile) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("← Back") // Simple back button
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onEditToggle) {
                Icon(Icons.Default.Edit, contentDescription = if (isEditing) "Cancel Edit" else "Edit")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (plant.photoUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(plant.photoUrl),
                contentDescription = plant.commonName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
        }

        if (isEditing) {
            OutlinedTextField(
                value = plant.commonName,
                onValueChange = { onPlantChange(plant.copy(commonName = it)) },
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = plant.scientificName,
                onValueChange = { onPlantChange(plant.copy(scientificName = it)) },
                label = { Text("Scientific Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = plant.notes,
                onValueChange = { onPlantChange(plant.copy(notes = it)) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = { onSave(plant) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Changes")
            }
        } else {
            Text(plant.commonName, style = MaterialTheme.typography.headlineSmall)
            Text(plant.scientificName, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Text("Health: ${plant.health}")
            Text("Sunlight: ${plant.sunlight}")
            Text("Water every ${plant.wateringFrequency} days")
            Text("Fertilize every ${plant.fertilizerFrequency} days")
            if (plant.notes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Notes: ${plant.notes}")
            }
        }
    }
}
