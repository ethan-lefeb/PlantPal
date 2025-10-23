package com.example.plantpal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPlant.commonName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = currentPlant.scientificName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (currentPlant.careInfo.family.isNotEmpty() || currentPlant.careInfo.genus.isNotEmpty()) {
                Text(
                    text = buildString {
                        if (currentPlant.careInfo.family.isNotEmpty()) append("Family: ${currentPlant.careInfo.family}")
                        if (currentPlant.careInfo.genus.isNotEmpty()) {
                            if (isNotEmpty()) append(" • ")
                            append("Genus: ${currentPlant.careInfo.genus}")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentPlant.confidence > 0.0) {
                    Chip(
                        text = "ID: ${(currentPlant.confidence * 100).toInt()}%",
                        icon = "✓"
                    )
                }
                if (currentPlant.careInfo.cycle.isNotEmpty()) {
                    Chip(
                        text = currentPlant.careInfo.cycle.capitalize(),
                        icon = "🔄"
                    )
                }
                if (currentPlant.careInfo.indoor) {
                    Chip(
                        text = "Indoor",
                        icon = "🏠"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Care Guide",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    QuickCareRow(
                        icon = "💧",
                        label = "Watering",
                        value = currentPlant.careInfo.watering.ifEmpty { "Regular" }.capitalize()
                    )

                    QuickCareRow(
                        icon = "☀️",
                        label = "Sunlight",
                        value = if (currentPlant.careInfo.sunlight.isNotEmpty()) {
                            currentPlant.careInfo.sunlight.joinToString(", ") {
                                it.replace("_", " ").capitalize()
                            }
                        } else {
                            currentPlant.sunlight
                        }
                    )

                    if (currentPlant.careInfo.careLevel.isNotEmpty()) {
                        QuickCareRow(
                            icon = "📊",
                            label = "Care Level",
                            value = currentPlant.careInfo.careLevel.capitalize()
                        )
                    }

                    if (currentPlant.careInfo.growthRate.isNotEmpty()) {
                        QuickCareRow(
                            icon = "🌱",
                            label = "Growth Rate",
                            value = currentPlant.careInfo.growthRate.capitalize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentPlant.careInfo.commonNames.isNotEmpty()) {
                CareInfoSection(
                    title = "Common Names",
                    content = currentPlant.careInfo.commonNames.joinToString(", ")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            CareInfoSection(
                title = "💧 Watering Schedule",
                content = buildString {
                    if (currentPlant.careInfo.wateringMinDays != null && currentPlant.careInfo.wateringMaxDays != null) {
                        append("Water every ${currentPlant.careInfo.wateringMinDays}-${currentPlant.careInfo.wateringMaxDays} days\n\n")
                    } else {
                        append("Water every ${currentPlant.wateringFrequency} days\n\n")
                    }

                    if (currentPlant.careInfo.watering.isNotEmpty()) {
                        append("Frequency: ${currentPlant.careInfo.watering.capitalize()}\n\n")
                    }

                    append("ℹ️ ${PlantCareDefaults.getCareTips(currentPlant.careInfo.family)}")

                    if (currentPlant.careInfo.droughtTolerant) {
                        append("\n\n🌵 This plant is drought tolerant")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            CareInfoSection(
                title = "☀️ Sunlight Requirements",
                content = buildString {
                    if (currentPlant.careInfo.sunlight.isNotEmpty()) {
                        append("Prefers: ${currentPlant.careInfo.sunlight.joinToString(", ") {
                            it.replace("_", " ").capitalize()
                        }}")
                    } else if (currentPlant.sunlight.isNotEmpty() && currentPlant.sunlight != "Unknown") {
                        append(currentPlant.sunlight)
                    } else {
                        append("Bright indirect light (typical houseplant)")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (currentPlant.careInfo.propagationMethods.isNotEmpty()) {
                CareInfoSection(
                    title = "🌱 Propagation Methods",
                    content = currentPlant.careInfo.propagationMethods.joinToString(", ") { it.capitalize() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val specialChars = mutableListOf<String>()
            if (currentPlant.careInfo.flowers) specialChars.add("🌸 Produces flowers")
            if (currentPlant.careInfo.flowerColor.isNotEmpty()) specialChars.add("Flower color: ${currentPlant.careInfo.flowerColor}")
            if (currentPlant.careInfo.attracts.isNotEmpty()) {
                specialChars.add("Attracts: ${currentPlant.careInfo.attracts.joinToString(", ")}")
            }
            if (currentPlant.careInfo.harvestSeason.isNotEmpty()) {
                specialChars.add("Harvest: ${currentPlant.careInfo.harvestSeason}")
            }

            if (specialChars.isNotEmpty()) {
                CareInfoSection(
                    title = "✨ Special Characteristics",
                    content = specialChars.joinToString("\n")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (currentPlant.careInfo.poisonousToHumans || currentPlant.careInfo.poisonousToPets) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚠️ Safety Warning",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentPlant.careInfo.poisonousToHumans) {
                            Text(
                                text = "• Toxic to humans",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        if (currentPlant.careInfo.poisonousToPets) {
                            Text(
                                text = "• Toxic to pets",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (currentPlant.careInfo.wikiDescription.isNotEmpty()) {
                CareInfoSection(
                    title = "📖 Description",
                    content = currentPlant.careInfo.wikiDescription
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (currentPlant.notes.isNotEmpty()) {
                CareInfoSection(
                    title = "📝 Your Notes",
                    content = currentPlant.notes
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Care History",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentPlant.lastWatered > 0L) {
                        Text(
                            text = "💧 Last watered: ${formatTimestamp(currentPlant.lastWatered)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "💧 Not watered yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentPlant.lastFertilized > 0L) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🌿 Last fertilized: ${formatTimestamp(currentPlant.lastFertilized)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isWatering = true
                            val result = plantRepository.waterPlant(currentPlant.plantId)
                            result.onSuccess {
                                // Update the local plant state
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
                            result.onSuccess {
                                // Update the local plant state
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun QuickCareRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text(text = icon, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun Chip(text: String, icon: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodySmall)
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CareInfoSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        days < 30 -> "${days / 7} weeks ago"
        else -> "${days / 30} months ago"
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}