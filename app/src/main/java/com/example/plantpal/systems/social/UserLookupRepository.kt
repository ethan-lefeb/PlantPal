package com.example.plantpal.systems.social

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserLookupRepository {
    private val db = FirebaseFirestore.getInstance()

    data class UserHit(
        val uid: String,
        val displayName: String?,
        val emailLower: String?
    )

    suspend fun findUserByEmail(email: String): Result<UserHit> {
        return try {
            val emailLower = email.trim().lowercase()
            val snap = db.collection("users")
                .whereEqualTo("emailLower", emailLower)
                .limit(1)
                .get()
                .await()

            val doc = snap.documents.firstOrNull()
                ?: return Result.failure(Exception("No user found for that email"))

            val displayName = doc.getString("displayName")
            val storedEmailLower = doc.getString("emailLower")

            Result.success(UserHit(doc.id, displayName, storedEmailLower))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
