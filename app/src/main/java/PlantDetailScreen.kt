package com.example.plantpal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
            val result = repository.getPlant(plantId)

            result.onSuccess { plant ->
                _uiState.value = PlantDetailUiState(plant = plant, isLoading = false)
            }.onFailure { error ->
                _uiState.value = PlantDetailUiState(error = error.message, isLoading = false)
            }
        }
    }

    fun waterPlant() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "watering")
            val result = repository.waterPlant(plantId)

            result.onSuccess {
                loadPlant()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    actionInProgress = null
                )
            }
        }
    }

    fun fertilizePlant() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "fertilizing")
            val result = repository.fertilizePlant(plantId)

            result.onSuccess {
                loadPlant()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    actionInProgress = null
                )
            }
        }
    }

    fun updateHealthStatus(health: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "updating")
            val result = repository.updateHealthStatus(plantId, health)

            result.onSuccess {
                loadPlant()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    actionInProgress = null
                )
            }
        }
    }

    fun updatePlant(updatedPlant: PlantProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "saving")
            val result = repository.updatePlant(updatedPlant)

            result.onSuccess {
                loadPlant()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    actionInProgress = null
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: String,
    onNavigateBack: () -> Unit,
    viewModel: PlantDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PlantDetailViewModel(plantId) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showHealthDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plant?.commonName ?: "Plant Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error loading plant",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = uiState.error ?: "Unknown error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlant() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.plant != null -> {
                PlantDetailContent(
                    plant = uiState.plant!!,
                    onWaterPlant = { viewModel.waterPlant() },
                    onFertilizePlant = { viewModel.fertilizePlant() },
                    onUpdateHealth = { showHealthDialog = true },
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
            onSave = { updatedPlant ->
                viewModel.updatePlant(updatedPlant)
                showEditDialog = false
            }
        )
    }

    if (showHealthDialog) {
        HealthStatusDialog(
            currentHealth = uiState.plant?.health ?: "healthy",
            onDismiss = { showHealthDialog = false },
            onSelect = { health ->
                viewModel.updateHealthStatus(health)
                showHealthDialog = false
            }
        )
    }
}

@Composable
fun PlantDetailContent(
    plant: PlantProfile,
    onWaterPlant: () -> Unit,
    onFertilizePlant: () -> Unit,
    onUpdateHealth: () -> Unit,
    actionInProgress: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (plant.photoUrl.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(plant.photoUrl),
                    contentDescription = plant.commonName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", style = MaterialTheme.typography.displayLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = plant.commonName,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = plant.scientificName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (plant.confidence > 0.0) {
            Text(
                text = "Confidence: ${(plant.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Health Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = plant.health.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (plant.health) {
                        "healthy" -> MaterialTheme.colorScheme.primary
                        "warning" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            TextButton(onClick = onUpdateHealth) {
                Text("Update")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Care Actions",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

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
                Spacer(modifier = Modifier.width(8.dp))
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
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fertilize")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Care History",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                CareHistoryItem(
                    label = "Last Watered",
                    timestamp = plant.lastWatered
                )
                Spacer(modifier = Modifier.height(12.dp))
                CareHistoryItem(
                    label = "Last Fertilized",
                    timestamp = plant.lastFertilized
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (plant.notes.isNotBlank()) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = plant.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Added ${formatDate(plant.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CareHistoryItem(
    label: String,
    timestamp: Long
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (timestamp > 0) formatDate(timestamp) else "Never",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = scientificName,
                    onValueChange = { scientificName = it },
                    label = { Text("Scientific Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
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
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
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
                healthOptions.forEach { health ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentHealth == health,
                            onClick = { onSelect(health) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = health.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}