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
    val is_locked: Boolean = false,
    val created_at: String
)

data class VerifyIdentityRequest(
    val status: String, // 'verified' hoặc 'rejected'
    val reason: String? = null
)

data class LockUserRequest(
    val reason: String
)

data class RevokeEkycRequest(
    val reason: String
)

data class CommissionTotals(
    val total_commission: Double,
    val order_count: Int
)

data class CommissionBucket(
    val bucket: String,
    val total_commission: Double,
    val order_count: Int
)

data class CommissionSummaryResponse(
    val success: Boolean,
    val range: String,
    val start: String,
    val end: String,
    val totals: CommissionTotals,
    val breakdown: List<CommissionBucket>
)