package com.example.plantpal

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AvatarDebugUtils {
    private const val TAG = "AvatarDebug"

    suspend fun regenerateAllAvatars(repository: PlantRepository, forceAll: Boolean = false): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val plants = repository.getAllPlants().getOrNull() ?: emptyList()
                var updatedCount = 0
                
                Log.d(TAG, "Starting avatar regeneration for ${plants.size} plants (forceAll=$forceAll)")
                
                plants.forEach { plant ->
                    Log.d(TAG, "Checking plant: ${plant.commonName}")
                    Log.d(TAG, "  Current avatar config: baseType=${plant.avatarConfig.baseType}, color=${plant.avatarConfig.color}")
                    Log.d(TAG, "  Plant family: ${plant.careInfo.family}, genus: ${plant.careInfo.genus}")

                    val shouldRegenerate = forceAll || 
                        plant.avatarConfig.baseType.isEmpty() || 
                        (plant.avatarConfig.baseType == "generic" && plant.careInfo.family.isNotEmpty())
                    
                    if (shouldRegenerate) {
                        val newConfig = AvatarGenerator.generateAvatarForPlant(
                            family = plant.careInfo.family,
                            genus = plant.careInfo.genus,
                            commonName = plant.commonName,
                            scientificName = plant.scientificName
                        )
                        
                        Log.d(TAG, "  Generated new avatar: baseType=${newConfig.baseType}, color=${newConfig.color}")
                        
                        val updated = plant.copy(avatarConfig = newConfig)
                        val result = repository.updatePlant(updated)
                        
                        if (result.isSuccess) {
                            updatedCount++
                            Log.d(TAG, "  ✅ Updated successfully")
                        } else {
                            Log.e(TAG, "  ❌ Failed to update: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.d(TAG, "  ⏭️  Skipping - avatar already set")
                    }
                }
                
                Log.d(TAG, "Avatar regeneration complete. Updated $updatedCount plants.")
                Result.success(updatedCount)
            } catch (e: Exception) {
                Log.e(TAG, "Error during avatar regeneration", e)
                Result.failure(e)
            }
        }
    }

    fun logPlantAvatarInfo(plant: PlantProfile) {
        Log.d(TAG, """
            Plant Avatar Debug Info:
            ========================
            Plant Name: ${plant.commonName}
            Scientific Name: ${plant.scientificName}
            
            Care Info:
              Family: ${plant.careInfo.family}
              Genus: ${plant.careInfo.genus}
              
            Avatar Config:
              Base Type: ${plant.avatarConfig.baseType}
              Color: ${plant.avatarConfig.color}
            
            Expected Avatar Type: ${determineExpectedType(plant)}
            ========================
        """.trimIndent())
    }
    
    private fun determineExpectedType(plant: PlantProfile): String {
        val family = plant.careInfo.family.lowercase()
        return when {
            family.contains("cactaceae") -> "cactus"
            family.contains("crassulaceae") -> "succulent"
            family.contains("polypodiaceae") -> "fern"
            family.contains("orchidaceae") -> "flower"
            family.contains("lamiaceae") -> "herb"
            family.contains("fagaceae") || family.contains("pinaceae") -> "tree"
            else -> "generic (family: ${plant.careInfo.family})"
        }
    }

    fun testAvatarGeneration(
        family: String,
        genus: String,
        commonName: String,
        scientificName: String
    ): AvatarConfig {
        Log.d(TAG, "Testing avatar generation for:")
        Log.d(TAG, "  Family: $family")
        Log.d(TAG, "  Genus: $genus")
        Log.d(TAG, "  Common: $commonName")
        
        val config = AvatarGenerator.generateAvatarForPlant(
            family = family,
            genus = genus,
            commonName = commonName,
            scientificName = scientificName
        )
        
        Log.d(TAG, "Generated avatar:")
        Log.d(TAG, "  Base Type: ${config.baseType}")
        Log.d(TAG, "  Color: ${config.color}")
        
        return config
    }
}
