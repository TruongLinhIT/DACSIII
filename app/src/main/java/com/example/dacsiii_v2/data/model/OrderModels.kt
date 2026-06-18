package com.example.dacsiii_v2.data.model

data class Order(
    val order_id: Int,
    val customer_id: Int,
    val driver_id: Int?,
    val package_type: String,
    val weight_kg: Double,
    val order_description: String?,
    val pickup_address: String,
    val delivery_address: String,
    val pickup_lat: Double,
    val pickup_lng: Double,
    val delivery_lat: Double,
    val delivery_lng: Double,
    val distance_km: Double?,
    val total_price: Double,
    val driver_earning: Double? = null,
    val photo_before_booking: String,
    val photo_at_pickup: String?,
    val photo_at_delivery: String?,
    val status: String,
    val created_at: String,
    val accepted_at: String?,
    val completed_at: String?,
    val sender_name: String,
    val sender_phone: String,
    val recipient_name: String,
    val recipient_phone: String,
    val pickup_note: String?,
    val delivery_note: String?,
    val package_size: String,
    val cod_amount: Double?,
    val payment_method: String
)

data class CreateOrderRequest(
    val package_type: String,
    val weight_kg: Double,
    val order_description: String?,
    val pickup_address: String,
    val delivery_address: String,
    val pickup_lat: Double,
    val pickup_lng: Double,
    val delivery_lat: Double,
    val delivery_lng: Double,
    val distance_km: Double?,
    val total_price: Double,
    val photo_before_booking: String, // URL hoặc base64 tùy logic upload
    val sender_name: String,
    val sender_phone: String,
    val recipient_name: String,
    val recipient_phone: String,
    val pickup_note: String?,
    val delivery_note: String?,
    val package_size: String,
    val cod_amount: Double?,
    val payment_method: String
)

data class OrderResponse(
    val success: Boolean,
    val message: String?,
    val order_id: Int? = null,
    val order: Order? = null,
    val orders: List<Order>? = null
)

data class OrderPhotoUploadResponse(
    val success: Boolean,
    val message: String,
    val photo_url: String
)

data class DriverInfo(
    val user_id: Int,
    val full_name: String?,
    val phone: String?,
    val avatar_url: String?,
    val vehicle_type: String?,
    val license_plate: String?,
    val rating_avg: Double?
)

data class OrderDetailResponse(
    val success: Boolean,
    val order: Order?,
    val driver: DriverInfo?
)