package com.example.plantpal.screens.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.plantpal.PlantProfile
import com.example.plantpal.PlantRepository
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


@OptIn(ExperimentalMaterial3Api::class)
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
            },
            containerColor = Color.Transparent
        ) { padding ->

            when {
                uiState.isLoading -> LoadingState(padding)
                uiState.error != null -> ErrorState(uiState.error!!, padding) {
                    viewModel.loadPlant()
                }
                uiState.plant != null -> {
                    PlantDetailContent(
                        plant = uiState.plant!!,
                        onWaterPlant = viewModel::waterPlant,
                        onFertilizePlant = viewModel::fertilizePlant,
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
        PlantImage(plant)

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

        HealthStatusSection(plant, onUpdateHealth)

        Spacer(Modifier.height(16.dp))

        CareActionsSection(
            actionInProgress = actionInProgress,
            onWaterPlant = onWaterPlant,
            onFertilizePlant = onFertilizePlant
        )
    }
}

@Composable
private fun PlantImage(plant: PlantProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        if (plant.photoUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(plant.photoUrl),
                contentDescription = plant.commonName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("🌿", style = MaterialTheme.typography.displayLarge) }
        }
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
