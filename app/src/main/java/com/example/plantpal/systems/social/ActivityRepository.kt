package com.example.plantpal.systems.social

import android.util.Log
import com.example.plantpal.data.ActivityEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ActivityRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun activitiesCollection(uid: String) =
        db.collection("users").document(uid).collection("activities")

    suspend fun log(
        type: String,
        text: String,
        plantId: String? = null,
        plantName: String? = null,
        createdAt: Long = System.currentTimeMillis()
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val uid = user.uid

            val actorName =
                user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                    ?: "Unknown"

            val doc = activitiesCollection(uid).document()
            val event = ActivityEvent(
                activityId = doc.id,
                actorUid = uid,
                actorName = actorName,
                type = type,
                text = text,
                plantId = plantId,
                plantName = plantName,
                createdAt = createdAt
            )

            doc.set(event).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("ActivityRepo", "Failed to log activity: $type", e)
            Result.failure(e)
        }
    }

    suspend fun getRecentForUser(uid: String, limit: Int = 10): Result<List<ActivityEvent>> {
        return try {
            val snapshot = activitiesCollection(uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { it.toObject(ActivityEvent::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Log.e("ActivityRepo", "Failed to load activities for $uid", e)
            Result.failure(e)
        }
    }


    suspend fun getMyRecent(limit: Int = 10): Result<List<ActivityEvent>> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val snapshot = activitiesCollection(uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { it.toObject(ActivityEvent::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Log.e("ActivityRepo", "Failed to load recent activities", e)
            Result.failure(e)
        }
    }

}
