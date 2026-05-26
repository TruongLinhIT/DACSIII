package com.example.dacsiii_v2.data.model

data class AllUsersResponse(
    val success: Boolean,
    val users: List<UserSummary>
)

data class UserSummary(
    val user_id: Int,
    val email: String?,
    val full_name: String?,
    val role: String,
    val is_verified: String,
    val created_at: String
)

data class VerifyIdentityRequest(
    val status: String, // 'verified' hoặc 'rejected'
    val reason: String? = null
)
