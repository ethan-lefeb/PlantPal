package com.example.plantpal.screens.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.plantpal.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlantDetailUiState(
    val plant: PlantProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionInProgress: String? = null
)

class PlantDetailViewModel(private val plantId: String) : ViewModel() {

    private val repository = PlantRepository()

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    init { loadPlant() }

    fun loadPlant() {
        viewModelScope.launch {
            _uiState.value = PlantDetailUiState(isLoading = true)
            repository.getPlant(plantId)
                .onSuccess { plant ->
                    _uiState.value = PlantDetailUiState(plant = plant, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = PlantDetailUiState(error = e.message, isLoading = false)
                }
        }
    }

    fun waterPlant() = perform("watering") { repository.waterPlant(plantId) }
    fun fertilizePlant() = perform("fertilizing") { repository.fertilizePlant(plantId) }
    fun updateHealthStatus(health: String) = perform("updating") {
        repository.updateHealthStatus(plantId, health)
    }
    fun updatePlant(updated: PlantProfile) =
        perform("saving") { repository.updatePlant(updated) }

    private fun perform(type: String, action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = type)
            action()
                .onSuccess { loadPlant() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message,
                        actionInProgress = null
                    )
                }
        }
    }
}


@Composable
fun PlantDetailScreenWrapper(
    plantId: String,
    onBack: () -> Unit
) {
    val viewModel: PlantDetailViewModel = viewModel(
        modelClass = PlantDetailViewModel::class.java,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PlantDetailViewModel(plantId) as T
        }
    )

    PlantDetailScreen(
        plantId = plantId,
        onNavigateBack = onBack,
        viewModel = viewModel
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PlantDetailScreen(
    plantId: String,
    onNavigateBack: () -> Unit,
    viewModel: PlantDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showHealthDialog by remember { mutableStateOf(false) }
    var showAvatarCustomization by remember { mutableStateOf(false) }
    var currentPlantForCustomization by remember { mutableStateOf<PlantProfile?>(null) }
    
    // Calculate health metrics for the plant
    val healthMetrics = remember(uiState.plant) {
        uiState.plant?.let { PlantHealthCalculator.calculateHealth(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFFB5E48C), Color(0xFFD9ED92), Color(0xFF99D98C))
            )
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(uiState.plant?.commonName ?: "Plant Details")
                            // Add health subtitle
                            if (healthMetrics != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        PlantHealthCalculator.getHealthEmoji(healthMetrics.healthStatus),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        "${(healthMetrics.overallHealth * 100).toInt()}% Health",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PlantHealthCalculator.getHealthColor(healthMetrics)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showEditDialog = true },
                            enabled = uiState.plant != null
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->

            when {
                uiState.isLoading -> LoadingState(padding)
                uiState.error != null -> ErrorState(uiState.error!!, padding) {
                    viewModel.loadPlant()
                }
                uiState.plant != null -> {
                    var currentPlant by remember(uiState.plant!!.plantId) { 
                        mutableStateOf(uiState.plant!!) 
                    }

                    LaunchedEffect(uiState.plant, uiState.actionInProgress) {
                        if (uiState.actionInProgress == null) {
                            currentPlant = uiState.plant!!
                        }
                    }

                    currentPlantForCustomization = currentPlant
                    
                    PlantDetailContent(
                        plant = currentPlant,
                        healthMetrics = healthMetrics,
                        onWaterPlant = {
                            currentPlant = currentPlant.copy(
                                lastWatered = System.currentTimeMillis()
                            )
                            viewModel.waterPlant()
                        },
                        onFertilizePlant = {
                            currentPlant = currentPlant.copy(
                                lastFertilized = System.currentTimeMillis()
                            )
                            viewModel.fertilizePlant()
                        },
                        onUpdateHealth = { showHealthDialog = true },
                        onCustomizeAvatar = { showAvatarCustomization = true },
                        actionInProgress = uiState.actionInProgress,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        if (showEditDialog && uiState.plant != null) {
            EditPlantDialog(
                plant = uiState.plant!!,
                onDismiss = { showEditDialog = false },
                onSave = {
                    viewModel.updatePlant(it)
                    showEditDialog = false
                }
            )
        }

        if (showHealthDialog) {
            HealthStatusDialog(
                currentHealth = uiState.plant?.health ?: "healthy",
                onDismiss = { showHealthDialog = false },
                onSelect = {
                    viewModel.updateHealthStatus(it)
                    showHealthDialog = false
                }
            )
        }

        if (showAvatarCustomization && currentPlantForCustomization != null) {
            AvatarCustomizationScreen(
                currentConfig = currentPlantForCustomization!!.avatarConfig,
                plantName = currentPlantForCustomization!!.commonName,
                plant = currentPlantForCustomization,
                onSave = { newConfig ->
                    val updatedPlant = currentPlantForCustomization!!.copy(avatarConfig = newConfig)
                    viewModel.updatePlant(updatedPlant)
                    showAvatarCustomization = false
                },
                onBack = { showAvatarCustomization = false }
            )
        }
    }
}


@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
private fun ErrorState(
    error: String,
    padding: PaddingValues,
    onRetry: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Error loading plant", style = MaterialTheme.typography.titleMedium)
            Text(error, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlantDetailContent(
    plant: PlantProfile,
    healthMetrics: PlantHealthCalculator.HealthMetrics?,
    onWaterPlant: () -> Unit,
    onFertilizePlant: () -> Unit,
    onUpdateHealth: () -> Unit,
    onCustomizeAvatar: () -> Unit = {},
    actionInProgress: String?,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        pageCount = { 2 },
        initialPage = 0
    )

    var avatarCenterX by remember { mutableStateOf(0f) }
    var avatarCenterY by remember { mutableStateOf(0f) }

    val animationController = rememberAvatarAnimationController(
        health = plant.health,
        daysSinceWatering = ((System.currentTimeMillis() - plant.lastWatered) / (1000 * 60 * 60 * 24)).toInt()
    )
    val particleSystem = rememberParticleSystem()
    val scope = rememberCoroutineScope()
    
    val handleWater: () -> Unit = {
        scope.launch {
            if (pagerState.currentPage != 0) {
                pagerState.animateScrollToPage(0)
                delay(200)
            }

            particleSystem.waterEffect(centerX = avatarCenterX, centerY = avatarCenterY)
            animationController.triggerAnimation(AnimationType.WATERING, intensity = 1.0f)
            delay(2000)
            onWaterPlant()
        }
        Unit
    }
    
    val handleFertilize: () -> Unit = {
        scope.launch {
            if (pagerState.currentPage != 0) {
                pagerState.animateScrollToPage(0)
                delay(200)
            }

            particleSystem.fertilizeEffect(centerX = avatarCenterX, centerY = avatarCenterY)
            animationController.triggerAnimation(AnimationType.FERTILIZING, intensity = 1.0f)
            animationController.triggerAnimation(AnimationType.HAPPY, intensity = 1.0f)
            delay(2000)
            onFertilizePlant()
        }
        Unit
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        PlantImageWithAvatar(
            plant = plant,
            pagerState = pagerState,
            animationController = animationController,
            particleSystem = particleSystem,
            onAvatarPositioned = { x, y ->
                avatarCenterX = x
                avatarCenterY = y
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onCustomizeAvatar,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("🎨 Customize Avatar")
            }

            if (healthMetrics != null) {
                HealthMetricsCard(
                    plant = plant,
                    metrics = healthMetrics
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        plant.commonName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        plant.scientificName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (plant.confidence > 0.0) {
                        Text(
                            "Identification Confidence: ${(plant.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    InfoRow("Sunlight", plant.sunlight)
                    InfoRow("Water Frequency", "Every ${plant.wateringFrequency} days")
                    InfoRow("Fertilize Frequency", "Every ${plant.fertilizerFrequency} days")
                    
                    if (plant.notes.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            plant.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            HealthStatusSection(plant, onUpdateHealth)

            Spacer(Modifier.height(16.dp))

            CareActionsSection(actionInProgress, handleWater, handleFertilize)

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlantImageWithAvatar(
    plant: PlantProfile,
    pagerState: PagerState,
    animationController: AvatarAnimationController,
    particleSystem: ParticleSystem,
    onAvatarPositioned: (Float, Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    onAvatarPositioned(size.width / 2f, size.height / 2f)
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                PlantAvatar(
                                    avatarConfig = plant.avatarConfig,
                                    health = plant.health,
                                    size = 200.dp,
                                    animated = true,
                                    animationController = animationController,
                                    modifier = Modifier.fillMaxSize(0.8f).align(Alignment.Center)
                                )
                                ParticleEffect(
                                    particleSystem = particleSystem,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        1 -> {
                            if (plant.photoUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(plant.photoUrl),
                                    contentDescription = plant.commonName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
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
                if (pagerState.currentPage == 0) {
                    Text(
                        text = "Avatar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (plant.photoUrl.isNotEmpty()) {
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
                    }
                } else if (pagerState.currentPage == 1 && plant.photoUrl.isNotEmpty()) {
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
                        text = "Photo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HealthStatusSection(
    plant: PlantProfile,
    onUpdateHealth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Health Status", style = MaterialTheme.typography.titleMedium)
            Text(
                plant.health.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                color = when (plant.health) {
                    "healthy" -> MaterialTheme.colorScheme.primary
                    "warning" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }
        TextButton(onClick = onUpdateHealth) { Text("Update") }
    }
}

@Composable
private fun CareActionsSection(
    actionInProgress: String?,
    onWaterPlant: () -> Unit,
    onFertilizePlant: () -> Unit
) {
    Column {
        Text("Care Actions", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onWaterPlant,
                enabled = actionInProgress == null,
                modifier = Modifier.weight(1f)
            ) {
                if (actionInProgress == "watering") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("💧")
                }
                Spacer(Modifier.width(8.dp))
                Text("Water")
            }

            Button(
                onClick = onFertilizePlant,
                enabled = actionInProgress == null,
                modifier = Modifier.weight(1f)
            ) {
                if (actionInProgress == "fertilizing") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("🌱")
                }
                Spacer(Modifier.width(8.dp))
                Text("Fertilize")
            }
        }
    }
}

@Composable
fun EditPlantDialog(
    plant: PlantProfile,
    onDismiss: () -> Unit,
    onSave: (PlantProfile) -> Unit
) {
    var commonName by remember { mutableStateOf(plant.commonName) }
    var scientificName by remember { mutableStateOf(plant.scientificName) }
    var notes by remember { mutableStateOf(plant.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Plant") },
        text = {
            Column {
                OutlinedTextField(
                    value = commonName,
                    onValueChange = { commonName = it },
                    label = { Text("Common Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = scientificName,
                    onValueChange = { scientificName = it },
                    label = { Text("Scientific Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        plant.copy(
                            commonName = commonName.trim(),
                            scientificName = scientificName.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                enabled = commonName.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
fun HealthStatusDialog(
    currentHealth: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val healthOptions = listOf("healthy", "warning", "critical")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Health Status") },
        text = {
            Column {
                healthOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = currentHealth == option,
                            onClick = { onSelect(option) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
