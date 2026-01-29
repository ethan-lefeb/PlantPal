package com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers

import android.util.Log
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.CustomReminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReminderRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun remindersCollection() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("reminders")

    companion object {
        private const val TAG = "ReminderRepo"
    }

    suspend fun createReminder(reminder: CustomReminder): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val docRef = remindersCollection().document()

            val reminderWithIds = reminder.copy(
                id = docRef.id,
                userId = userId
            )

            docRef.set(reminderWithIds).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating reminder", e)
            Result.failure(e)
        }
    }

    suspend fun getReminders(): Result<List<CustomReminder>> {
        return try {
            val snap = remindersCollection()
                .orderBy("nextFireAt")
                .get()
                .await()

            val reminders = snap.documents.mapNotNull {
                it.toObject(CustomReminder::class.java)
            }

            Result.success(reminders)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading reminders", e)
            Result.failure(e)
        }
    }

    suspend fun updateReminder(reminder: CustomReminder): Result<Unit> {
        return try {
            if (reminder.id.isEmpty()) {
                return Result.failure(Exception("Reminder ID required"))
            }
            remindersCollection().document(reminder.id).set(reminder).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reminder", e)
            Result.failure(e)
        }
    }

    

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            remindersCollection().document(reminderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reminder", e)
            Result.failure(e)
        }
    }
}