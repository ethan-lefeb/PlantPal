package com.example.plantpal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlantCareDetailScreen(
    plant: PlantProfile,
    onBack: () -> Unit
) {
    val plantRepository = remember { PlantRepository() }
    var currentPlant by remember { mutableStateOf(plant) }

    var isWatering by remember { mutableStateOf(false) }
    var isFertilizing by remember { mutableStateOf(false) }
    var isRotating by remember { mutableStateOf(false) }

    var showEdit by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showAvatarCustomization by remember { mutableStateOf(false) }

    var avatarCenterX by remember { mutableStateOf(0f) }
    var avatarCenterY by remember { mutableStateOf(0f) }

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
        ) {
            val pagerState = rememberPagerState(pageCount = { 2 })
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size ->
                            avatarCenterX = size.width / 2f
                            avatarCenterY = size.height / 2f
                        }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when (page) {
                                0 -> {
                                    if (currentPlant.photoUrl.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(currentPlant.photoUrl),
                                            contentDescription = currentPlant.commonName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            PlantAvatar(
                                                avatarConfig = currentPlant.avatarConfig,
                                                health = currentPlant.health,
                                                modifier = Modifier.fillMaxSize(0.8f).align(Alignment.Center),
                                                size = 200.dp,
                                                animated = true,
                                                animationController = animationController
                                            )
                                            ParticleEffect(
                                                particleSystem = particleSystem,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                                1 -> {
                                    if (currentPlant.photoUrl.isNotEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            PlantAvatar(
                                                avatarConfig = currentPlant.avatarConfig,
                                                health = currentPlant.health,
                                                modifier = Modifier.fillMaxSize(0.8f).align(Alignment.Center),
                                                size = 200.dp,
                                                animated = true,
                                                animationController = animationController
                                            )
                                            ParticleEffect(
                                                particleSystem = particleSystem,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Text(
                                            "No photo available",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(2) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(2.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = MaterialTheme.shapes.small,
                                    color = if (pagerState.currentPage == index) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                ) {}
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage == 0 && currentPlant.photoUrl.isNotEmpty()) {
                            Text(
                                text = "Photo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "◀",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Swipe",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else if (pagerState.currentPage == 1 && currentPlant.photoUrl.isNotEmpty()) {
                            Text(
                                text = "Swipe",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "▶",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Avatar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Avatar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

            OutlinedButton(
                onClick = { showAvatarCustomization = true },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Customize Avatar")
            }

            Spacer(Modifier.height(24.dp))

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
                            particleSystem.waterEffect(centerX = avatarCenterX, centerY = avatarCenterY)
                            animationController.triggerAnimation(AnimationType.WATERING, intensity = 1.0f)
                            
                            val result = plantRepository.waterPlant(currentPlant.plantId)
                            if (result.isSuccess) {
                                currentPlant = currentPlant.copy(lastWatered = System.currentTimeMillis())
                                delay(500)
                                animationController.triggerAnimation(AnimationType.HAPPY, intensity = 1.0f)
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
                        Text("ðŸ’§ Water")
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isFertilizing = true
                            particleSystem.fertilizeEffect(centerX = avatarCenterX, centerY = avatarCenterY)
                            animationController.triggerAnimation(AnimationType.FERTILIZING, intensity = 1.0f)
                            
                            val result = plantRepository.fertilizePlant(currentPlant.plantId)
                            if (result.isSuccess) {
                                currentPlant = currentPlant.copy(lastFertilized = System.currentTimeMillis())
                                delay(500)
                                animationController.triggerAnimation(AnimationType.HAPPY, intensity = 1.0f)
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
                        Text("ðŸŒ¿ Fertilize")
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isRotating = true
                        animationController.triggerAnimation(AnimationType.GROWING, intensity = 1.0f)
                        
                        val updated = currentPlant.copy(
                            careProfile = currentPlant.careProfile.copy(
                                lastRotated = System.currentTimeMillis()
                            )
                        )
                        val result = plantRepository.updatePlant(updated)
                        if (result.isSuccess) {
                            currentPlant = updated
                            delay(500)
                            animationController.triggerAnimation(AnimationType.HAPPY, intensity = 1.0f)
                        }
                        isRotating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRotating
            ) {
                if (isRotating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("ðŸ”„ Rotate Plant")
                }
            }

            Spacer(Modifier.height(32.dp))
            }
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
            plant = currentPlant,
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
        if (ts <= 0L) "ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â"
        else SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ts))

    val rotationEvery = p.careProfile.rotationFrequency
    val lastRotatedTs = p.careProfile.lastRotated

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                CareRow("ÃƒÂ°Ã…Â¸Ã¢â‚¬â„¢Ã‚Â§ Watered", tsLabel(p.lastWatered), daysSince(p.lastWatered))
                CareRow("ÃƒÂ°Ã…Â¸Ã…â€™Ã‚Â¿ Fertilized", tsLabel(p.lastFertilized), daysSince(p.lastFertilized))
                CareRow("ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ¢â‚¬Å¾ Rotated", tsLabel(lastRotatedTs), daysSince(lastRotatedTs))
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
        val right = if (dateLabel == "ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â") "ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â"
        else "$dateLabel  Ãƒâ€šÃ‚Â·  ${if (daysSince == 0) "today" else "$daysSince d ago"}"
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
