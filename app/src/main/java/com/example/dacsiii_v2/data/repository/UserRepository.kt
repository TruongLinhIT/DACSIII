package com.example.dacsiii_v2.data.repository

import android.content.Context
import android.net.Uri
import com.example.dacsiii_v2.data.model.ApiErrorResponse
import com.example.dacsiii_v2.data.model.ProfileUpdateRequest
import com.example.dacsiii_v2.data.model.ChangePasswordRequest
import com.example.dacsiii_v2.data.model.DeviceTokenRequest
import com.example.dacsiii_v2.data.remote.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class UserRepository(private val apiService: ApiService) {
    suspend fun getMe(token: String) = runCatching {
        apiService.getMe("Bearer $token")
    }.mapErrorMessage()

    suspend fun updateProfile(token: String, request: ProfileUpdateRequest) = runCatching {
        apiService.updateProfile("Bearer $token", request)
    }.mapErrorMessage()

    suspend fun uploadIdentity(token: String, context: Context, front: Uri, back: Uri, portrait: Uri) = runCatching {
        apiService.uploadIdentity(
            "Bearer $token",
            createPart(context, front, "id_card_front"),
            createPart(context, back, "id_card_back"),
            createPart(context, portrait, "portrait")
        )
    }.mapErrorMessage()

    suspend fun uploadAvatar(token: String, context: Context, avatar: Uri) = runCatching {
        apiService.uploadAvatar("Bearer $token", createPart(context, avatar, "avatar"))
    }.mapErrorMessage()

    suspend fun sendChangePasswordOtp(token: String) = runCatching {
        apiService.sendChangePasswordOtp("Bearer $token")
    }.mapErrorMessage()

    suspend fun changePasswordWithOtp(token: String, request: ChangePasswordRequest) = runCatching {
        apiService.changePasswordWithOtp("Bearer $token", request)
    }.mapErrorMessage()

    suspend fun registerDeviceToken(token: String, deviceToken: String) = runCatching {
        apiService.registerDeviceToken("Bearer $token", DeviceTokenRequest(deviceToken))
    }.mapErrorMessage()

    suspend fun getNotifications(
        token: String,
        status: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ) = runCatching {
        apiService.getNotifications("Bearer $token", status, limit, offset)
    }.mapErrorMessage()

    suspend fun markNotificationRead(token: String, notificationId: Int) = runCatching {
        apiService.markNotificationRead("Bearer $token", notificationId)
    }.mapErrorMessage()

    suspend fun markAllNotificationsRead(token: String) = runCatching {
        apiService.markAllNotificationsRead("Bearer $token")
    }.mapErrorMessage()
}

private fun createPart(context: Context, uri: Uri, name: String): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    val fileName = "${name}_${System.currentTimeMillis()}"
    return MultipartBody.Part.createFormData(name, fileName, requestBody)
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