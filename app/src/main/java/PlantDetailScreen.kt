package com.example.plantpal

import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ---------- UI STATE ----------
data class PlantDetailUiState(
    val plant: PlantProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionInProgress: String? = null
)

// ---------- VIEWMODEL ----------
class PlantDetailViewModel(private val plantId: String) : ViewModel() {
    private val repository = PlantRepository()

    private val _uiState: MutableStateFlow<PlantDetailUiState> =
        MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    init { loadPlant() }

    fun loadPlant() {
        viewModelScope.launch {
            _uiState.value = PlantDetailUiState(isLoading = true)
            val result: Result<PlantProfile> = repository.getPlant(plantId)
            result
                .onSuccess { plant: PlantProfile ->
                    _uiState.value = PlantDetailUiState(plant = plant, isLoading = false)
                }
                .onFailure { error: Throwable ->
                    _uiState.value = PlantDetailUiState(error = error.message, isLoading = false)
                }
        }
    }

    fun waterPlant() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "watering")
            val result: Result<Unit> = repository.waterPlant(plantId)
            result
                .onSuccess { loadPlant() }
                .onFailure { e: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message, actionInProgress = null
                    )
                }
        }
    }

    fun fertilizePlant() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "fertilizing")
            val result: Result<Unit> = repository.fertilizePlant(plantId)
            result
                .onSuccess { loadPlant() }
                .onFailure { e: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message, actionInProgress = null
                    )
                }
        }
    }

    fun updateHealthStatus(health: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "updating")
            val result: Result<Unit> = repository.updateHealthStatus(plantId, health)
            result
                .onSuccess { loadPlant() }
                .onFailure { e: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message, actionInProgress = null
                    )
                }
        }
    }

    fun updatePlant(updated: PlantProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "saving")
            val result: Result<Unit> = repository.updatePlant(updated)
            result
                .onSuccess { loadPlant() }
                .onFailure { e: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message, actionInProgress = null
                    )
                }
        }
    }
}

// ---------- SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PlantDetailScreen(
    plantId: String,
    onNavigateBack: () -> Unit,
    viewModel: PlantDetailViewModel = viewModel(
        modelClass = PlantDetailViewModel::class.java,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlantDetailViewModel(plantId) as T
            }
        }
    )
) {
    val uiState: PlantDetailUiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showHealthDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plant?.commonName ?: "Plant Details") },
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
                ) { CircularProgressIndicator() }
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
                            "Error loading plant",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.error ?: "Unknown error")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlant() }) { Text("Retry") }
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
            onSave = { updated: PlantProfile ->
                viewModel.updatePlant(updated)
                showEditDialog = false
            }
        )
    }

    if (showHealthDialog) {
        HealthStatusDialog(
            currentHealth = uiState.plant?.health ?: "healthy",
            onDismiss = { showHealthDialog = false },
            onSelect = { health: String ->
                viewModel.updateHealthStatus(health)
                showHealthDialog = false
            }
        )
    }
}

// ---------- SUPPORTING COMPOSABLES ----------
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
                ) { Text("🌿", style = MaterialTheme.typography.displayLarge) }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(plant.commonName, style = MaterialTheme.typography.headlineMedium)
        Text(
            plant.scientificName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (plant.confidence > 0.0) {
            Text(
                "Confidence: ${(plant.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Health Status", style = MaterialTheme.typography.titleMedium)
                Text(
                    plant.health.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
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

        Spacer(Modifier.height(16.dp))
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
                    onValueChange = { value: String -> commonName = value },
                    label = { Text("Common Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = scientificName,
                    onValueChange = { value: String -> scientificName = value },
                    label = { Text("Scientific Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { value: String -> notes = value },
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
    val healthOptions: List<String> = listOf("healthy", "warning", "critical")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Health Status") },
        text = {
            Column {
                healthOptions.forEach { health: String ->
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
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = health.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
