package com.example.dacsiii_v2.data.model

data class ApiMessageResponse(
    val success: Boolean,
    val message: String
)

data class UserMeResponse(
    val success: Boolean,
    val user: UserProfile
)

data class UserProfile(
    val user_id: Int,
    val phone: String?,
    val full_name: String?,
    val email: String?,
    val role: String,
    val avatar_url: String?,
    val cccd_number: String?,
    val id_card_front_url: String?,
    val id_card_back_url: String?,
    val portrait_url: String?,
    val is_verified: String,
    val is_locked: Boolean = false,
    val identity_reject_reason: String?,
    val created_at: String,
    val updated_at: String
)

data class ProfileUpdateRequest(
    val full_name: String? = null,
    val password: String? = null,
    val email: String? = null,
    val cccd_number: String? = null,
    val avatar_url: String? = null
)

data class ChangePasswordRequest(
    val otp: String,
    val newPassword: String
)

data class IdentityUploadResponse(
    val success: Boolean,
    val message: String,
    val files: IdentityFiles
)

data class IdentityFiles(
    val id_card_front_url: String,
    val id_card_back_url: String,
    val portrait_url: String,
    val is_verified: String
)

data class AvatarUploadResponse(
    val success: Boolean,
    val message: String,
    val avatar_url: String
)

data class DeviceTokenRequest(
    val token: String
)
