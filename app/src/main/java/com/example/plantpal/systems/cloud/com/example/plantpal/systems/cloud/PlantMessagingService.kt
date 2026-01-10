package com.example.plantpal.com.example.plantpal.systems.cloud.com.example.plantpal.systems.cloud

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.plantpal.MainActivity
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.SetOptions

class PlantMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: "PlantPal Update"
        val body = message.notification?.body ?: "Check your plant’s status!"

        sendNotification(title, body)
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = "plant_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Plant Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Plant care reminders and updates"
            }

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the main app when tapping the notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_menu_gallery)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted — skip showing
            return
        }

        NotificationManagerCompat.from(this).notify(0, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        println("🌱 FCM token refreshed: $token")

        val uid = AuthRepository.currentUserId()
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(mapOf("fcmToken" to token), SetOptions.merge()) // SAFE MERGE
                .addOnSuccessListener {
                    println("✅ Token updated for user: $uid")
                }
                .addOnFailureListener { e ->
                    println("⚠️ Failed to update token: ${e.message}")
                }
        } else {
            println("⚠️ No user logged in yet — token will be saved later.")
        }
    }
}
