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
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantCareDetailScreen(
    plant: PlantProfile,
    onBack: () -> Unit
) {
    val plantRepository = remember { PlantRepository() }
    var currentPlant by remember { mutableStateOf(plant) }

    // loading flags for care actions
    var isWatering by remember { mutableStateOf(false) }
    var isFertilizing by remember { mutableStateOf(false) }
    var isRotating by remember { mutableStateOf(false) }

    // ui state
    var showEdit by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var avatarCardSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var showAvatarCustomization by remember { mutableStateOf(false) }

    val animationController = rememberAvatarAnimationController(
        health = currentPlant.health,
        daysSinceWatering = ((System.currentTimeMillis() - currentPlant.lastWatered) / (1000 * 60 * 60 * 24)).toInt()
    )

    val particleSystem = rememberParticleSystem()

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
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showAvatarCustomization = true },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("\uD83C\uDFA8 Customize Avatar")
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
private fun SmallBusyDot(colorOn: Boolean = true) {
    CircularProgressIndicator(
        modifier = Modifier.size(16.dp),
        color = if (colorOn) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp
    )
}

@Composable
private fun CareSummary(p: PlantProfile) {
    // helpers
    fun daysSince(ts: Long): Int {
        if (ts <= 0L) return Int.MAX_VALUE
        val days = (System.currentTimeMillis() - ts) / (1000L * 60 * 60 * 24)
        return max(0, days.toInt())
    }
    fun tsLabel(ts: Long): String =
        if (ts <= 0L) "—"
        else SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ts))

    val rotationEvery = p.careProfile.rotationFrequency
    val lastRotatedTs = p.careProfile.lastRotated

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // frequencies
        Text(
            "Water every ${p.wateringFrequency} day${if (p.wateringFrequency == 1) "" else "s"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Fertilize every ${p.fertilizerFrequency} day${if (p.fertilizerFrequency == 1) "" else "s"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Rotate every $rotationEvery day${if (rotationEvery == 1) "" else "s"}",
            style = MaterialTheme.typography.bodyMedium
        )

        // Last care timestamps
        Spacer(Modifier.height(4.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Last care", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                CareRow("💧 Watered", tsLabel(p.lastWatered), daysSince(p.lastWatered))
                CareRow("🌿 Fertilized", tsLabel(p.lastFertilized), daysSince(p.lastFertilized))
                CareRow("🔄 Rotated", tsLabel(lastRotatedTs), daysSince(lastRotatedTs))
            }
        }
    }
}

@Composable
private fun CareRow(label: String, dateLabel: String, daysSince: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        val right = if (dateLabel == "—") "—"
        else "$dateLabel  ·  ${if (daysSince == 0) "today" else "$daysSince d ago"}"
        Text(right, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
