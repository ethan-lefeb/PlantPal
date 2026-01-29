package com.example.plantpal.systems.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    data class FriendRequest(
        val requestId: String = "",
        val fromUid: String = "",
        val toUid: String = "",
        val status: String = "pending",
        val createdAt: Long = 0L
    )

    private fun requests() = db.collection("friend_requests")
    private fun friends(uid: String) = db.collection("users").document(uid).collection("friends")

    suspend fun sendRequest(toUid: String): Result<Unit> {
        return try {
            val me = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            if (toUid == me.uid) return Result.failure(Exception("You can’t add yourself"))

            val requestDoc = requests().document()
            val data = mapOf(
                "fromUid" to me.uid,
                "toUid" to toUid,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis()
            )
            requestDoc.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIncomingRequests(): Result<List<FriendRequest>> {
        return try {
            val me = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val snap = requests()
                .whereEqualTo("toUid", me.uid)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val list = snap.documents.map {
                FriendRequest(
                    requestId = it.id,
                    fromUid = it.getString("fromUid") ?: "",
                    toUid = it.getString("toUid") ?: "",
                    status = it.getString("status") ?: "pending",
                    createdAt = it.getLong("createdAt") ?: 0L
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptRequest(requestId: String, fromUid: String): Result<Unit> {
        return try {
            val me = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val now = System.currentTimeMillis()

            db.runBatch { batch ->
                batch.update(requests().document(requestId), "status", "accepted")
                batch.set(
                    friends(me.uid).document(fromUid),
                    mapOf("friendUid" to fromUid, "createdAt" to now)
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyFriendUids(): Result<List<String>> {
        return try {
            val me = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val snap = friends(me.uid).get().await()
            Result.success(snap.documents.map { it.id }.filter { it.isNotBlank() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun syncAcceptedOutgoingToFriends(): Result<Int> {
        return try {
            val me = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))

            val snap = requests()
                .whereEqualTo("fromUid", me.uid)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            var created = 0
            for (doc in snap.documents) {
                val toUid = doc.getString("toUid") ?: continue

                val friendDoc = friends(me.uid).document(toUid).get().await()
                if (!friendDoc.exists()) {
                    friends(me.uid).document(toUid)
                        .set(mapOf("friendUid" to toUid, "createdAt" to System.currentTimeMillis()))
                        .await()
                    created++
                }
            }

            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun declineRequest(requestId: String): Result<Unit> {
        return try {
            requests().document(requestId)
                .update("status", "declined")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
