package com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers

import android.util.Log
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlantRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val progressRepo = ProgressRepository()

    private fun getPlantsCollection() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("plants")

    // create a new plant
    suspend fun addPlant(plant: PlantProfile): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
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
            val userId = auth.currentUser?.uid
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

            Log.d("PlantRepo", "✅ Plant updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error updating plant", e)
            Result.failure(e)
        }
    }

    suspend fun deletePlant(plantId: String): Result<Unit> {
        return try {
            Log.d("PlantRepo", "Deleting plant: $plantId")
            getPlantsCollection().document(plantId).delete().await()

            Log.d("PlantRepo", "✅ Plant deleted successfully")
            
            // UPDATE PROGRESS AFTER DELETION
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

            Log.d("PlantRepo", "✅ Plant watered successfully")
            
            // Record care action for badges and streaks
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
