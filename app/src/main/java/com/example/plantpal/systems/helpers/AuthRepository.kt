package com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Register a new user and save their displayName in Firestore
    suspend fun registerUser(email: String, password: String, displayName: String): Result<Unit> {
        return try {
            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("No user returned"))

            // Save profile data in Firestore
            val profile = mapOf(
                "displayName" to displayName,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid).set(profile).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login existing user
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Fetch the current user's displayName from Firestore
    suspend fun getCurrentUserName(): String? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = db.collection("users").document(uid).get().await()
        return snapshot.getString("displayName")
    }

    fun currentUserId(): String? = auth.currentUser?.uid

    fun signOut() = auth.signOut()
}
