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
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantHealthCalculator
import com.example.plantpal.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.ui.theme.LocalUIScale
import com.example.plantpal.ui.theme.ScaledSizes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val BlossomLight = Color(0xFFF7D6E4)   // lighter blush blossom
private val BlossomOnLight = Color(0xFF6A3347)
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
    val scaled = LocalUIScale.current  // GET SCALED VALUES
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Plants",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = scaled.titleLarge
                        )
                    )
                },
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
                        Text(
                            "Error loading plants",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = scaled.bodyLarge
                        )
                        Spacer(Modifier.height(scaled.spacingMedium))
                        Button(
                            onClick = { viewModel.loadPlants() },
                            modifier = Modifier.height(scaled.buttonHeight)
                        ) {
                            Text("Retry", fontSize = scaled.labelLarge)
                        }
                    }
                }

                uiState.plants.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(scaled.paddingLarge)
                    ) {
                        Text(
                            "Your plants will appear here 🌱",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = scaled.titleLarge
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(scaled.spacingSmall))
                        Text(
                            "Tap the + button to add your first plant!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = scaled.bodyLarge
                            ),
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
                        PlantHealthCalculator.getCareUrgency(metrics) * 1000 + (1 - metrics.overallHealth)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(scaled.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(scaled.spacingMedium)
                    ) {
                        item {
                            LibrarySummaryCard(plantsWithHealth, scaled)
                        }

                        items(plantsWithHealth) { (plant, metrics) ->
                            EnhancedPlantCard(
                                plant = plant,
                                metrics = metrics,
                                scaled = scaled,
                                onDelete = { viewModel.deletePlant(it) },
                                onClick = { onPlantClick(plant.plantId) }
                            )
                        }
                        item {
                            Spacer(Modifier.height(scaled.spacingXLarge * 2))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySummaryCard(
    plantsWithHealth: List<Pair<PlantProfile, PlantHealthCalculator.HealthMetrics>>,
    scaled: ScaledSizes
) {
    val total = plantsWithHealth.size
    val needingAttention = plantsWithHealth.count { (plant, _) ->
        PlantHealthCalculator.needsAttention(plant)
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
                .padding(scaled.paddingMedium),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$total",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = scaled.headlineMedium
                    ),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Total Plants",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = scaled.bodySmall
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                modifier = Modifier
                    .height(scaled.spacingXLarge)
                    .width(1.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${(avgHealth * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = scaled.headlineMedium
                    ),
                    fontWeight = FontWeight.Bold,
                    color = when {
                        avgHealth >= 0.75f -> Color(0xFF4CAF50)
                        avgHealth >= 0.45f -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
                Text(
                    "Avg Health",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = scaled.bodySmall
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                modifier = Modifier
                    .height(scaled.spacingXLarge)
                    .width(1.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (needingAttention > 0) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(scaled.iconSizeSmall)
                        )
                        Spacer(Modifier.width(scaled.spacingXSmall))
                    }
                    Text(
                        "$needingAttention",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = scaled.headlineMedium
                        ),
                        fontWeight = FontWeight.Bold,
                        color = if (needingAttention > 0) Color(0xFFFFC107) else Color(0xFF4CAF50)
                    )
                }
                Text(
                    "Need Care",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = scaled.bodySmall
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EnhancedPlantCard(
    plant: PlantProfile,
    metrics: PlantHealthCalculator.HealthMetrics,
    scaled: ScaledSizes,
    onDelete: (PlantProfile) -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (metrics.healthStatus) {
                "Critical" -> Color(0xFFFFF3E0)
                "Warning" -> Color(0xFFFFFDE7)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(scaled.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantAvatar(
                avatarConfig = plant.avatarConfig,
                health = metrics.healthStatus,
                size = scaled.avatarSizeLarge,
                animated = true
            )

            Spacer(Modifier.width(scaled.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plant.commonName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = scaled.titleMedium
                    ),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    plant.scientificName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = scaled.bodySmall
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(scaled.spacingXSmall))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(scaled.spacingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HealthBadge(metrics.healthStatus, scaled)

                    Text(
                        "${(metrics.overallHealth * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = scaled.labelSmall
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(scaled.buttonHeight)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete plant",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(scaled.iconSizeMedium)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete ${plant.commonName}?",
                    fontSize = scaled.titleMedium
                )
            },
            text = {
                Text(
                    "This action cannot be undone.",
                    fontSize = scaled.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(plant)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", fontSize = scaled.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", fontSize = scaled.labelLarge)
                }
            }
        )
    }
}

@Composable
private fun HealthBadge(status: String, scaled: ScaledSizes) {
    val (color, emoji) = when (status) {
        "Healthy" -> Color(0xFF4CAF50) to "✓"
        "Warning" -> Color(0xFFFFC107) to "⚠"
        "Critical" -> Color(0xFFF44336) to "!"
        else -> MaterialTheme.colorScheme.outline to "?"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = scaled.paddingSmall,
                vertical = scaled.paddingXSmall
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                emoji,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = scaled.labelSmall
                )
            )
            Spacer(Modifier.width(scaled.spacingXSmall))
            Text(
                status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = scaled.labelSmall
                ),
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}