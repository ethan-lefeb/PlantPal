package com.example.plantpal

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import kotlin.coroutines.resume

data class PlantIdResponse(
    val suggestions: List<Suggestion> = emptyList()
)

data class Suggestion(
    val plant_name: String? = null,
    val probability: Double? = null,
    val plant_details: PlantDetails? = null
)

data class PlantDetails(
    val scientific_name: String? = null
)

// --- Base64 helper ---
fun Uri.toBase64(context: Context): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(this)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// --- Plant.ID API call ---
suspend fun identifyPlantSuspend(context: Context, imageUri: Uri, apiKey: String): PlantIdResponse? {
    val client = OkHttpClient()
    val base64Image = imageUri.toBase64(context) ?: return null

    val json = """
        {
          "images": ["$base64Image"],
          "organs": ["leaf"]
        }
    """.trimIndent()

    val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

    val request = Request.Builder()
        .url("https://api.plant.id/v2/identify")
        .addHeader("Api-Key", apiKey)
        .post(requestBody)
        .build()

    return suspendCancellableCoroutine { cont ->
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                e.printStackTrace()
                cont.resume(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter(PlantIdResponse::class.java)
                        val body = response.body?.string()
                        val result = adapter.fromJson(body ?: "")
                        cont.resume(result)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cont.resume(null)
                    }
                } else {
                    println("Plant.ID API error: ${response.code} - ${response.message}")
                    cont.resume(null)
                }
            }
        })
    }
}
