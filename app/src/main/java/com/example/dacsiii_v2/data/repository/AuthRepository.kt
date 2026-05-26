package com.example.dacsiii_v2.data.repository

import com.example.dacsiii_v2.data.model.ApiErrorResponse
import com.example.dacsiii_v2.data.model.FirebaseLoginRequest
import com.example.dacsiii_v2.data.model.LoginRequest
import com.example.dacsiii_v2.data.model.ResetPasswordRequest
import com.example.dacsiii_v2.data.model.SendOtpRequest
import com.example.dacsiii_v2.data.model.VerifyOtpRequest
import com.example.dacsiii_v2.data.remote.ApiService
import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(request: LoginRequest) = runCatching {
        apiService.login(request)
    }.mapErrorMessage()

    suspend fun sendOtp(email: String) = runCatching {
        apiService.sendOtp(SendOtpRequest(email))
    }.mapErrorMessage()

    suspend fun verifyOtp(email: String, otp: String) = runCatching {
        apiService.verifyOtp(VerifyOtpRequest(email, otp))
    }.mapErrorMessage()

    suspend fun resetPassword(request: ResetPasswordRequest) = runCatching {
        apiService.resetPassword(request)
    }.mapErrorMessage()

    suspend fun firebaseLogin(idToken: String) = runCatching {
        apiService.firebaseLogin(FirebaseLoginRequest(idToken))
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
