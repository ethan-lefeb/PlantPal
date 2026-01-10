package com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers

import androidx.compose.ui.graphics.Color
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import kotlin.math.max
import kotlin.math.min

object PlantHealthCalculator {

    data class HealthMetrics(
        val overallHealth: Float,
        val hydrationLevel: Float,
        val nutritionLevel: Float,
        val careConsistency: Float,
        val healthStatus: String,
        val primaryConcern: String?,
        val daysUntilWaterNeeded: Int,
        val daysUntilFertilizerNeeded: Int
    )

    fun calculateHealth(plant: PlantProfile): HealthMetrics {
        val now = System.currentTimeMillis()
        val daysSinceWatering = if (plant.lastWatered > 0) {
            ((now - plant.lastWatered) / (1000 * 60 * 60 * 24)).toInt()
        } else {
            0
        }
        
        val daysSinceFertilizing = if (plant.lastFertilized > 0) {
            ((now - plant.lastFertilized) / (1000 * 60 * 60 * 24)).toInt()
        } else {
            0
        }
        
        val daysSinceCreation = ((now - plant.createdAt) / (1000 * 60 * 60 * 24)).toInt()
        val hydration = calculateHydration(
            daysSinceWatering,
            plant.wateringFrequency,
            plant.careInfo.droughtTolerant
        )
        
        val nutrition = calculateNutrition(
            daysSinceFertilizing,
            plant.fertilizerFrequency
        )
        
        val consistency = calculateCareConsistency(
            daysSinceCreation,
            daysSinceWatering,
            daysSinceFertilizing,
            plant.wateringFrequency,
            plant.fertilizerFrequency
        )

        val overall = (
            hydration * 0.60f +
            nutrition * 0.25f +
            consistency * 0.15f
        ).coerceIn(0f, 1f)

        val status = when {
            overall >= 0.75f -> "healthy"
            overall >= 0.45f -> "warning"
            else -> "critical"
        }

        val concern = when {
            hydration < 0.3f -> "Needs water urgently!"
            nutrition < 0.3f && daysSinceCreation > 30 -> "Needs fertilizer"
            hydration < 0.6f -> "Could use some water soon"
            nutrition < 0.6f -> "Consider fertilizing"
            consistency < 0.5f -> "Irregular care schedule"
            else -> null
        }

        val daysUntilWater = max(0, plant.wateringFrequency - daysSinceWatering)
        val daysUntilFertilizer = max(0, plant.fertilizerFrequency - daysSinceFertilizing)
        
        return HealthMetrics(
            overallHealth = overall,
            hydrationLevel = hydration,
            nutritionLevel = nutrition,
            careConsistency = consistency,
            healthStatus = status,
            primaryConcern = concern,
            daysUntilWaterNeeded = daysUntilWater,
            daysUntilFertilizerNeeded = daysUntilFertilizer
        )
    }

    private fun calculateHydration(
        daysSinceWatering: Int,
        wateringFrequency: Int,
        isDroughtTolerant: Boolean
    ): Float {
        val tolerance = if (isDroughtTolerant) 1.5f else 1.0f
        
        return when {
            daysSinceWatering == 0 -> 1.0f
            daysSinceWatering <= wateringFrequency -> {
                1.0f - (daysSinceWatering.toFloat() / wateringFrequency.toFloat() * 0.3f)
            }
            daysSinceWatering <= (wateringFrequency * tolerance).toInt() -> {
                0.7f - ((daysSinceWatering - wateringFrequency).toFloat() / wateringFrequency.toFloat() * 0.3f)
            }
            daysSinceWatering <= (wateringFrequency * tolerance * 1.5f).toInt() -> {
                0.4f - ((daysSinceWatering - wateringFrequency * tolerance) / (wateringFrequency * tolerance) * 0.2f)
            }
            else -> {
                max(0.1f, 0.2f - (daysSinceWatering - wateringFrequency * 2) * 0.01f)
            }
        }
    }

    private fun calculateNutrition(
        daysSinceFertilizing: Int,
        fertilizerFrequency: Int
    ): Float {
        return when {
            daysSinceFertilizing == 0 -> 1.0f
            daysSinceFertilizing <= fertilizerFrequency -> {
                1.0f - (daysSinceFertilizing.toFloat() / fertilizerFrequency.toFloat() * 0.2f)
            }
            daysSinceFertilizing <= fertilizerFrequency * 1.5f -> {
                0.8f - ((daysSinceFertilizing - fertilizerFrequency).toFloat() / fertilizerFrequency.toFloat() * 0.2f)
            }
            daysSinceFertilizing <= fertilizerFrequency * 2 -> {
                0.6f - ((daysSinceFertilizing - fertilizerFrequency * 1.5f) / (fertilizerFrequency * 0.5f) * 0.2f)
            }
            else -> {
                max(0.3f, 0.4f - (daysSinceFertilizing - fertilizerFrequency * 2) * 0.005f)
            }
        }
    }

    private fun calculateCareConsistency(
        daysSinceCreation: Int,
        daysSinceWatering: Int,
        daysSinceFertilizing: Int,
        wateringFrequency: Int,
        fertilizerFrequency: Int
    ): Float {
        if (daysSinceCreation < 7) {
            return 1.0f
        }

        val expectedWaterings = daysSinceCreation / wateringFrequency
        val wateringDelay = max(0f, (daysSinceWatering - wateringFrequency).toFloat() / wateringFrequency.toFloat())
        val fertilizerDelay = max(0f, (daysSinceFertilizing - fertilizerFrequency).toFloat() / fertilizerFrequency.toFloat())
        val consistencyScore = 1.0f - min(1.0f, (wateringDelay * 0.4f + fertilizerDelay * 0.2f))
        
        return max(0.1f, consistencyScore)
    }

    fun getHealthColor(healthMetrics: HealthMetrics): Color {
        return when {
            healthMetrics.overallHealth >= 0.75f -> Color(0xFF4CAF50)
            healthMetrics.overallHealth >= 0.45f -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
    }

    fun getHealthAdjustedColor(baseColor: Color, health: Float): Color {
        return when {
            health > 0.8f -> baseColor
            health > 0.6f -> Color(
                red = baseColor.red,
                green = baseColor.green * 0.95f,
                blue = baseColor.blue * 0.95f
            )
            health > 0.4f -> Color(
                red = baseColor.red * 0.85f,
                green = baseColor.green * 0.85f,
                blue = baseColor.blue * 0.80f
            )
            else -> Color(
                red = baseColor.red * 0.65f,
                green = baseColor.green * 0.65f,
                blue = baseColor.blue * 0.60f
            )
        }
    }

    fun getHealthEmoji(healthStatus: String): String {
        return when (healthStatus) {
            "healthy" -> "😊"
            "warning" -> "😐"
            "critical" -> "😢"
            else -> "🌱"
        }
    }

    fun getHealthDescription(healthMetrics: HealthMetrics): String {
        return when {
            healthMetrics.overallHealth >= 0.9f -> "Thriving! Your plant is in excellent condition."
            healthMetrics.overallHealth >= 0.75f -> "Healthy and happy! Keep up the good care."
            healthMetrics.overallHealth >= 0.6f -> "Doing okay, but could use a little attention."
            healthMetrics.overallHealth >= 0.45f -> "Needs care soon. ${healthMetrics.primaryConcern ?: "Check watering."}"
            healthMetrics.overallHealth >= 0.3f -> "In rough shape. ${healthMetrics.primaryConcern ?: "Urgent care needed!"}"
            else -> "Critical condition! Immediate attention required."
        }
    }

    fun getCareUrgency(healthMetrics: HealthMetrics): Int {
        return when {
            healthMetrics.hydrationLevel < 0.3f -> 3
            healthMetrics.hydrationLevel < 0.5f -> 2
            healthMetrics.hydrationLevel < 0.7f -> 1
            healthMetrics.nutritionLevel < 0.4f -> 1
            else -> 0
        }
    }

    fun getRecommendedAction(healthMetrics: HealthMetrics, plant: PlantProfile): String {
        return when {
            healthMetrics.hydrationLevel < 0.3f -> 
                "💧 Water immediately! Your plant is very thirsty."
            
            healthMetrics.hydrationLevel < 0.6f && healthMetrics.daysUntilWaterNeeded <= 1 ->
                "💧 Water within the next day or two."
            
            healthMetrics.nutritionLevel < 0.4f && healthMetrics.hydrationLevel > 0.6f ->
                "🌿 Consider fertilizing to boost nutrition."
            
            healthMetrics.overallHealth < 0.5f ->
                "⚠️ ${healthMetrics.primaryConcern ?: "Check your care routine."}"
            
            healthMetrics.daysUntilWaterNeeded <= 2 ->
                "💧 Next watering in ${healthMetrics.daysUntilWaterNeeded} day${if (healthMetrics.daysUntilWaterNeeded != 1) "s" else ""}."
            
            healthMetrics.overallHealth >= 0.9f ->
                "✨ Your plant is thriving! Keep doing what you're doing."
            
            else ->
                "✅ Your plant is healthy. Next water in ${healthMetrics.daysUntilWaterNeeded} days."
        }
    }

    fun needsAttention(plant: PlantProfile): Boolean {
        val metrics = calculateHealth(plant)
        return metrics.overallHealth < 0.6f || metrics.hydrationLevel < 0.5f
    }

    fun getHealthPercentage(plant: PlantProfile): Int {
        val metrics = calculateHealth(plant)
        return (metrics.overallHealth * 100f).toInt()
    }
}
