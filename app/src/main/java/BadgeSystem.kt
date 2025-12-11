package com.example.plantpal

import androidx.compose.ui.graphics.Color

data class UserProgress(
    val userId: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCareDate: Long = 0L,
    val totalPlants: Int = 0,
    val totalWaterings: Int = 0,
    val totalFertilizations: Int = 0,
    val healthyDays: Int = 0,
    val unlockedBadges: List<String> = emptyList(),
    val firstPlantDate: Long = 0L,
    val perfectCareWeeks: Int = 0
)

enum class BadgeCategory {
    STARTER,
    DEDICATION,
    COLLECTION,
    CARE_MASTER,
    SPECIAL
}

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: BadgeCategory,
    val requirement: Int,
    val color: Color,
    val rarity: BadgeRarity
)

enum class BadgeRarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}

object BadgeDefinitions {
    
    val allBadges = listOf(
        Badge(
            id = "first_plant",
            name = "Green Beginner",
            description = "Add your first plant",
            icon = "🌱",
            category = BadgeCategory.STARTER,
            requirement = 1,
            color = Color(0xFF8BC34A),
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "first_water",
            name = "First Drop",
            description = "Water a plant for the first time",
            icon = "💧",
            category = BadgeCategory.STARTER,
            requirement = 1,
            color = Color(0xFF4FC3F7),
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "first_fertilize",
            name = "Plant Food",
            description = "Fertilize a plant for the first time",
            icon = "🌿",
            category = BadgeCategory.STARTER,
            requirement = 1,
            color = Color(0xFF66BB6A),
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "streak_3",
            name = "Getting Started",
            description = "Maintain a 3-day care streak",
            icon = "🔥",
            category = BadgeCategory.DEDICATION,
            requirement = 3,
            color = Color(0xFFFF9800),
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "streak_7",
            name = "Week Warrior",
            description = "Maintain a 7-day care streak",
            icon = "⚡",
            category = BadgeCategory.DEDICATION,
            requirement = 7,
            color = Color(0xFFFF9800),
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "streak_30",
            name = "Monthly Master",
            description = "Maintain a 30-day care streak",
            icon = "🏆",
            category = BadgeCategory.DEDICATION,
            requirement = 30,
            color = Color(0xFFFFD700),
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "streak_100",
            name = "Centurion",
            description = "Maintain a 100-day care streak",
            icon = "👑",
            category = BadgeCategory.DEDICATION,
            requirement = 100,
            color = Color(0xFFFFD700),
            rarity = BadgeRarity.LEGENDARY
        ),
        Badge(
            id = "plants_5",
            name = "Small Garden",
            description = "Collect 5 plants",
            icon = "🪴",
            category = BadgeCategory.COLLECTION,
            requirement = 5,
            color = Color(0xFF4CAF50),
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "plants_10",
            name = "Green Thumb",
            description = "Collect 10 plants",
            icon = "🌳",
            category = BadgeCategory.COLLECTION,
            requirement = 10,
            color = Color(0xFF388E3C),
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "plants_25",
            name = "Botanical Garden",
            description = "Collect 25 plants",
            icon = "🏡",
            category = BadgeCategory.COLLECTION,
            requirement = 25,
            color = Color(0xFF2E7D32),
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "plants_50",
            name = "Plant Empire",
            description = "Collect 50 plants",
            icon = "🌍",
            category = BadgeCategory.COLLECTION,
            requirement = 50,
            color = Color(0xFF1B5E20),
            rarity = BadgeRarity.LEGENDARY
        ),
        Badge(
            id = "waterings_50",
            name = "Hydration Hero",
            description = "Water plants 50 times",
            icon = "💦",
            category = BadgeCategory.CARE_MASTER,
            requirement = 50,
            color = Color(0xFF29B6F6),
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "waterings_200",
            name = "Water Bearer",
            description = "Water plants 200 times",
            icon = "🌊",
            category = BadgeCategory.CARE_MASTER,
            requirement = 200,
            color = Color(0xFF0288D1),
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "fertilizations_25",
            name = "Nutrition Expert",
            description = "Fertilize plants 25 times",
            icon = "🧪",
            category = BadgeCategory.CARE_MASTER,
            requirement = 25,
            color = Color(0xFF8BC34A),
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "fertilizations_100",
            name = "Growth Guru",
            description = "Fertilize plants 100 times",
            icon = "🔬",
            category = BadgeCategory.CARE_MASTER,
            requirement = 100,
            color = Color(0xFF689F38),
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "all_healthy",
            name = "Perfect Health",
            description = "Keep all plants healthy for a week",
            icon = "✨",
            category = BadgeCategory.SPECIAL,
            requirement = 1,
            color = Color(0xFFFFD700),
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "perfect_week",
            name = "Flawless Week",
            description = "Care for plants every day for a week",
            icon = "⭐",
            category = BadgeCategory.SPECIAL,
            requirement = 1,
            color = Color(0xFFFFC107),
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "early_bird",
            name = "Early Bird",
            description = "Water plants before 8 AM 10 times",
            icon = "🌅",
            category = BadgeCategory.SPECIAL,
            requirement = 10,
            color = Color(0xFFFF9800),
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "night_owl",
            name = "Night Gardener",
            description = "Water plants after 8 PM 10 times",
            icon = "🌙",
            category = BadgeCategory.SPECIAL,
            requirement = 10,
            color = Color(0xFF673AB7),
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "diversity",
            name = "Plant Collector",
            description = "Have 5 different plant families",
            icon = "🎨",
            category = BadgeCategory.SPECIAL,
            requirement = 5,
            color = Color(0xFFE91E63),
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "rescuer",
            name = "Plant Rescuer",
            description = "Nurse a critical plant back to health",
            icon = "❤️",
            category = BadgeCategory.SPECIAL,
            requirement = 1,
            color = Color(0xFFF44336),
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "dedicated_month",
            name = "Monthly Devotion",
            description = "Care for plants every day for 30 days",
            icon = "📅",
            category = BadgeCategory.SPECIAL,
            requirement = 30,
            color = Color(0xFF9C27B0),
            rarity = BadgeRarity.LEGENDARY
        )
    )
    
    fun getBadgeById(id: String): Badge? = allBadges.find { it.id == id }
    
    fun getBadgesByCategory(category: BadgeCategory): List<Badge> =
        allBadges.filter { it.category == category }
}

object BadgeChecker {
    
    fun checkUnlockedBadges(
        progress: UserProgress,
        plants: List<PlantProfile>
    ): List<String> {
        val unlockedBadges = mutableListOf<String>()

        if (progress.totalPlants >= 1) unlockedBadges.add("first_plant")
        if (progress.totalWaterings >= 1) unlockedBadges.add("first_water")
        if (progress.totalFertilizations >= 1) unlockedBadges.add("first_fertilize")
        if (progress.longestStreak >= 3) unlockedBadges.add("streak_3")
        if (progress.longestStreak >= 7) unlockedBadges.add("streak_7")
        if (progress.longestStreak >= 30) unlockedBadges.add("streak_30")
        if (progress.longestStreak >= 100) unlockedBadges.add("streak_100")
        if (progress.totalPlants >= 5) unlockedBadges.add("plants_5")
        if (progress.totalPlants >= 10) unlockedBadges.add("plants_10")
        if (progress.totalPlants >= 25) unlockedBadges.add("plants_25")
        if (progress.totalPlants >= 50) unlockedBadges.add("plants_50")
        if (progress.totalWaterings >= 50) unlockedBadges.add("waterings_50")
        if (progress.totalWaterings >= 200) unlockedBadges.add("waterings_200")
        if (progress.totalFertilizations >= 25) unlockedBadges.add("fertilizations_25")
        if (progress.totalFertilizations >= 100) unlockedBadges.add("fertilizations_100")
        if (progress.perfectCareWeeks >= 1) unlockedBadges.add("perfect_week")

        val allHealthy = plants.isNotEmpty() && plants.all { 
            PlantHealthCalculator.calculateHealth(it).overallHealth >= 0.75f 
        }
        if (allHealthy && progress.healthyDays >= 7) {
            unlockedBadges.add("all_healthy")
        }

        val uniqueFamilies = plants.map { it.careInfo.family.lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
            .size
        if (uniqueFamilies >= 5) {
            unlockedBadges.add("diversity")
        }
        
        return unlockedBadges.distinct()
    }
    
    fun getNewlyUnlockedBadges(
        oldProgress: UserProgress,
        newProgress: UserProgress,
        plants: List<PlantProfile>
    ): List<Badge> {
        val oldBadges = oldProgress.unlockedBadges.toSet()
        val newBadges = checkUnlockedBadges(newProgress, plants).toSet()
        
        val newlyUnlocked = newBadges - oldBadges
        return newlyUnlocked.mapNotNull { BadgeDefinitions.getBadgeById(it) }
    }
}
