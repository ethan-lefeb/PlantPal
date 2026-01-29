package com.example.plantpal.screens.detail

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Slider
import kotlin.math.roundToInt
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
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.CustomReminder
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.avatars.com.example.plantpal.systems.avatars.AnimationType
import com.example.plantpal.com.example.plantpal.systems.avatars.com.example.plantpal.systems.avatars.AvatarAnimationController
import com.example.plantpal.com.example.plantpal.systems.avatars.com.example.plantpal.systems.avatars.rememberAvatarAnimationController
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantHealthCalculator
import com.example.plantpal.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ReminderRepository
import com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components.HealthMetricsCard
import com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components.ParticleEffect
import com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components.ParticleSystem
import com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components.fertilizeEffect
import com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components.rememberParticleSystem
import com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components.waterEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.plantpal.ui.theme.ForestGradientBalanced


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

    init {
        loadPlant()
    }

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
    val uiStateState = viewModel.uiState.collectAsState()
    val uiState = uiStateState.value

    var showEditDialog by remember { mutableStateOf(false) }
    var showHealthDialog by remember { mutableStateOf(false) }
    var showAvatarCustomization by remember { mutableStateOf(false) }
    var currentPlantForCustomization by remember { mutableStateOf<PlantProfile?>(null) }

    var showRemindersDialog by remember { mutableStateOf(false) }

    var showScrollHint by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2500)
        showScrollHint = false
    }

    // Calculate health metrics for the plant
    val healthMetrics = remember(uiState.plant) {
        uiState.plant?.let { PlantHealthCalculator.calculateHealth(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(ForestGradientBalanced)
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(uiState.plant?.commonName ?: "Plant Details")
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
                    val plantNonNull = uiState.plant!!
                    var currentPlant by remember(plantNonNull.plantId) {
                        mutableStateOf(plantNonNull)
                    }

                    LaunchedEffect(uiState.plant, uiState.actionInProgress) {
                        if (uiState.actionInProgress == null) {
                            currentPlant = uiState.plant!!
                        }
                    }

                    currentPlantForCustomization = currentPlant

                    PlantDetailContent(
                        plant = plantNonNull,
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
                        onEditReminders = { showRemindersDialog = true },
                        actionInProgress = uiState.actionInProgress,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        // Cache plant locally so Kotlin can smart-cast cleanly
        val plant = uiState.plant

        if (showEditDialog && plant != null) {
            EditPlantDialog(
                plant = plant,
                onDismiss = { showEditDialog = false },
                onSave = {
                    viewModel.updatePlant(it)
                    showEditDialog = false
                }
            )
        }

        if (showHealthDialog && plant != null) {
            HealthStatusDialog(
                currentHealth = plant.health,
                onDismiss = { showHealthDialog = false },
                onSelect = {
                    viewModel.updateHealthStatus(it)
                    showHealthDialog = false
                }
            )
        }

        if (showAvatarCustomization && currentPlantForCustomization != null) {
            GamifiedAvatarCustomizationScreen(
                plantId = currentPlantForCustomization!!.plantId,
                onNavigateBack = {
                    showAvatarCustomization = false
                    viewModel.loadPlant()
                }
            )
        }

        if (showRemindersDialog && uiState.plant != null) {
            PlantRemindersDialog(
                plant = uiState.plant!!,
                onDismiss = { showRemindersDialog = false }
            )
        }

        AnimatedVisibility(
            visible = showScrollHint && uiState.plant != null && !uiState.isLoading && uiState.error == null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Scroll to see care actions",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: String,
    padding: PaddingValues,
    onRetry: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(padding),
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
    onEditReminders: () -> Unit = {},
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

            Spacer(Modifier.height(16.dp))

            CareActionsSection(actionInProgress, handleWater, handleFertilize)

            Spacer(Modifier.height(32.dp))

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

                    LaunchedEffect(plant.careInfo) {
                        println("🌿 Plant Data:")
                        println("  careLevel: '${plant.careInfo.careLevel}'")
                        println("  growthRate: '${plant.careInfo.growthRate}'")
                        println("  maintenance: '${plant.careInfo.maintenance}'")
                        println("  droughtTolerant: ${plant.careInfo.droughtTolerant}")
                        println("  indoor: ${plant.careInfo.indoor}")
                        println("  poisonousToPets: ${plant.careInfo.poisonousToPets}")
                        println("  poisonousToHumans: ${plant.careInfo.poisonousToHumans}")
                    }

                    if (plant.careInfo.careLevel.isNotBlank()) {
                        InfoRow("Care Level", plant.careInfo.careLevel.replaceFirstChar { it.uppercase() })
                    }

                    if (plant.careInfo.growthRate.isNotBlank()) {
                        InfoRow("Growth Rate", plant.careInfo.growthRate.replaceFirstChar { it.uppercase() })
                    }

                    if (plant.careInfo.poisonousToPets) {
                        InfoRow("Pet Safe", "⚠️ No - Toxic to pets")
                    } else {
                        InfoRow("Pet Safe", "✓ Yes - Safe for pets")
                    }

                    if (plant.careInfo.indoor) {
                        InfoRow("Indoor Plant", "Yes ✓")
                    }

                    if (plant.careInfo.droughtTolerant) {
                        InfoRow("Drought Tolerant", "Yes ✓")
                    }

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

            OutlinedButton(
                onClick = onEditReminders,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Custom reminders")
            }


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
                                    modifier = Modifier
                                        .fillMaxSize(0.8f)
                                        .align(Alignment.Center)
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

@Composable
private fun PlantRemindersDialog(
    plant: PlantProfile,
    onDismiss: () -> Unit
) {
    val reminderRepo = remember { ReminderRepository() }
    val scope = rememberCoroutineScope()

    var reminders by remember { mutableStateOf<List<CustomReminder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<CustomReminder?>(null) }

    // Initial load: use existing getReminders() and filter by plantId
    LaunchedEffect(plant.plantId) {
        loading = true
        errorMessage = null
        val result = reminderRepo.getReminders()
        result
            .onSuccess { all ->
                reminders = all.filter { it.plantId == plant.plantId }
            }
            .onFailure { e ->
                errorMessage = e.message ?: "Failed to load reminders."
            }
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminders for ${plant.commonName}") },
        text = {
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Couldn't load reminders.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                reminders.isEmpty() -> {
                    Text("No custom reminders for this plant yet.")
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reminders.sortedBy { it.nextFireAt }.forEach { reminder ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        reminder.title.ifBlank { "Reminder" },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (reminder.message.isNotBlank()) {
                                        Text(
                                            reminder.message,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Next: ${formatTimestamp(reminder.nextFireAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            modifier = Modifier.weight(1f),
                                            onClick = { editingReminder = reminder }
                                        ) {
                                            Text("Edit")
                                        }
                                        OutlinedButton(
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                scope.launch {
                                                    reminderRepo.deleteReminder(reminder.id)
                                                        .onSuccess {
                                                            reminders = reminders.filter { it.id != reminder.id }
                                                        }
                                                        .onFailure { e ->
                                                            errorMessage = e.message
                                                                ?: "Failed to delete reminder."
                                                        }
                                                }
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showAddDialog = true }) {
                Text("Add reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Add dialog
    if (showAddDialog) {
        AddReminderDialog(
            plant = plant,
            onDismiss = { showAddDialog = false },
            onSave = { draft ->
                scope.launch {
                    val result = reminderRepo.createReminder(draft)
                    result
                        .onSuccess { id ->
                            val saved = draft.copy(
                                id = id,
                                userId = plant.userId
                            )
                            reminders = reminders + saved
                            showAddDialog = false
                        }
                        .onFailure { e ->
                            errorMessage = e.message ?: "Failed to save reminder."
                        }
                }
            }
        )
    }

    editingReminder?.let { reminder ->
        EditReminderDialog(
            initial = reminder,
            onDismiss = { editingReminder = null },
            onSave = { updated ->
                scope.launch {
                    reminderRepo.updateReminder(updated)
                        .onSuccess {
                            reminders = reminders.map { if (it.id == updated.id) updated else it }
                            editingReminder = null
                        }
                        .onFailure { e ->
                            errorMessage = e.message ?: "Failed to update reminder."
                        }
                }
            }
        )
    }
}


@Composable
private fun AddReminderDialog(
    plant: PlantProfile,
    onDismiss: () -> Unit,
    onSave: (CustomReminder) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }
    var repeatDays by remember { mutableStateOf(7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth()
                )

                TimePicker(
                    hour = hour,
                    minute = minute,
                    onChange = { h, m ->
                        hour = h
                        minute = m
                    }
                )

                OutlinedTextField(
                    value = repeatDays.toString(),
                    onValueChange = { value ->
                        repeatDays = value.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    },
                    label = { Text("Repeat interval (days)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val nextFire = computeNextFireAt(hour, minute)
                    val draft = CustomReminder(
                        id = "", // filled in by repo
                        userId = plant.userId,
                        plantId = plant.plantId,
                        plantName = plant.commonName,
                        title = title,
                        message = message,
                        nextFireAt = nextFire,
                        repeatIntervalDays = repeatDays
                    )
                    onSave(draft)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
private fun EditReminderDialog(
    initial: CustomReminder,
    onDismiss: () -> Unit,
    onSave: (CustomReminder) -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var message by remember { mutableStateOf(initial.message) }

    val cal = remember(initial.nextFireAt) {
        java.util.Calendar.getInstance().apply { timeInMillis = initial.nextFireAt }
    }
    var hour by remember { mutableStateOf(cal.get(java.util.Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(cal.get(java.util.Calendar.MINUTE)) }
    var repeatDays by remember { mutableStateOf(initial.repeatIntervalDays ?: 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth()
                )

                TimePicker(
                    hour = hour,
                    minute = minute,
                    onChange = { h, m ->
                        hour = h
                        minute = m
                    }
                )

                OutlinedTextField(
                    value = repeatDays.toString(),
                    onValueChange = { value ->
                        repeatDays = value.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    },
                    label = { Text("Repeat interval (days)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val newFire = computeNextFireAt(hour, minute)
                    onSave(
                        initial.copy(
                            title = title,
                            message = message,
                            nextFireAt = newFire,
                            repeatIntervalDays = repeatDays
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TimePickerRow(
    hour: Int,
    minute: Int,
    onChange: (Int, Int) -> Unit
) {
    TimePicker(
        hour = hour,
        minute = minute,
        onChange = onChange
    )
}


@Composable
private fun TimePicker(
    hour: Int,
    minute: Int,
    onChange: (Int, Int) -> Unit
) {
    // Convert current time to "minutes since midnight"
    val initialMinutes = (hour.coerceIn(0, 23) * 60) + minute.coerceIn(0, 59)

    var sliderValue by remember(hour, minute) {
        mutableStateOf(initialMinutes.toFloat())
    }
    val clamped = sliderValue.coerceIn(0f, (24 * 60 - 1).toFloat())
    val totalMinutes = clamped.roundToInt()
    val displayHour = totalMinutes / 60
    val displayMinute = totalMinutes % 60
    LaunchedEffect(displayHour, displayMinute) {
        onChange(displayHour, displayMinute)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = String.format("%02d:%02d", displayHour, displayMinute),
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = clamped,
            onValueChange = { newValue ->
                sliderValue = newValue
            },
            valueRange = 0f..(24 * 60 - 1).toFloat(),
            steps = (24 * 60 - 2)
        )

        Text(
            text = "Drag to set time of day",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


private fun computeNextFireAt(hour: Int, minute: Int): Long {
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        set(java.util.Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        set(java.util.Calendar.MINUTE, minute.coerceIn(0, 59))

        if (timeInMillis <= System.currentTimeMillis()) {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
    }
    return cal.timeInMillis
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("MMM d, yyyy h:mm a")
    return formatter.format(java.util.Date(timestamp))
}
