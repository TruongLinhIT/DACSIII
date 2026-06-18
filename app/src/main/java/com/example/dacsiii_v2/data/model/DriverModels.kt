package com.example.dacsiii_v2.data.model

data class DriverProfileResponse(
    val success: Boolean,
    val profile: DriverProfile
)

data class DriverProfile(
    val user_id: Int,
    val phone: String?,
    val full_name: String?,
    val email: String?,
    val avatar_url: String?,
    val cccd_number: String?,
    val id_card_front_url: String?,
    val id_card_back_url: String?,
    val portrait_url: String?,
    val is_verified: String?,
    val identity_reject_reason: String?,
    val role: String?,
    val vehicle_type: String?,
    val license_plate: String?,
    val is_online: Int?,
    val wallet_balance: Double?,
    val rating_avg: Double?
)

data class DriverProfileUpdateRequest(
    val license_plate: String,
    val vehicle_type: String? = null
)

data class DriverLocationRequest(
    val current_lat: Double?,
    val current_lng: Double?,
    val is_online: Int? = 1
)

data class DriverWalletResponse(
    val success: Boolean,
    val wallet: DriverWallet
)

data class DriverWallet(
    val wallet_balance: Double,
    val rating_avg: Double
)

data class DriverOrderListResponse(
    val success: Boolean,
    val orders: List<DriverOrderSummary>
)

data class DriverOrderSummary(
    val order_id: Int,
    val package_type: String? = null,
    val weight_kg: Double? = null,
    val pickup_address: String? = null,
    val delivery_address: String? = null,
    val pickup_lat: Double? = null,
    val pickup_lng: Double? = null,
    val delivery_lat: Double? = null,
    val delivery_lng: Double? = null,
    val distance_km: Double? = null,
    val total_price: Double? = null,
    val driver_earning: Double? = null,
    val cod_amount: Double? = null,
    val status: String? = null,
    val created_at: String? = null,
    val accepted_at: String? = null,
    val completed_at: String? = null,
    val customer_name: String? = null,
    val customer_phone: String? = null,
    val distance_from_driver: Double? = null
)

data class DriverEarningsTotals(
    val total_earning: Double,
    val order_count: Int
)

data class DriverEarningsBucket(
    val bucket: String,
    val total_earning: Double,
    val order_count: Int
)

data class DriverEarningsResponse(
    val success: Boolean,
    val range: String,
    val start: String,
    val end: String,
    val totals: DriverEarningsTotals,
    val breakdown: List<DriverEarningsBucket>
)
