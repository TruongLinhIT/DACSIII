package com.example.dacsiii_v2.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token generated: $token")

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("fcm_token", token).apply()

        val jwtToken = sharedPrefs.getString("jwt_token", null)
        if (!jwtToken.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userRepository = UserRepository(RetrofitClient.api)
                    userRepository.registerDeviceToken(jwtToken, token)
                    Log.d(TAG, "FCM Token registered automatically on token refresh.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to auto-register FCM token: ${e.message}")
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        val data = message.data
        val type = data["type"] ?: ""
        val orderId = data["order_id"] ?: ""
        val userId = data["user_id"] ?: ""

        val title = message.notification?.title ?: data["title"] ?: "Thong bao"
        val body = message.notification?.body ?: data["body"] ?: "Ban co thong bao moi"

        // Broadcast locally to notify active UI
        val broadcastIntent = Intent("com.example.dacsiii_v2.NOTIFICATION_RECEIVED").apply {
            putExtra("type", type)
            putExtra("order_id", orderId)
            putExtra("user_id", userId)
            putExtra("title", title)
            putExtra("body", body)
        }
        sendBroadcast(broadcastIntent)

        NotificationHelper.showOrderNotification(
            context = applicationContext,
            title = title,
            body = body,
            type = type,
            orderId = orderId,
            userId = userId
        )
    }
}
