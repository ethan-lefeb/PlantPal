package com.example.plantpal

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import java.net.HttpURLConnection
import java.net.URL

class CareReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        fun triggerCareNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<CareReminderWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        val url = URL(
            "https://us-central1-plantpal-3193b.cloudfunctions.net/sendCareRemindersNow?userId=$uid"
        )

        return try {
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val code = conn.responseCode
            if (code == 200) Result.success() else Result.retry()

        } catch (e: Exception) {
            Result.retry()
        }
    }
}
