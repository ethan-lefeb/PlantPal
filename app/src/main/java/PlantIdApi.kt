package com.example.plantpal

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.resume

data class PlantIdResponse(
    val suggestions: List<Suggestion>
)

data class Suggestion(
    val plant_name: String,
    val scientific_name: String,
    val probability: Double
)

// Callback-based API
fun identifyPlant(
    context: Context,
    imageUri: Uri,
    apiKey: String,
    callback: (PlantIdResponse?) -> Unit
) {
    val client = OkHttpClient()
    val file = File(imageUri.path!!)
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("organs", "leaf")
        .addFormDataPart(
            "images",
            file.name,
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url("https://api.plant.id/v2/identify")
        .addHeader("s4VpDe9U20KQ3NsQJQmimFqneGPGfp1wef27eS7bkJyiGvQntv", apiKey)
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: java.io.IOException) {
            callback(null)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val jsonAdapter = moshi.adapter(PlantIdResponse::class.java)
                val plantIdResponse = jsonAdapter.fromJson(response.body?.string() ?: "")
                callback(plantIdResponse)
            } else {
                callback(null)
            }
        }
    })
}

// Coroutine-friendly suspend version
suspend fun identifyPlantSuspend(context: Context, imageUri: Uri, apiKey: String): PlantIdResponse? =
    suspendCancellableCoroutine { cont ->
        identifyPlant(context, imageUri, apiKey) { response ->
            cont.resume(response)
        }
    }
