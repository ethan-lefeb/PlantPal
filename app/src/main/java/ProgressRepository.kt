package com.example.plantpal

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProgressRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "ProgressRepo"
    }
    
    private fun getProgressDocument() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("progress")
            .document("main")

    suspend fun getUserProgress(): Result<UserProgress> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val doc = getProgressDocument().get().await()
            val progress = doc.toObject(UserProgress::class.java) 
                ?: UserProgress(userId = userId)
            
            Result.success(progress)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting progress", e)
            Result.failure(e)
        }
    }

    suspend fun updateProgress(progress: UserProgress): Result<Unit> {
        return try {
            getProgressDocument().set(progress).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress", e)
            Result.failure(e)
        }
    }

    suspend fun recordCareAction(
        actionType: CareActionType,
        plants: List<PlantProfile>
    ): Result<BadgeUpdateResult> {
        return try {
            val progressResult = getUserProgress()
            if (progressResult.isFailure) {
                return Result.failure(progressResult.exceptionOrNull()!!)
            }
            
            val oldProgress = progressResult.getOrNull()!!
            val now = System.currentTimeMillis()

            var newProgress = when (actionType) {
                CareActionType.WATER -> oldProgress.copy(
                    totalWaterings = oldProgress.totalWaterings + 1
                )
                CareActionType.FERTILIZE -> oldProgress.copy(
                    totalFertilizations = oldProgress.totalFertilizations + 1
                )
            }

            val streakUpdate = calculateSmartStreak(oldProgress, plants, now)
            newProgress = newProgress.copy(
                currentStreak = streakUpdate.newStreak,
                longestStreak = streakUpdate.newLongestStreak,
                lastCareDate = now,
                totalPlants = plants.size
            )

            val newBadges = BadgeChecker.checkUnlockedBadges(newProgress, plants)
            newProgress = newProgress.copy(unlockedBadges = newBadges)
            
            val newlyUnlocked = BadgeChecker.getNewlyUnlockedBadges(
                oldProgress, 
                newProgress, 
                plants
            )

            updateProgress(newProgress).getOrThrow()
            
            Result.success(
                BadgeUpdateResult(
                    newProgress = newProgress,
                    newlyUnlockedBadges = newlyUnlocked,
                    streakIncreased = newProgress.currentStreak > oldProgress.currentStreak,
                    streakBroken = streakUpdate.streakBroken
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error recording care action", e)
            Result.failure(e)
        }
    }

    suspend fun checkStreakStatus(plants: List<PlantProfile>): Result<StreakStatus> {
        return try {
            val progressResult = getUserProgress()
            if (progressResult.isFailure) {
                return Result.failure(progressResult.exceptionOrNull()!!)
            }
            
            val progress = progressResult.getOrNull()!!
            val now = System.currentTimeMillis()
            
            val streakUpdate = calculateSmartStreak(progress, plants, now)

            if (streakUpdate.newStreak != progress.currentStreak) {
                val newProgress = progress.copy(
                    currentStreak = streakUpdate.newStreak,
                    longestStreak = streakUpdate.newLongestStreak,
                    lastCareDate = now
                )
                updateProgress(newProgress).getOrThrow()
            }
            
            Result.success(
                StreakStatus(
                    currentStreak = streakUpdate.newStreak,
                    hasOverduePlants = streakUpdate.hasOverduePlants,
                    streakBroken = streakUpdate.streakBroken
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking streak status", e)
            Result.failure(e)
        }
    }

    private data class StreakUpdate(
        val newStreak: Int,
        val newLongestStreak: Int,
        val hasOverduePlants: Boolean,
        val streakBroken: Boolean
    )
    
    private fun calculateSmartStreak(
        progress: UserProgress,
        plants: List<PlantProfile>,
        currentTime: Long
    ): StreakUpdate {

        if (plants.isEmpty()) {
            return StreakUpdate(
                newStreak = progress.currentStreak,
                newLongestStreak = progress.longestStreak,
                hasOverduePlants = false,
                streakBroken = false
            )
        }

        val overduePlants = plants.filter { plant ->
            val daysSinceWatering = if (plant.lastWatered > 0) {
                ((currentTime - plant.lastWatered) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }
            daysSinceWatering > plant.wateringFrequency
        }
        
        val hasOverduePlants = overduePlants.isNotEmpty()

        if (progress.lastCareDate == 0L) {
            return if (hasOverduePlants) {
                StreakUpdate(0, 0, true, false)
            } else {
                StreakUpdate(1, 1, false, false)
            }
        }
        
        val daysSinceLastCheck = ((currentTime - progress.lastCareDate) / (1000 * 60 * 60 * 24)).toInt()
        
        return when {
            hasOverduePlants -> {
                val wasBroken = progress.currentStreak > 0
                StreakUpdate(
                    newStreak = 0,
                    newLongestStreak = progress.longestStreak,
                    hasOverduePlants = true,
                    streakBroken = wasBroken
                )
            }
            
            daysSinceLastCheck == 0 -> {
                StreakUpdate(
                    newStreak = progress.currentStreak,
                    newLongestStreak = progress.longestStreak,
                    hasOverduePlants = false,
                    streakBroken = false
                )
            }
            
            else -> {
                val newStreak = if (progress.currentStreak == 0) {
                    1
                } else {
                    progress.currentStreak + daysSinceLastCheck
                }
                
                StreakUpdate(
                    newStreak = newStreak,
                    newLongestStreak = maxOf(newStreak, progress.longestStreak),
                    hasOverduePlants = false,
                    streakBroken = false
                )
            }
        }
    }

    suspend fun initializeProgress(plants: List<PlantProfile>): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid 
                ?: return Result.failure(Exception("Not authenticated"))
            
            val progressResult = getUserProgress()
            val currentProgress = progressResult.getOrNull() 
                ?: UserProgress(userId = userId)
            
            val now = System.currentTimeMillis()
            val newProgress = currentProgress.copy(
                totalPlants = plants.size,
                firstPlantDate = if (currentProgress.firstPlantDate == 0L) now 
                                 else currentProgress.firstPlantDate,
                currentStreak = if (currentProgress.currentStreak == 0) 1
                               else currentProgress.currentStreak,
                longestStreak = if (currentProgress.longestStreak == 0) 1
                               else currentProgress.longestStreak,
                lastCareDate = now,
                unlockedBadges = BadgeChecker.checkUnlockedBadges(currentProgress, plants)
            )
            
            updateProgress(newProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDailyHealth(plants: List<PlantProfile>): Result<Unit> {
        return try {
            val progressResult = getUserProgress()
            if (progressResult.isFailure) return Result.success(Unit)
            
            val progress = progressResult.getOrNull()!!

            val allHealthy = plants.isNotEmpty() && plants.all {
                PlantHealthCalculator.calculateHealth(it).overallHealth >= 0.75f
            }
            
            val newHealthyDays = if (allHealthy) {
                progress.healthyDays + 1
            } else {
                0
            }
            
            val newProgress = progress.copy(
                healthyDays = newHealthyDays,
                unlockedBadges = BadgeChecker.checkUnlockedBadges(
                    progress.copy(healthyDays = newHealthyDays),
                    plants
                )
            )
            
            updateProgress(newProgress)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating daily health", e)
            Result.failure(e)
        }
    }

    suspend fun checkPerfectWeek(plants: List<PlantProfile>): Result<Unit> {
        return try {
            val progressResult = getUserProgress()
            if (progressResult.isFailure) return Result.success(Unit)
            
            val progress = progressResult.getOrNull()!!

            if (progress.currentStreak >= 7) {
                val newProgress = progress.copy(
                    perfectCareWeeks = progress.perfectCareWeeks + 1,
                    unlockedBadges = BadgeChecker.checkUnlockedBadges(
                        progress.copy(perfectCareWeeks = progress.perfectCareWeeks + 1),
                        plants
                    )
                )
                updateProgress(newProgress)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

enum class CareActionType {
    WATER,
    FERTILIZE
}

data class BadgeUpdateResult(
    val newProgress: UserProgress,
    val newlyUnlockedBadges: List<Badge>,
    val streakIncreased: Boolean,
    val streakBroken: Boolean = false
)

data class StreakStatus(
    val currentStreak: Int,
    val hasOverduePlants: Boolean,
    val streakBroken: Boolean
)
