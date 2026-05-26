package com.example.dacsiii_v2.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class SendOtpRequest(
    val email: String
)

data class SendOtpResponse(
    val success: Boolean,
    val message: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class UserDto(
    val user_id: Int,
    val email: String,
    val role: String
)

data class VerifyOtpResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto
)

// Generic error payload from backend.
data class ApiErrorResponse(
    val success: Boolean? = null,
    val message: String? = null
)

data class FirebaseLoginRequest(
    val idToken: String
)
