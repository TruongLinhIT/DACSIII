package com.example.dacsiii_v2.data.model

data class NotificationItem(
    val notification_id: Int,
    val user_id: Int,
    val title: String,
    val body: String,
    val type: String? = null,
    val data_json: String? = null,
    val is_read: Int = 0,
    val created_at: String? = null
)

data class NotificationListResponse(
    val success: Boolean,
    val notifications: List<NotificationItem>
)

