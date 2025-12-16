package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun DashboardScreen(
    onOpenLibrary: () -> Unit,
    onAddPlant: () -> Unit,
    onOpenPlant: (String) -> Unit,
    onOpenBadges: () -> Unit = {}
) {
    val repo = remember { PlantRepository() }
    val progressRepo = remember { ProgressRepository() }
    
    val plantRepo = remember { PlantRepository() }

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
                .onFailure { /* Progress is optional, so just log */ }
            
            plantRepo.getAllPlants()
                .onSuccess { plants = it; error = null }
                .onFailure { error = it.message }

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

    val plantsWithHealth = plants.map { plant ->
        plant to PlantHealthCalculator.calculateHealth(plant)
    }

    val total = plants.size
    val healthyPlants = plantsWithHealth.count { it.second.overallHealth >= 0.75f }
    val warningPlants = plantsWithHealth.count { it.second.overallHealth in 0.45f..0.75f }
    val criticalPlants = plantsWithHealth.count { it.second.overallHealth < 0.45f }

    val dueWater = plants.count { daysSince(it.lastWatered) >= it.wateringFrequency }
    val dueFert = plants.count { daysSince(it.lastFertilized) >= it.fertilizerFrequency }

    val urgentPlants = plantsWithHealth
        .filter { (_, metrics) ->
            PlantHealthCalculator.getCareUrgency(metrics) >= 2
        }
        .sortedByDescending { (_, metrics) -> PlantHealthCalculator.getCareUrgency(metrics) }
        .take(5)

    val upcomingWater = plantsWithHealth
        .sortedBy { (_, metrics) -> metrics.daysUntilWaterNeeded }
        .take(5)

    val overallHealth = if (plants.isNotEmpty()) {
        plantsWithHealth.map { it.second.overallHealth }.average().toFloat()
    } else 0f

    val now = System.currentTimeMillis()
    val dueCustomReminders = customReminders
        .filter { it.isEnabled && it.nextFireAt <= now }
        .sortedBy { it.nextFireAt }
        .take(5)

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Couldn’t load dashboard", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    scope.launch {
                        isLoading = true
                        plantRepo.getAllPlants()
                            .onSuccess { plants = it; error = null }
                            .onFailure { error = it.message }

                        reminderRepo.getReminders()
                            .onSuccess { customReminders = it }

                        isLoading = false
                    }
                }) {
                    Text("Retry")
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                }
                if (progress != null) {
                    item {
                        StreakWidget(
                            currentStreak = progress!!.currentStreak,
                            longestStreak = progress!!.longestStreak,
                            onClick = onOpenBadges
                        )
                    }
                }

                if (total > 0) {
                    item {
                        OverallHealthCard(
                            overallHealth = overallHealth,
                            healthyCount = healthyPlants,
                            warningCount = warningPlants,
                            criticalCount = criticalPlants,
                            totalCount = total
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EnhancedStatCard(
                            label = "Total Plants",
                            value = total.toString(),
                            icon = "🌱",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedStatCard(
                            label = "Need Water",
                            value = dueWater.toString(),
                            icon = "💧",
                            color = if (dueWater > 0) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedStatCard(
                            label = "Need Food",
                            value = dueFert.toString(),
                            icon = "🌿",
                            color = if (dueFert > 0) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (total == 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(24.dp)
                            ) {
                                Text(
                                    "No plants yet 🌱",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Start your plant care journey today!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = onAddPlant) {
                                    Text("Add Your First Plant")
                                }
                            }
                        }
                    }
                } else {
                    if (urgentPlants.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "🚨 Needs Urgent Care",
                                action = null
                            )
                        }

                        items(urgentPlants) { (plant, metrics) ->
                            UrgentPlantCard(
                                plant = plant,
                                metrics = metrics,
                                onClick = { onOpenPlant(plant.plantId) }
                            )
                        }
                    }

                    if (upcomingWater.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📅 Upcoming Care",
                                action = "See All",
                                onAction = onOpenLibrary
                            )
                        }

                        items(upcomingWater) { (plant, metrics) ->
                            UpcomingCareCard(
                                plant = plant,
                                metrics = metrics,
                                onClick = { onOpenPlant(plant.plantId) }
                            )
                        }
                    }

                    if (dueCustomReminders.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "⏰ Custom reminders",
                                action = null
                            )
                        }

                        items(dueCustomReminders) { reminder ->
                            CustomReminderDashboardCard(
                                reminder = reminder,
                                onClick = {
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
private fun OverallHealthCard(
    overallHealth: Float,
    healthyCount: Int,
    warningCount: Int,
    criticalCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            Modifier.padding(20.dp)
        ) {
            Text(
                "Overall Plant Health",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${(overallHealth * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        "$healthyCount of $totalCount plants are thriving",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HealthChip("Healthy", healthyCount, MaterialTheme.colorScheme.primary)
                        HealthChip("Warning", warningCount, MaterialTheme.colorScheme.tertiary)
                        HealthChip("Critical", criticalCount, MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthChip(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "$label ($count)",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EnhancedStatCard(
    label: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action)
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun UrgentPlantCard(
    plant: PlantProfile,
    metrics: PlantHealthCalculator.HealthMetrics,
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantAvatar(
                avatarConfig = plant.avatarConfig,
                health = metrics.healthStatus,
                size = 50.dp,
                animated = false
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plant.commonName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    metrics.healthStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE65100)
                )
            }
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF6F00),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun UpcomingCareCard(
    plant: PlantProfile,
    metrics: PlantHealthCalculator.HealthMetrics,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                plant.commonName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Water in ${metrics.daysUntilWaterNeeded} days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

//custom reminders!
@Composable
private fun CustomReminderDashboardCard(
    reminder: CustomReminder,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            Modifier.padding(16.dp)
        ) {
            Text(
                text = reminder.title.ifBlank { "Reminder" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = reminder.message.ifBlank {
                    reminder.plantName?.let { "For $it" } ?: "Custom plant task"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plant.commonName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    when {
                        metrics.daysUntilWaterNeeded <= 0 -> "💧 Water now"
                        metrics.daysUntilWaterNeeded == 1 -> "💧 Water in 1 day"
                        else -> "💧 Water in ${metrics.daysUntilWaterNeeded} days"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Surface(
                shape = CircleShape,
                color = when {
                    metrics.overallHealth >= 0.75f -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    metrics.overallHealth >= 0.45f -> Color(0xFFFFC107).copy(alpha = 0.2f)
                    else -> Color(0xFFF44336).copy(alpha = 0.2f)
                }
            ) {
                Text(
                    "${(metrics.overallHealth * 100).toInt()}%",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        metrics.overallHealth >= 0.75f -> Color(0xFF4CAF50)
                        metrics.overallHealth >= 0.45f -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
            }
        }
    }
}
