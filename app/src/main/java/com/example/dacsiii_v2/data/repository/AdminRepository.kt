package com.example.dacsiii_v2.data.repository

import com.example.dacsiii_v2.data.model.ApiErrorResponse
import com.example.dacsiii_v2.data.model.LockUserRequest
import com.example.dacsiii_v2.data.model.RevokeEkycRequest
import com.example.dacsiii_v2.data.model.VerifyIdentityRequest
import com.example.dacsiii_v2.data.remote.ApiService
import com.google.gson.Gson
import retrofit2.HttpException

class AdminRepository(private val apiService: ApiService) {
    suspend fun getAllUsers(token: String, role: String?, isVerified: String?, isLocked: Boolean? = null) = runCatching {
        val lockedParam = when (isLocked) {
            true -> "1"
            false -> null
            null -> null
        }
        apiService.getAllUsers("Bearer $token", role, isVerified)
    }.mapErrorMessage()

    suspend fun getUserDetail(token: String, userId: Int) = runCatching {
        apiService.getUserDetail("Bearer $token", userId)
    }.mapErrorMessage()

    suspend fun verifyUserIdentity(token: String, userId: Int, request: VerifyIdentityRequest) = runCatching {
        apiService.verifyUserIdentity("Bearer $token", userId, request)
    }.mapErrorMessage()

    suspend fun lockUser(token: String, userId: Int, reason: String) = runCatching {
        apiService.lockUser("Bearer $token", userId, LockUserRequest(reason))
    }.mapErrorMessage()

    suspend fun unlockUser(token: String, userId: Int) = runCatching {
        apiService.unlockUser("Bearer $token", userId)
    }.mapErrorMessage()

    suspend fun revokeEkyc(token: String, userId: Int, reason: String) = runCatching {
        apiService.revokeEkyc("Bearer $token", userId, RevokeEkycRequest(reason))
    }.mapErrorMessage()

    suspend fun getCommissionSummary(token: String, range: String, date: String? = null) = runCatching {
        apiService.getCommissionSummary("Bearer $token", range, date)
    }.mapErrorMessage()
}

private fun <T> Result<T>.mapErrorMessage(): Result<T> = fold(
    onSuccess = { Result.success(it) },
    onFailure = { error ->
        Result.failure(RuntimeException(parseErrorMessage(error)))
    }
)

private fun parseErrorMessage(error: Throwable): String {
    if (error is HttpException) {
        val errorBody = error.response()?.errorBody()?.string()
        if (!errorBody.isNullOrBlank()) {
            val apiError = runCatching {
                Gson().fromJson(errorBody, ApiErrorResponse::class.java)
            }.getOrNull()
            val message = apiError?.message?.trim()
            if (!message.isNullOrEmpty()) {
                return message
            }
        }
    }
    return error.message ?: "Unknown error"
}