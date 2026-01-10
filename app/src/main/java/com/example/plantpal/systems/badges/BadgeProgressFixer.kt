package com.example.plantpal

import android.util.Log
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.Badge
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.BadgeChecker
import com.example.plantpal.com.example.plantpal.systems.badges.com.example.plantpal.systems.badges.BadgeDefinitions
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.PlantRepository
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BadgeProgressFixer {
    private const val TAG = "BadgeProgressFixer"

    suspend fun refreshBadgeProgress(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting badge progress refresh...")
                
                val plantRepo = PlantRepository()
                val progressRepo = ProgressRepository()

                val plantsResult = plantRepo.getAllPlants()
                if (plantsResult.isFailure) {
                    val error = "Failed to load plants: ${plantsResult.exceptionOrNull()?.message}"
                    Log.e(TAG, error)
                    return@withContext Result.failure(Exception(error))
                }
                
                val plants = plantsResult.getOrNull()!!
                Log.d(TAG, "Found ${plants.size} plants")

                val progressResult = progressRepo.getUserProgress()
                if (progressResult.isFailure) {
                    val error = "Failed to load progress: ${progressResult.exceptionOrNull()?.message}"
                    Log.e(TAG, error)
                    return@withContext Result.failure(Exception(error))
                }
                
                val currentProgress = progressResult.getOrNull()!!
                Log.d(TAG, "Current progress - Plants: ${currentProgress.totalPlants}, Badges: ${currentProgress.unlockedBadges.size}")

                val updatedProgress = currentProgress.copy(
                    totalPlants = plants.size,
                    unlockedBadges = BadgeChecker.checkUnlockedBadges(
                        currentProgress.copy(totalPlants = plants.size),
                        plants
                    )
                )

                progressRepo.updateProgress(updatedProgress)
                
                val newBadges = updatedProgress.unlockedBadges.size - currentProgress.unlockedBadges.size
                val message = """
                    ✅ Badge progress refreshed!
                    
                    Plants: ${currentProgress.totalPlants} → ${updatedProgress.totalPlants}
                    Badges: ${currentProgress.unlockedBadges.size} → ${updatedProgress.unlockedBadges.size}
                    
                    ${if (newBadges > 0) "🎉 Unlocked $newBadges new badge(s)!" else ""}
                """.trimIndent()
                
                Log.d(TAG, message)
                Result.success(message)
                
            } catch (e: Exception) {
                val error = "Error refreshing badges: ${e.message}"
                Log.e(TAG, error, e)
                Result.failure(Exception(error))
            }
        }
    }

    suspend fun previewBadgeChanges(): Result<BadgePreview> {
        return withContext(Dispatchers.IO) {
            try {
                val plantRepo = PlantRepository()
                val progressRepo = ProgressRepository()
                
                val plants = plantRepo.getAllPlants().getOrNull() ?: emptyList()
                val progress = progressRepo.getUserProgress().getOrNull() 
                    ?: return@withContext Result.failure(Exception("No progress found"))
                
                val currentBadgeIds = progress.unlockedBadges.toSet()
                val shouldHaveBadgeIds = BadgeChecker.checkUnlockedBadges(
                    progress.copy(totalPlants = plants.size),
                    plants
                ).toSet()
                
                val missingBadgeIds = shouldHaveBadgeIds - currentBadgeIds
                val missingBadges = missingBadgeIds.mapNotNull { 
                    BadgeDefinitions.getBadgeById(it)
                }
                
                Result.success(
                    BadgePreview(
                        currentPlantCount = plants.size,
                        progressPlantCount = progress.totalPlants,
                        currentBadgeCount = currentBadgeIds.size,
                        shouldHaveBadgeCount = shouldHaveBadgeIds.size,
                        missingBadges = missingBadges
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

data class BadgePreview(
    val currentPlantCount: Int,
    val progressPlantCount: Int,
    val currentBadgeCount: Int,
    val shouldHaveBadgeCount: Int,
    val missingBadges: List<Badge>
)
