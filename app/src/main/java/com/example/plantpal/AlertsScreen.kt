package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.math.max

private fun daysSince(ts: Long): Int {
    if (ts <= 0L) return Int.MAX_VALUE // treat as never done
    val days = (System.currentTimeMillis() - ts) / (1000L * 60 * 60 * 24)
    return max(0, days.toInt())
}

data class CareAlert(
    val plant: PlantProfile,
    val type: CareType
)

enum class CareType { WATER, FERTILIZE, ROTATE }

data class AlertsUiState(
    val water: List<CareAlert> = emptyList(),
    val fertilize: List<CareAlert> = emptyList(),
    val rotate: List<CareAlert> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class AlertsViewModel : ViewModel() {
    private val repo = PlantRepository()
    var ui by mutableStateOf(AlertsUiState())
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            ui = ui.copy(loading = true, error = null)
            repo.getAllPlants()
                .onSuccess { plants ->
                    // Water due
                    val water = plants.filter { p ->
                        val interval = when {
                            p.wateringFrequency > 0 -> p.wateringFrequency
                            p.careInfo.wateringMinDays != null -> p.careInfo.wateringMinDays!!
                            p.careInfo.wateringMaxDays != null -> p.careInfo.wateringMaxDays!!
                            else -> 7
                        }
                        (interval - daysSince(p.lastWatered)) <= 0
                    }.map { CareAlert(it, CareType.WATER) }

                    // Fertilizer due
                    val fert = plants.filter { p ->
                        val interval = if (p.fertilizerFrequency > 0) p.fertilizerFrequency else 30
                        (interval - daysSince(p.lastFertilized)) <= 0
                    }.map { CareAlert(it, CareType.FERTILIZE) }

                    // Rotation due
                    val rotate = plants.filter { p ->
                        val interval = p.careProfile.rotationFrequency
                        val last = p.careProfile.lastRotated
                        (interval - daysSince(last)) <= 0
                    }.map { CareAlert(it, CareType.ROTATE) }

                    ui = AlertsUiState(
                        water = water.sortedBy { it.plant.commonName.lowercase() },
                        fertilize = fert.sortedBy { it.plant.commonName.lowercase() },
                        rotate = rotate.sortedBy { it.plant.commonName.lowercase() },
                        loading = false
                    )
                }
                .onFailure { e ->
                    ui = AlertsUiState(loading = false, error = e.message)
                }
        }
    }

    fun markCareDone(alert: CareAlert) {
        viewModelScope.launch {
            when (alert.type) {
                CareType.WATER -> repo.waterPlant(alert.plant.plantId)
                CareType.FERTILIZE -> repo.fertilizePlant(alert.plant.plantId)
                CareType.ROTATE -> {
                    val updated = alert.plant.copy(
                        careProfile = alert.plant.careProfile.copy(
                            lastRotated = System.currentTimeMillis()
                        )
                    )
                    repo.updatePlant(updated)
                }
            }.onSuccess { refresh() }
        }
    }
}

@Composable
fun AlertsScreen(
    onOpenPlant: (String) -> Unit
) {
    val vm = remember { AlertsViewModel() }
    val state = vm.ui

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Couldn’t load alerts", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.refresh() }) { Text("Retry") }
            }
        }
        else -> {
            val total = state.water.size + state.fertilize.size + state.rotate.size
            if (total == 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(Modifier.height(8.dp))
                        Text("No care needed today 🎉")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Today’s alerts", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$total item${if (total == 1) "" else "s"} due today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (state.water.isNotEmpty()) {
                        item { SectionHeader("Water") }
                        items(state.water) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = alert.plant.scientificName.ifBlank { "Needs water today" },
                                actionLabel = "Mark watered",
                                onAction = { vm.markCareDone(alert) },
                                onOpen = { onOpenPlant(alert.plant.plantId) }
                            )
                        }
                    }

                    if (state.fertilize.isNotEmpty()) {
                        item { SectionHeader("Fertilize") }
                        items(state.fertilize) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = alert.plant.scientificName.ifBlank { "Needs fertilizer today" },
                                actionLabel = "Mark fertilized",
                                onAction = { vm.markCareDone(alert) },
                                onOpen = { onOpenPlant(alert.plant.plantId) }
                            )
                        }
                    }

                    if (state.rotate.isNotEmpty()) {
                        item { SectionHeader("Rotate") }
                        items(state.rotate) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = alert.plant.scientificName.ifBlank { "Needs rotation today" },
                                actionLabel = "Mark rotated",
                                onAction = { vm.markCareDone(alert) },
                                onOpen = { onOpenPlant(alert.plant.plantId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun CareCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    onOpen: () -> Unit
) {
    ElevatedCard(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            supportingContent = { Text(subtitle) },
            trailingContent = {
                FilledTonalButton(onClick = onAction) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(actionLabel)
                }
            }
        )
    }
}
