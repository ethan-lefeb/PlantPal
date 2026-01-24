package com.example.plantpal.com.example.plantpal.ui.screens.com.example.plantpal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.plantpal.PlantAvatar
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantHealthCalculator
import com.example.plantpal.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ProgressRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ReminderRepository
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.StreakWidget
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.UserProgress
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.CustomReminder
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.ui.theme.LocalUIScale
import com.example.plantpal.ui.theme.ScaledSizes
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun DashboardScreen(
    onOpenLibrary: () -> Unit,
    onAddPlant: () -> Unit,
    onOpenPlant: (String) -> Unit,
    onOpenBadges: () -> Unit = {}
) {
    val scaled = LocalUIScale.current

    val repo = remember { PlantRepository() }
    val progressRepo = remember { ProgressRepository() }

    val reminderRepo = remember { ReminderRepository() }
    var customReminders by remember { mutableStateOf<List<CustomReminder>>(emptyList()) }

    var plants by remember { mutableStateOf<List<PlantProfile>>(emptyList()) }
    var progress by remember { mutableStateOf<UserProgress?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true

            val plantsResult = repo.getAllPlants()
            val progressResult = progressRepo.getUserProgress()

            plantsResult
                .onSuccess { plants = it; error = null }
                .onFailure { error = it.message }

            progressResult
                .onSuccess { progress = it }
                .onFailure { /* Progress is optional */ }

            reminderRepo.getReminders()
                .onSuccess { customReminders = it }
                .onFailure {  }

            isLoading = false
        }
    }

    fun daysSince(ts: Long): Int {
        if (ts <= 0L) return Int.MAX_VALUE
        val days = (System.currentTimeMillis() - ts) / (1000L * 60 * 60 * 24)
        return max(0, days.toInt())
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaled.paddingMedium),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = scaled.bodyMedium
                )
                Spacer(Modifier.height(scaled.spacingMedium))
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val result = repo.getAllPlants()
                            result.onSuccess { plants = it; error = null }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.height(scaled.buttonHeight)
                ) {
                    Text("Retry", fontSize = scaled.labelLarge)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaled.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(scaled.spacingMedium)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier.padding(
                    top = scaled.paddingLarge,
                    bottom = scaled.paddingMedium
                )
            ) {
                Text(
                    "🌿 PlantPal",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = scaled.displayLarge
                    ),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Your plant care companion",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = scaled.bodyLarge
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Streak Widget
        if (progress != null) {
            item {
                StreakWidget(
                    currentStreak = progress!!.currentStreak,
                    longestStreak = progress!!.longestStreak,
                    onClick = onOpenBadges,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Quick Stats
        item {
            QuickStatsCard(plants = plants, scaled = scaled)
        }

        // Plants needing attention
        item {
            SectionHeader(
                title = "Plants Needing Attention",
                action = "View All",
                onAction = onOpenLibrary,
                scaled = scaled
            )
        }

        val urgentPlants = plants.filter { plant ->
            val metrics = PlantHealthCalculator.calculateHealth(plant)
            metrics.healthStatus == "Critical" || metrics.healthStatus == "Warning"
        }

        if (urgentPlants.isEmpty()) {
            item {
                EmptyStateCard(scaled = scaled)
            }
        } else {
            items(urgentPlants.take(3)) { plant ->
                val metrics = PlantHealthCalculator.calculateHealth(plant)
                UrgentPlantCard(
                    plant = plant,
                    metrics = metrics,
                    scaled = scaled,
                    onClick = { onOpenPlant(plant.plantId) }
                )
            }
        }

        // Custom reminders
        val dueReminders = customReminders.filter {
            it.isEnabled && it.nextFireAt <= System.currentTimeMillis()
        }

        if (dueReminders.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Custom Reminders",
                    scaled = scaled
                )
            }

            items(dueReminders.take(3)) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    scaled = scaled,
                    onClick = {
                        reminder.plantId?.let { plantId ->
                            if (plantId.isNotBlank()) onOpenPlant(plantId)
                        }
                    }
                )
            }
        }

        // Add Plant Button
        item {
            Button(
                onClick = onAddPlant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaled.buttonHeight),
                contentPadding = PaddingValues(
                    horizontal = scaled.paddingMedium,
                    vertical = scaled.paddingSmall
                )
            ) {
                Text("➕ Add New Plant", fontSize = scaled.labelLarge)
            }
        }
    }
}

@Composable
private fun QuickStatsCard(plants: List<PlantProfile>, scaled: ScaledSizes) {
    val healthyCount = plants.count {
        PlantHealthCalculator.calculateHealth(it).healthStatus == "Healthy"
    }
    val warningCount = plants.count {
        PlantHealthCalculator.calculateHealth(it).healthStatus == "Warning"
    }
    val criticalCount = plants.count {
        PlantHealthCalculator.calculateHealth(it).healthStatus == "Critical"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(scaled.paddingMedium)) {
            Text(
                "Garden Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = scaled.titleMedium
                ),
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(scaled.spacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(scaled.spacingMedium)
            ) {
                HealthChip("Healthy", healthyCount, MaterialTheme.colorScheme.primary, scaled)
                HealthChip("Warning", warningCount, MaterialTheme.colorScheme.tertiary, scaled)
                HealthChip("Critical", criticalCount, MaterialTheme.colorScheme.error, scaled)
            }
        }
    }
}

@Composable
private fun HealthChip(label: String, count: Int, color: Color, scaled: ScaledSizes) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "$label ($count)",
            modifier = Modifier.padding(
                horizontal = scaled.paddingSmall,
                vertical = scaled.paddingXSmall
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = scaled.bodyMedium
            ),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    scaled: ScaledSizes
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = scaled.titleMedium
            ),
            fontWeight = FontWeight.Bold
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, fontSize = scaled.labelLarge)
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(scaled.iconSizeSmall)
                )
            }
        }
    }
}

@Composable
private fun UrgentPlantCard(
    plant: PlantProfile,
    metrics: PlantHealthCalculator.HealthMetrics,
    scaled: ScaledSizes,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(scaled.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantAvatar(
                avatarConfig = plant.avatarConfig,
                health = metrics.healthStatus,
                size = scaled.avatarSizeMedium,
                animated = false
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
                    metrics.healthStatus,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = scaled.bodyMedium
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Icon(
                Icons.Default.Warning,
                contentDescription = "Needs attention",
                modifier = Modifier.size(scaled.iconSizeMedium),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: CustomReminder,
    scaled: ScaledSizes,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(scaled.paddingMedium)) {
            Text(
                reminder.title.ifBlank { "Custom Reminder" },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = scaled.titleSmall
                ),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(scaled.spacingXSmall))
            Text(
                reminder.message.ifBlank {
                    reminder.plantName?.let { "For $it" } ?: "Custom plant task"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = scaled.bodyMedium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyStateCard(scaled: ScaledSizes) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(scaled.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎉",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = scaled.displayLarge
                )
            )
            Spacer(Modifier.height(scaled.spacingSmall))
            Text(
                "All plants are happy!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = scaled.titleMedium
                ),
                fontWeight = FontWeight.Bold
            )
            Text(
                "No plants need immediate attention",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = scaled.bodyMedium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}