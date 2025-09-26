package com.example.plantpal.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    suspend fun registerUser(email: String, password: String, displayName: String): Result<Unit> {
        return try {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
                ?: return Result.failure(Exception("No user returned"))
            val profile = mapOf(
                "displayName" to displayName,
                "createdAt"   to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid).set(profile).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun currentUserId(): String? = auth.currentUser?.uid
    fun signOut() = auth.signOut()
}
