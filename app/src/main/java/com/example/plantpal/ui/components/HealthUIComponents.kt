package com.example.plantpal.com.example.plantpal.ui.components.com.example.plantpal.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantHealthCalculator
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile

@Composable
fun HealthCircleIndicator(
    healthPercentage: Int,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    animated: Boolean = true
) {
    val healthColor = when {
        healthPercentage >= 75 -> Color(0xFF4CAF50)
        healthPercentage >= 45 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    val animatedProgress = if (animated) {
        animateFloatAsState(
            targetValue = healthPercentage / 100f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            label = "progress"
        ).value
    } else {
        healthPercentage / 100f
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val radius = (canvasSize.minDimension - strokeWidth.toPx()) / 2
            val centerX = canvasSize.width / 2
            val centerY = canvasSize.height / 2

            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            val sweepAngle = animatedProgress * 360f
            drawArc(
                color = healthColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$healthPercentage%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = healthColor
            )
            Text(
                "Health",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricProgressBar(
    label: String,
    level: Float,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val color = when {
        level >= 0.7f -> Color(0xFF4CAF50)
        level >= 0.4f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                "${(level * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = level,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun HealthMetricsCard(
    plant: PlantProfile,
    metrics: PlantHealthCalculator.HealthMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Health Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthCircleIndicator(
                    healthPercentage = (metrics.overallHealth * 100).toInt(),
                    size = 100.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricProgressBar(
                        label = "Hydration",
                        level = metrics.hydrationLevel,
                        icon = Icons.Default.WaterDrop
                    )
                    MetricProgressBar(
                        label = "Nutrition",
                        level = metrics.nutritionLevel,
                        icon = Icons.Default.Eco
                    )
                    MetricProgressBar(
                        label = "Consistency",
                        level = metrics.careConsistency,
                        icon = Icons.Default.CalendarToday
                    )
                }
            }

            Divider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(PlantHealthCalculator.getHealthColor(metrics))
                    )
                    Text(
                        PlantHealthCalculator.getHealthDescription(metrics),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (metrics.primaryConcern != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⚠️",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                metrics.primaryConcern,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CareScheduleItem(
                    icon = "💧",
                    label = "Water",
                    daysUntil = metrics.daysUntilWaterNeeded
                )
                CareScheduleItem(
                    icon = "🌿",
                    label = "Fertilize",
                    daysUntil = metrics.daysUntilFertilizerNeeded
                )
            }
        }
    }
}

@Composable
private fun CareScheduleItem(
    icon: String,
    label: String,
    daysUntil: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = when {
                daysUntil == 0 -> Color(0xFFF44336).copy(alpha = 0.2f)
                daysUntil <= 2 -> Color(0xFFFFC107).copy(alpha = 0.2f)
                else -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                when {
                    daysUntil == 0 -> "Today"
                    daysUntil == 1 -> "Tomorrow"
                    else -> "In $daysUntil days"
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    daysUntil == 0 -> Color(0xFFF44336)
                    daysUntil <= 2 -> Color(0xFFFFC107)
                    else -> Color(0xFF4CAF50)
                }
            )
        }
    }
}

@Composable
fun HealthBadge(
    health: Float,
    size: Dp = 24.dp,
    showPercentage: Boolean = true
) {
    val healthPercentage = (health * 100).toInt()
    val color = when {
        health >= 0.75f -> Color(0xFF4CAF50)
        health >= 0.45f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Surface(
        shape = CircleShape,
        color = color,
        modifier = Modifier.size(size)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (showPercentage) {
                Text(
                    "$healthPercentage",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HealthStatusRow(
    metrics: PlantHealthCalculator.HealthMetrics,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            PlantHealthCalculator.getHealthEmoji(metrics.healthStatus),
            style = MaterialTheme.typography.titleMedium
        )

        Column {
            Text(
                metrics.healthStatus.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PlantHealthCalculator.getHealthColor(metrics)
            )
            Text(
                "${(metrics.overallHealth * 100).toInt()}% Health",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HealthPulseIndicator(
    health: Float,
    size: Dp = 16.dp
) {
    val color = when {
        health >= 0.75f -> Color(0xFF4CAF50)
        health >= 0.45f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.3f))
        )

        Box(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .clip(CircleShape)
                .background(color)
        )
    }
}