package com.example.plantpal

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

    // Create a new plant
    suspend fun addPlant(plant: PlantProfile): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val docRef = getPlantsCollection().document()
            val plantWithIds = plant.copy(
                plantId = docRef.id,
                userId = userId
            )

            docRef.set(plantWithIds).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all plants for current user
    suspend fun getAllPlants(): Result<List<PlantProfile>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = getPlantsCollection().get().await()
            val plants = snapshot.documents.mapNotNull {
                it.toObject(PlantProfile::class.java)
            }
            Result.success(plants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get a specific plant by ID
    suspend fun getPlant(plantId: String): Result<PlantProfile> {
        return try {
            val doc = getPlantsCollection().document(plantId).get().await()
            val plant = doc.toObject(PlantProfile::class.java)

            if (plant != null) {
                Result.success(plant)
            } else {
                Result.failure(Exception("Plant not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update an existing plant
    suspend fun updatePlant(plant: PlantProfile): Result<Unit> {
        return try {
            if (plant.plantId.isEmpty()) {
                return Result.failure(Exception("Plant ID is required"))
            }

            getPlantsCollection().document(plant.plantId).set(plant).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete a plant
    suspend fun deletePlant(plantId: String): Result<Unit> {
        return try {
            getPlantsCollection().document(plantId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update watering timestamp
    suspend fun waterPlant(plantId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            getPlantsCollection().document(plantId)
                .update(
                    mapOf(
                        "lastWatered" to currentTime,
                        "currentStatus.lastWatered" to currentTime
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update fertilizing timestamp
    suspend fun fertilizePlant(plantId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            getPlantsCollection().document(plantId)
                .update(
                    mapOf(
                        "lastFertilized" to currentTime,
                        "currentStatus.lastFertilized" to currentTime
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update health status
    suspend fun updateHealthStatus(plantId: String, health: String): Result<Unit> {
        return try {
            getPlantsCollection().document(plantId)
                .update(
                    mapOf(
                        "health" to health,
                        "currentStatus.health" to health
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}