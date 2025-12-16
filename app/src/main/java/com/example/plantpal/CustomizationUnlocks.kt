package com.example.plantpal

import androidx.compose.ui.graphics.Color

enum class UnlockType {
    POT_STYLE,
    POT_COLOR,
    PLANT_COLOR
}

data class UnlockableItem(
    val id: String,
    val name: String,
    val type: UnlockType,
    val requiredBadge: String,
    val icon: String = "",
    val previewColor: Color? = null
)

object CustomizationUnlocks {
    val defaultUnlocked = listOf(
        "classic",
        "terracotta",
        "green"
    )
    val allUnlockables = listOf(
        UnlockableItem(
            id = "modern",
            name = "Modern Pot",
            type = UnlockType.POT_STYLE,
            requiredBadge = "plants_5",
            icon = "📐"
        ),
        UnlockableItem(
            id = "hanging",
            name = "Hanging Pot",
            type = UnlockType.POT_STYLE,
            requiredBadge = "plants_10",
            icon = "🪴"
        ),
        UnlockableItem(
            id = "ceramic_white",
            name = "White Ceramic",
            type = UnlockType.POT_COLOR,
            requiredBadge = "first_water",
            icon = "⚪",
            previewColor = Color(0xFFF5F5F5)
        ),
        UnlockableItem(
            id = "rustic_brown",
            name = "Rustic Brown",
            type = UnlockType.POT_COLOR,
            requiredBadge = "first_fertilize",
            icon = "🟤",
            previewColor = Color(0xFF8B4513)
        ),
        UnlockableItem(
            id = "ceramic_blue",
            name = "Blue Ceramic",
            type = UnlockType.POT_COLOR,
            requiredBadge = "streak_3",
            icon = "🔵",
            previewColor = Color(0xFF4FC3F7)
        ),
        UnlockableItem(
            id = "ceramic_green",
            name = "Green Ceramic",
            type = UnlockType.POT_COLOR,
            requiredBadge = "waterings_50",
            icon = "🟢",
            previewColor = Color(0xFF66BB6A)
        ),
        UnlockableItem(
            id = "modern_gray",
            name = "Modern Gray",
            type = UnlockType.POT_COLOR,
            requiredBadge = "streak_7",
            icon = "⚫",
            previewColor = Color(0xFF757575)
        ),
        UnlockableItem(
            id = "pink",
            name = "Pretty Pink",
            type = UnlockType.POT_COLOR,
            requiredBadge = "plants_25",
            icon = "🌸",
            previewColor = Color(0xFFE91E63)
        ),
        UnlockableItem(
            id = "yellow",
            name = "Sunny Yellow",
            type = UnlockType.POT_COLOR,
            requiredBadge = "perfect_week",
            icon = "🌟",
            previewColor = Color(0xFFFFEB3B)
        ),
        UnlockableItem(
            id = "purple",
            name = "Royal Purple",
            type = UnlockType.POT_COLOR,
            requiredBadge = "diversity",
            icon = "💜",
            previewColor = Color(0xFF9C27B0)
        ),
        UnlockableItem(
            id = "light_green",
            name = "Light Green",
            type = UnlockType.PLANT_COLOR,
            requiredBadge = "first_plant",
            icon = "🌿",
            previewColor = Color(0xFF8BC34A)
        ),
        UnlockableItem(
            id = "dark_green",
            name = "Dark Green",
            type = UnlockType.PLANT_COLOR,
            requiredBadge = "streak_3",
            icon = "🌲",
            previewColor = Color(0xFF2E7D32)
        ),
        UnlockableItem(
            id = "pink",
            name = "Pink Plant",
            type = UnlockType.PLANT_COLOR,
            requiredBadge = "plants_10",
            icon = "🌺",
            previewColor = Color(0xFFE91E63)
        ),
        UnlockableItem(
            id = "red",
            name = "Red Plant",
            type = UnlockType.PLANT_COLOR,
            requiredBadge = "fertilizations_25",
            icon = "🌹",
            previewColor = Color(0xFFF44336)
        ),
        UnlockableItem(
            id = "orange",
            name = "Orange Plant",
            type = UnlockType.PLANT_COLOR,
            requiredBadge = "waterings_200",
            icon = "🧡",
            previewColor = Color(0xFFFF9800)
        ),
        UnlockableItem(
            id = "yellow",
            name = "Golden Plant",
            type = UnlockType.PLANT_COLOR,
            requiredBadge = "streak_30",
            icon = "✨",
            previewColor = Color(0xFFFFD700)
        ),
        UnlockableItem(
            id = "brown",
            name = "Autumn Brown",
            type = UnlockType.PLANT_COLOR,
            requiredBadge = "all_healthy",
            icon = "🍂",
            previewColor = Color(0xFF8D6E63)
        )
    )
    fun getUnlockablesByType(type: UnlockType): List<UnlockableItem> {
        return allUnlockables.filter { it.type == type }
    }
    fun isUnlocked(itemId: String, unlockedBadges: List<String>): Boolean {
        if (itemId in defaultUnlocked) return true
        val item = allUnlockables.find { it.id == itemId } ?: return false
        return item.requiredBadge in unlockedBadges
    }
    fun getUnlockedItems(unlockedBadges: List<String>): List<UnlockableItem> {
        return allUnlockables.filter { isUnlocked(it.id, unlockedBadges) }
    }
    fun getUnlockedItemsByType(type: UnlockType, unlockedBadges: List<String>): List<UnlockableItem> {
        return getUnlockedItems(unlockedBadges).filter { it.type == type }
    }
    fun getRequiredBadge(itemId: String): Badge? {
        val item = allUnlockables.find { it.id == itemId } ?: return null
        return BadgeDefinitions.getBadgeById(item.requiredBadge)
    }
    fun getUnlockProgress(itemId: String, progress: UserProgress): Float {
        val item = allUnlockables.find { it.id == itemId } ?: return 0f
        val badge = BadgeDefinitions.getBadgeById(item.requiredBadge) ?: return 0f
        
        val currentValue = when (badge.id) {
            "first_plant", "first_water", "first_fertilize" -> if (progress.totalPlants > 0) 1 else 0
            "streak_3", "streak_7", "streak_30", "streak_100" -> progress.longestStreak
            "plants_5", "plants_10", "plants_25", "plants_50" -> progress.totalPlants
            "waterings_50", "waterings_200" -> progress.totalWaterings
            "fertilizations_25", "fertilizations_100" -> progress.totalFertilizations
            "perfect_week" -> progress.perfectCareWeeks
            "all_healthy" -> progress.healthyDays
            else -> 0
        }
        
        return (currentValue.toFloat() / badge.requirement.toFloat()).coerceIn(0f, 1f)
    }
    fun getAllPotStyles(): List<Pair<String, String>> {
        return listOf(
            "classic" to "Classic",
            "modern" to "Modern",
            "hanging" to "Hanging"
        )
    }
    fun getAllPotColors(): List<Pair<String, String>> {
        return listOf(
            "terracotta" to "Terracotta",
            "ceramic_white" to "White Ceramic",
            "ceramic_blue" to "Blue Ceramic",
            "ceramic_green" to "Green Ceramic",
            "modern_gray" to "Modern Gray",
            "rustic_brown" to "Rustic Brown",
            "pink" to "Pink",
            "yellow" to "Yellow",
            "purple" to "Purple"
        )
    }
    fun getAllPlantColors(): List<Pair<String, String>> {
        return listOf(
            "green" to "Green",
            "light_green" to "Light Green",
            "dark_green" to "Dark Green",
            "pink" to "Pink",
            "red" to "Red",
            "orange" to "Orange",
            "yellow" to "Yellow",
            "brown" to "Brown"
        )
    }
}
