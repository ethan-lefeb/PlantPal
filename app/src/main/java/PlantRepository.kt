package com.example.plantpal

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlantRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error saving plant", e)
            Result.failure(e)
        }
    }

    // get all plants for current user
    suspend fun getAllPlants(): Result<List<PlantProfile>> {
        return try {
            val userId = auth.currentUser?.uid
            Log.d("PlantRepo", "Getting all plants for user: $userId")

            if (userId == null) {
                Log.e("PlantRepo", "User not authenticated!")
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = getPlantsCollection().get().await()
            Log.d("PlantRepo", "Retrieved ${snapshot.size()} plants")

            val plants = snapshot.documents.mapNotNull {
                it.toObject(PlantProfile::class.java)
            }

            Log.d("PlantRepo", "Successfully parsed ${plants.size} plants")
            Result.success(plants)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error getting plants", e)
            Result.failure(e)
        }
    }

    // get a specific plant by ID
    suspend fun getPlant(plantId: String): Result<PlantProfile> {
        return try {
            Log.d("PlantRepo", "Getting plant: $plantId")

            val doc = getPlantsCollection().document(plantId).get().await()
            val plant = doc.toObject(PlantProfile::class.java)

            if (plant != null) {
                Log.d("PlantRepo", "✅ Plant found: ${plant.commonName}")
                Result.success(plant)
            } else {
                Log.e("PlantRepo", "❌ Plant not found")
                Result.failure(Exception("Plant not found"))
            }
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error getting plant", e)
            Result.failure(e)
        }
    }

    // update an existing plant
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

    // delete a plant
    suspend fun deletePlant(plantId: String): Result<Unit> {
        return try {
            Log.d("PlantRepo", "Deleting plant: $plantId")
            getPlantsCollection().document(plantId).delete().await()

            Log.d("PlantRepo", "✅ Plant deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error deleting plant", e)
            Result.failure(e)
        }
    }

    // update watering timestamp
    suspend fun waterPlant(plantId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            Log.d("PlantRepo", "Watering plant: $plantId at $currentTime")

            getPlantsCollection().document(plantId)
                .update("lastWatered", currentTime)
                .await()

            Log.d("PlantRepo", "✅ Plant watered successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error watering plant", e)
            Result.failure(e)
        }
    }

    // update fertilizing timestamp
    suspend fun fertilizePlant(plantId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            Log.d("PlantRepo", "Fertilizing plant: $plantId at $currentTime")

            getPlantsCollection().document(plantId)
                .update("lastFertilized", currentTime)
                .await()

            Log.d("PlantRepo", "✅ Plant fertilized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PlantRepo", "❌ Error fertilizing plant", e)
            Result.failure(e)
        }
    }

    // update health status
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