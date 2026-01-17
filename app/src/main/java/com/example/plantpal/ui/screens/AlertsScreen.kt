package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.CustomReminder
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ReminderRepository
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
    val customReminders: List<CustomReminder> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class AlertsViewModel : ViewModel() {

    private val repo = PlantRepository()
    private val reminderRepo = ReminderRepository()

    var uiState by mutableStateOf(AlertsUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)

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
                        val interval =
                            if (p.fertilizerFrequency > 0) p.fertilizerFrequency else 30
                        (interval - daysSince(p.lastFertilized)) <= 0
                    }.map { CareAlert(it, CareType.FERTILIZE) }

                    // Rotation due
                    val rotate = plants.filter { p ->
                        val interval = p.careProfile.rotationFrequency
                        val last = p.careProfile.lastRotated
                        (interval - daysSince(last)) <= 0
                    }.map { CareAlert(it, CareType.ROTATE) }

                    // Custom reminders
                    val remindersResult = reminderRepo.getReminders()
                    val now = System.currentTimeMillis()
                    val dueCustom = remindersResult
                        .getOrDefault(emptyList())
                        .filter { it.isEnabled && it.nextFireAt <= now }
                        .sortedBy { it.nextFireAt }

                    uiState = AlertsUiState(
                        water = water.sortedBy { it.plant.commonName.lowercase() },
                        fertilize = fert.sortedBy { it.plant.commonName.lowercase() },
                        rotate = rotate.sortedBy { it.plant.commonName.lowercase() },
                        customReminders = dueCustom,
                        loading = false
                    )
                }
                .onFailure { e ->
                    uiState = uiState.copy(loading = false, error = e.message)
                }
        }
    }

    fun markCareDone(alert: CareAlert) {
        viewModelScope.launch {
            val result = when (alert.type) {
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
            }

            result.onSuccess {
                refresh()
            }
        }
    }
}


@Composable
fun AlertsScreen(
    onOpenPlant: (String) -> Unit
) {
    val viewModel = remember { AlertsViewModel() }
    val state = viewModel.uiState
    val context = LocalContext.current

    when {
        state.loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        state.error != null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Couldn't load alerts",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { viewModel.refresh() }) {
                    Text("Retry")
                }
            }
        }

        else -> {
            val totalCareAlerts =
                state.water.size + state.fertilize.size + state.rotate.size
            val totalCustom = state.customReminders.size
            val total = totalCareAlerts + totalCustom

            if (total == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                    // Header
                    item {
                        Text(
                            "Today’s alerts",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$total item${if (total == 1) "" else "s"} due today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Water section
                    if (state.water.isNotEmpty()) {
                        item { SectionHeader("Watering") }
                        items(state.water) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = "Needs watering",
                                actionLabel = "Mark done",
                                onAction = {
                                    // Update lastWatered + optionally trigger worker
                                    viewModel.markCareDone(alert)
                                    CareReminderWorker.triggerCareNow(context)
                                },
                                onOpen = { onOpenPlant(alert.plant.plantId) }
                            )
                        }
                    }

                    // Fertilize section
                    if (state.fertilize.isNotEmpty()) {
                        item { SectionHeader("Fertilizing") }
                        items(state.fertilize) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = "Needs fertilizer",
                                actionLabel = "Mark done",
                                onAction = {
                                    viewModel.markCareDone(alert)
                                    CareReminderWorker.triggerCareNow(context)
                                },
                                onOpen = { onOpenPlant(alert.plant.plantId) }
                            )
                        }
                    }

                    // Rotate section
                    if (state.rotate.isNotEmpty()) {
                        item { SectionHeader("Rotation") }
                        items(state.rotate) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = "Should be rotated",
                                actionLabel = "Mark done",
                                onAction = {
                                    viewModel.markCareDone(alert)
                                    CareReminderWorker.triggerCareNow(context)
                                },
                                onOpen = { onOpenPlant(alert.plant.plantId) }
                            )
                        }
                    }

                    // Custom reminders section
                    if (state.customReminders.isNotEmpty()) {
                        item { SectionHeader("Custom reminders") }
                        items(state.customReminders) { reminder ->
                            CustomReminderAlertCard(
                                reminder = reminder,
                                onOpen = {
                                    val plantId = reminder.plantId
                                    if (!plantId.isNullOrBlank()) {
                                        onOpenPlant(plantId)
                                    }
                                }
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
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge
    )
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
            headlineContent = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = { Text(subtitle) },
            trailingContent = {
                FilledTonalButton(
                    onClick = onAction,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2E7D32), // Material green
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(actionLabel)
                }
            }
        )
    }
}

@Composable
private fun CustomReminderAlertCard(
    reminder: CustomReminder,
    onOpen: () -> Unit
) {
    ElevatedCard(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    reminder.title.ifBlank { "Reminder" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    reminder.message.ifBlank {
                        reminder.plantName?.let { "For $it" } ?: "Custom plant task"
                    }
                )
            }
        )
    }
}