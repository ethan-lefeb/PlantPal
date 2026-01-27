package com.example.plantpal.com.example.plantpal.systems.helpers

import android.util.Log
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.CareActionType
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.ProgressRepository
import com.example.plantpal.systems.social.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlantRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val progressRepo = ProgressRepository()
    private val activityRepo = ActivityRepository()

    private fun getPlantsCollection() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("plants")

    private fun requireUid(): String? = auth.currentUser?.uid

    private suspend fun safePlantName(plantId: String): String? {
        return try {
            val snap = getPlantsCollection().document(plantId).get().await()
            snap.toObject(PlantProfile::class.java)?.commonName
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun safeLogActivity(
        type: String,
        text: String,
        plantId: String? = null,
        plantName: String? = null
    ) {
        try {
            activityRepo.log(
                type = type,
                text = text,
                plantId = plantId,
                plantName = plantName
            ).onFailure { e ->
                Log.w("PlantRepo", "Activity log failed ($type)", e)
            }
        } catch (e: Exception) {
            Log.w("PlantRepo", "Activity log threw ($type)", e)
        }
    }

    suspend fun addPlant(plant: PlantProfile): Result<String> {
        return try {
            val userId = requireUid()
            Log.d("PlantRepo", "Current user ID: $userId")

            if (userId == null) {
                Log.e("PlantRepo", "User not authenticated!")
                return Result.failure(Exception("User not authenticated"))
            }

            val docRef = getPlantsCollection().document()
            Log.d("PlantRepo", "Created doc ref: ${docRef.id}")

            val plantWithIds = plant.copy(
                plantId = docRef.id,
                userId = userId
            )

            Log.d("PlantRepo", "Saving plant to Firestore: $plantWithIds")
            docRef.set(plantWithIds).await()

            safeLogActivity(
                type = "plant_added",
                text = "Added ${plantWithIds.commonName}",
                plantId = plantWithIds.plantId,
                plantName = plantWithIds.commonName
            )

            Log.d("PlantRepo", "✅ Plant saved successfully!")

            Log.d("PlantRepo", "Updating progress for new plant...")
            val allPlants = getAllPlants().getOrNull() ?: emptyList()
            progressRepo.initializeProgress(allPlants)
            Log.d("PlantRepo", "✅ Progress updated! Badge check complete.")

            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error saving plant", e)
            Result.failure(e)
        }
    }

    suspend fun getAllPlants(): Result<List<PlantProfile>> {
        return try {
            val userId = requireUid()
            Log.d("PlantRepo", "Getting all plants for user: $userId")

            if (userId == null) {
                Log.e("PlantRepo", "User not authenticated!")
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = getPlantsCollection().get().await()
            val plants = snapshot.documents.mapNotNull { it.toObject(PlantProfile::class.java) }

            Log.d("PlantRepo", "Retrieved ${plants.size} plants")
            Result.success(plants)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error fetching plants", e)
            Result.failure(e)
        }
    }

    suspend fun getPlant(plantId: String): Result<PlantProfile> {
        return try {
            Log.d("PlantRepo", "Getting plant: $plantId")
            val snapshot = getPlantsCollection().document(plantId).get().await()
            val plant = snapshot.toObject(PlantProfile::class.java)
                ?: return Result.failure(Exception("Plant not found"))

            Log.d("PlantRepo", "✅ Retrieved plant: ${plant.commonName}")
            Result.success(plant)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error fetching plant", e)
            Result.failure(e)
        }
    }

    suspend fun updatePlant(plant: PlantProfile): Result<Unit> {
        return try {
            if (plant.plantId.isEmpty()) {
                Log.e("PlantRepo", "Cannot update plant - no ID provided")
                return Result.failure(Exception("Plant ID is required"))
            }

            Log.d("PlantRepo", "Updating plant: ${plant.plantId}")
            getPlantsCollection().document(plant.plantId).set(plant).await()

            safeLogActivity(
                type = "plant_updated",
                text = "Updated ${plant.commonName}",
                plantId = plant.plantId,
                plantName = plant.commonName
            )

            Log.d("PlantRepo", "✅ Plant updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error updating plant", e)
            Result.failure(e)
        }
    }

    suspend fun deletePlant(plantId: String): Result<Unit> {
        return try {
            val name = safePlantName(plantId)

            Log.d("PlantRepo", "Deleting plant: $plantId")
            getPlantsCollection().document(plantId).delete().await()

            safeLogActivity(
                type = "plant_deleted",
                text = if (name.isNullOrBlank()) "Deleted a plant" else "Deleted $name",
                plantId = plantId,
                plantName = name
            )

            Log.d("PlantRepo", "✅ Plant deleted successfully")

            Log.d("PlantRepo", "Updating progress after deletion...")
            val allPlants = getAllPlants().getOrNull() ?: emptyList()
            progressRepo.initializeProgress(allPlants)
            Log.d("PlantRepo", "✅ Progress updated!")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error deleting plant", e)
            Result.failure(e)
        }
    }

    suspend fun waterPlant(plantId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            Log.d("PlantRepo", "Watering plant: $plantId at $currentTime")

            getPlantsCollection().document(plantId)
                .update("lastWatered", currentTime)
                .await()

            val name = safePlantName(plantId)
            safeLogActivity(
                type = "watered",
                text = if (name.isNullOrBlank()) "Watered a plant" else "Watered $name",
                plantId = plantId,
                plantName = name
            )

            Log.d("PlantRepo", "✅ Plant watered successfully")

            val allPlants = getAllPlants().getOrNull() ?: emptyList()
            progressRepo.recordCareAction(CareActionType.WATER, allPlants)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error watering plant", e)
            Result.failure(e)
        }
    }

    suspend fun fertilizePlant(plantId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            Log.d("PlantRepo", "Fertilizing plant: $plantId at $currentTime")

            getPlantsCollection().document(plantId)
                .update("lastFertilized", currentTime)
                .await()

            val name = safePlantName(plantId)
            safeLogActivity(
                type = "fertilized",
                text = if (name.isNullOrBlank()) "Fertilized a plant" else "Fertilized $name",
                plantId = plantId,
                plantName = name
            )

            Log.d("PlantRepo", "✅ Plant fertilized successfully")

            val allPlants = getAllPlants().getOrNull() ?: emptyList()
            progressRepo.recordCareAction(CareActionType.FERTILIZE, allPlants)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error fertilizing plant", e)
            Result.failure(e)
        }
    }

    suspend fun updateHealthStatus(plantId: String, health: String): Result<Unit> {
        return try {
            Log.d("PlantRepo", "Updating health status for plant: $plantId to $health")

            getPlantsCollection().document(plantId)
                .update("health", health)
                .await()

            Log.d("PlantRepo", "✅ Health status updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error updating health status", e)
            Result.failure(e)
        }
    }
}
