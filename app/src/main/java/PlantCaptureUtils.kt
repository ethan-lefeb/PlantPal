package com.example.plantpal

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

suspend fun savePlantProfileToFirebase(
    imageUri: Uri,
    suggestion: Suggestion,
    userId: String
): String {
    val storageRef = FirebaseStorage.getInstance().reference
        .child("plants/${UUID.randomUUID()}.jpg")
    storageRef.putFile(imageUri).await()
    val downloadUrl = storageRef.downloadUrl.await().toString()

    val plantProfile = PlantProfile(
        plantId = UUID.randomUUID().toString(),
        userId = userId,
        commonName = suggestion.plant_name,
        scientificName = suggestion.scientific_name,
        confidence = suggestion.probability,
        photoUrl = downloadUrl
    )

    FirebaseFirestore.getInstance()
        .collection("plants")
        .document(plantProfile.plantId)
        .set(plantProfile)
        .await()

    return plantProfile.plantId
}
