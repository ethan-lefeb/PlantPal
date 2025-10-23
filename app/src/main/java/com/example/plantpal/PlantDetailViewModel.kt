package com.example.plantpal

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class PlantDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getPlant(userId: String, plantId: String): Result<PlantProfile> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("plants")
                .document(plantId)
                .get()
                .await()
            val plant = doc.toObject(PlantProfile::class.java)
            if (plant != null) Result.success(plant)
            else Result.failure(Exception("Plant not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePlant(updatedPlant: PlantProfile): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(updatedPlant.userId)
                .collection("plants")
                .document(updatedPlant.plantId)
                .set(updatedPlant)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
