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
    val scientific_name: String? = null,
    val common_names: List<String>? = null,
    val taxonomy: Taxonomy? = null,
    val url: String? = null,
    val wiki_description: WikiDescription? = null,
    val synonyms: List<String>? = null,
    val watering: WateringInfo? = null,
    val propagation_methods: List<String>? = null
)

data class Taxonomy(
    val genus: String? = null,
    val family: String? = null
)

data class WikiDescription(
    val value: String? = null,
    val citation: String? = null,
    val license_name: String? = null,
    val license_url: String? = null
)

data class WateringInfo(
    val max: Int? = null,
    val min: Int? = null
)

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


suspend fun identifyPlantSuspend(
    context: Context,
    imageUri: Uri,
    apiKey: String
): PlantIdResponse? {
    val client = OkHttpClient()
    val base64Image = imageUri.toBase64(context) ?: return null

    val json = """
        {
          "images": ["$base64Image"],
          "plant_details": [
            "common_names",
            "taxonomy",
            "url",
            "wiki_description",
            "synonyms",
            "watering",
            "propagation_methods"
          ]
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

data class HealthAssessmentResponse(
    val is_plant_probability: Double = 0.0,
    val is_healthy_probability: Double = 0.0,
    val suggestions: List<HealthSuggestion> = emptyList()
)

data class HealthSuggestion(
    val id: String? = null,
    val plant_name: String? = null,
    val probability: Double? = null,
    val plant_details: PlantDetails? = null,
    val disease_details: DiseaseDetails? = null
)

data class DiseaseDetails(
    val description: String? = null,
    val treatment: Treatment? = null,
    val cause: String? = null
)

data class Treatment(
    val chemical: List<String>? = null,
    val biological: List<String>? = null,
    val prevention: List<String>? = null
)

suspend fun assessPlantHealth(
    context: Context,
    imageUri: Uri,
    apiKey: String
): HealthAssessmentResponse? {
    val client = OkHttpClient()
    val base64Image = imageUri.toBase64(context) ?: return null

    val json = """
        {
          "images": ["$base64Image"],
          "plant_details": [
            "common_names",
            "watering",
            "propagation_methods",
            "wiki_description"
          ]
        }
    """.trimIndent()

    val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

    val request = Request.Builder()
        .url("https://api.plant.id/v2/health_assessment")
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
                        val adapter = moshi.adapter(HealthAssessmentResponse::class.java)
                        val body = response.body?.string()
                        val result = adapter.fromJson(body ?: "")
                        cont.resume(result)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cont.resume(null)
                    }
                } else {
                    println("Plant.ID Health API error: ${response.code} - ${response.message}")
                    cont.resume(null)
                }
            }
        })
    }
}