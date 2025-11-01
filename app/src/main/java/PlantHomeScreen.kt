package com.example.plantpal

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- UI STATE ---
data class PlantsUiState(
    val plants: List<PlantProfile> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// --- VIEWMODEL ---
class PlantsViewModel : ViewModel() {
    private val repository = PlantRepository()
    private val _uiState = MutableStateFlow(PlantsUiState())
    val uiState: StateFlow<PlantsUiState> = _uiState.asStateFlow()

    init {
        loadPlants()
    }

    fun loadPlants() {
        viewModelScope.launch {
            _uiState.value = PlantsUiState(isLoading = true)
            val result = repository.getAllPlants()

            result.onSuccess { plants ->
                _uiState.value = PlantsUiState(plants = plants, isLoading = false)
            }.onFailure { error ->
                _uiState.value = PlantsUiState(error = error.message, isLoading = false)
            }
        }
    }

    fun deletePlant(plant: PlantProfile) {
        viewModelScope.launch {
            val result = repository.deletePlant(plant.plantId)
            result.onSuccess {
                loadPlants()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }
}

// --- MAIN SCREEN ---
@Composable
fun PlantsHomeScreen(
    viewModel: PlantsViewModel = viewModel(),
    onPlantClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error loading plants",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlants() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.plants.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your plants will appear here 🌱\n\nTap + to add your first plant!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.plants) { plant ->
                        PlantCard(
                            plant = plant,
                            onDelete = { viewModel.deletePlant(it) },
                            onClick = { onPlantClick(plant.plantId) }
                        )
                    }
                }
            }
        }
    }
}

// --- CARD ---
@Composable
fun PlantCard(
    plant: PlantProfile,
    onDelete: (PlantProfile) -> Unit,
    onClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            PlantAvatar(
                avatarConfig = plant.avatarConfig,
                health = plant.health,
                modifier = Modifier,
                size = 80.dp,
                animated = false
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.commonName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = plant.scientificName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val healthEmoji = when (plant.health) {
                    "healthy" -> "😊"
                    "warning" -> "😐"
                    "critical" -> "😢"
                    else -> "🌱"
                }
                Text(
                    text = "$healthEmoji ${plant.health.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (plant.health) {
                        "healthy" -> MaterialTheme.colorScheme.primary
                        "warning" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )

                if (plant.careInfo.wateringMaxDays != null) {
                    Text(
                        text = "💧 Water every ${plant.careInfo.wateringMinDays}-${plant.careInfo.wateringMaxDays} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete plant",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant?") },
            text = { Text("Are you sure you want to delete ${plant.commonName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(plant)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
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
