package com.example.plantpal

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun PlantsHomeScreen(
    viewModel: PlantsViewModel = viewModel(),
    onPlantClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
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
                Text(
                    "Your plants will appear here 🌱\n\nTap + to add your first plant!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyColumn(
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            PlantAvatar(
                avatarConfig = plant.avatarConfig,
                health = plant.health,
                size = 80.dp,
                animated = false,
                modifier = Modifier
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(plant.commonName, style = MaterialTheme.typography.titleMedium)
                Text(
                    plant.scientificName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val emoji = when (plant.health) {
                    "healthy" -> "😊"
                    "warning" -> "😟"
                    "critical" -> "😢"
                    else -> "🌱"
                }

                Text(
                    "$emoji ${plant.health.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (plant.health) {
                        "healthy" -> MaterialTheme.colorScheme.primary
                        "warning" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete plant", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant?") },
            text = { Text("Are you sure you want to delete ${plant.commonName}?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(plant)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
