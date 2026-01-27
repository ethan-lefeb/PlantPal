package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.CustomReminder
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ReminderRepository
import com.example.plantpal.ui.theme.LocalUIScale
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
    val scaled = LocalUIScale.current
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
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = scaled.bodyLarge
                    ),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(scaled.spacingMedium))
                TextButton(onClick = { viewModel.refresh() }) {
                    Text("Retry", fontSize = scaled.labelLarge)
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
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(scaled.iconSizeLarge)
                        )
                        Spacer(Modifier.height(scaled.spacingSmall))
                        Text(
                            "No care needed today 🎉",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = scaled.titleMedium
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaled.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(scaled.spacingMedium)
                ) {
                    // Header
                    item {
                        Text(
                            "Today's alerts",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = scaled.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(scaled.spacingXSmall))
                        Text(
                            "$total item${if (total == 1) "" else "s"} due today",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = scaled.bodyMedium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Water section
                    if (state.water.isNotEmpty()) {
                        item { SectionHeader("Watering", scaled) }
                        items(state.water) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = "Needs watering",
                                actionLabel = "Mark done",
                                scaled = scaled,
                                onAction = {
                                    viewModel.markCareDone(alert)
                                    CareReminderWorker.triggerCareNow(context)
                                },
                                onOpen = { onOpenPlant(alert.plant.plantId) }
                            )
                        }
                    }

                    // Fertilize section
                    if (state.fertilize.isNotEmpty()) {
                        item { SectionHeader("Fertilizing", scaled) }
                        items(state.fertilize) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = "Needs fertilizer",
                                actionLabel = "Mark done",
                                scaled = scaled,
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
                        item { SectionHeader("Rotation", scaled) }
                        items(state.rotate) { alert ->
                            CareCard(
                                title = alert.plant.commonName,
                                subtitle = "Should be rotated",
                                actionLabel = "Mark done",
                                scaled = scaled,
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
                        item { SectionHeader("Custom reminders", scaled) }
                        items(state.customReminders) { reminder ->
                            CustomReminderAlertCard(
                                reminder = reminder,
                                scaled = scaled,
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
private fun SectionHeader(title: String, scaled: com.example.plantpal.ui.theme.ScaledSizes) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = scaled.titleLarge,
            fontWeight = FontWeight.Bold
        )
    )
    Spacer(Modifier.height(scaled.spacingXSmall))
}

@Composable
private fun CareCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    scaled: com.example.plantpal.ui.theme.ScaledSizes,
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = scaled.titleMedium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = scaled.bodyMedium
                    )
                )
            },
            trailingContent = {
                FilledTonalButton(
                    onClick = onAction,
                    modifier = Modifier.height(scaled.buttonHeight)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(scaled.iconSizeSmall)
                    )
                    Spacer(Modifier.width(scaled.spacingSmall))
                    Text(actionLabel, fontSize = scaled.labelMedium)
                }
            }
        )
    }
}

@Composable
private fun CustomReminderAlertCard(
    reminder: CustomReminder,
    scaled: com.example.plantpal.ui.theme.ScaledSizes,
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = scaled.titleMedium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    reminder.message.ifBlank {
                        reminder.plantName?.let { "For $it" } ?: "Custom plant task"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = scaled.bodyMedium
                    )
                )
            }
        )
    }
}