package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlantsUiState(
    val plants: List<PlantProfile> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class PlantsViewModel : ViewModel() {
    private val repository = PlantRepository()
    private val _uiState = MutableStateFlow(PlantsUiState())
    val uiState: StateFlow<PlantsUiState> = _uiState.asStateFlow()

    init { loadPlants() }

    fun loadPlants() {
        viewModelScope.launch {
            _uiState.value = PlantsUiState(isLoading = true)
            repository.getAllPlants()
                .onSuccess { _uiState.value = PlantsUiState(it, isLoading = false) }
                .onFailure { _uiState.value = PlantsUiState(error = it.message, isLoading = false) }
        }
    }

    fun deletePlant(plant: PlantProfile) {
        viewModelScope.launch {
            repository.deletePlant(plant.plantId)
                .onSuccess { loadPlants() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsHomeScreen(
    viewModel: PlantsViewModel = viewModel(),
    onPlantClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Plants") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                uiState.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading plants", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadPlants() }) { Text("Retry") }
                    }
                }

                uiState.plants.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            "Your plants will appear here 🌱",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap the + button to add your first plant!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // Calculate health metrics for all plants
                    val plantsWithHealth = uiState.plants.map { plant ->
                        plant to PlantHealthCalculator.calculateHealth(plant)
                    }.sortedByDescending { (_, metrics) -> 
                        // Sort by urgency first, then by health
                        PlantHealthCalculator.getCareUrgency(metrics) * 1000 + (1 - metrics.overallHealth)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            LibrarySummaryCard(plantsWithHealth)
                        }

                        items(plantsWithHealth) { (plant, metrics) ->
                            EnhancedPlantCard(
                                plant = plant,
                                metrics = metrics,
                                onDelete = { viewModel.deletePlant(it) },
                                onClick = { onPlantClick(plant.plantId) }
                            )
                        }
                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySummaryCard(plantsWithHealth: List<Pair<PlantProfile, PlantHealthCalculator.HealthMetrics>>) {
    val total = plantsWithHealth.size
    val needingAttention = plantsWithHealth.count { (_, metrics) ->
        PlantHealthCalculator.needsAttention(metrics.healthStatus, metrics.overallHealth)
    }
    val avgHealth = if (total > 0) {
        plantsWithHealth.map { it.second.overallHealth }.average().toFloat()
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$total",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Total Plants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${(avgHealth * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        avgHealth >= 0.75f -> Color(0xFF4CAF50)
                        avgHealth >= 0.45f -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
                Text(
                    "Avg Health",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (needingAttention > 0) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        "$needingAttention",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (needingAttention > 0) Color(0xFFFFC107) else Color(0xFF4CAF50)
                    )
                }
                Text(
                    "Need Care",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EnhancedPlantCard(
    plant: PlantProfile,
    metrics: PlantHealthCalculator.HealthMetrics,
    onDelete: (PlantProfile) -> Unit,
    onClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val healthPercentage = (metrics.overallHealth * 100).toInt()
    val urgency = PlantHealthCalculator.getCareUrgency(metrics)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                urgency >= 3 -> Color(0xFFFFF3E0)
                urgency >= 2 -> Color(0xFFFFF9E6)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (urgency >= 2) 6.dp else 3.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    PlantAvatar(
                        avatarConfig = plant.avatarConfig,
                        health = metrics.healthStatus,
                        size = 80.dp,
                        animated = false,
                        modifier = Modifier
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp),
                        shape = CircleShape,
                        color = PlantHealthCalculator.getHealthColor(metrics),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "$healthPercentage%",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            plant.commonName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (urgency >= 2) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Needs attention",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Text(
                        plant.scientificName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PlantHealthCalculator.getHealthColor(metrics))
                        )
                        
                        Text(
                            metrics.healthStatus.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = PlantHealthCalculator.getHealthColor(metrics),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (urgency >= 1) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            PlantHealthCalculator.getRecommendedAction(metrics, plant),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete plant",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (urgency >= 1) {
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricChip(
                        label = "Water",
                        value = if (metrics.daysUntilWaterNeeded == 0) "Today" 
                                else "${metrics.daysUntilWaterNeeded}d",
                        level = metrics.hydrationLevel,
                        icon = "💧"
                    )
                    MetricChip(
                        label = "Food",
                        value = if (metrics.daysUntilFertilizerNeeded == 0) "Today"
                                else "${metrics.daysUntilFertilizerNeeded}d",
                        level = metrics.nutritionLevel,
                        icon = "🌿"
                    )
                    MetricChip(
                        label = "Care",
                        value = "${(metrics.careConsistency * 100).toInt()}%",
                        level = metrics.careConsistency,
                        icon = "📊"
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant?") },
            text = { 
                Text("Are you sure you want to delete ${plant.commonName}? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(plant)
                }) { 
                    Text("Delete", color = MaterialTheme.colorScheme.error) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { 
                    Text("Cancel") 
                }
            }
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    level: Float,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = when {
                level >= 0.7f -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                level >= 0.4f -> Color(0xFFFFC107).copy(alpha = 0.15f)
                else -> Color(0xFFF44336).copy(alpha = 0.15f)
            }
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        level >= 0.7f -> Color(0xFF4CAF50)
                        level >= 0.4f -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
private fun PlantHealthCalculator.needsAttention(status: String, health: Float): Boolean {
    return health < 0.6f || status == "critical"
}
