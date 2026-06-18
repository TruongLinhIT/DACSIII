package com.example.dacsiii_v2.data.repository

import android.content.Context
import android.net.Uri
import com.example.dacsiii_v2.data.model.ApiErrorResponse
import com.example.dacsiii_v2.data.model.DriverProfileUpdateRequest
import com.example.dacsiii_v2.data.model.DriverLocationRequest
import com.example.dacsiii_v2.data.remote.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class DriverRepository(private val apiService: ApiService) {
    suspend fun getProfile(token: String) = runCatching {
        apiService.getDriverProfile("Bearer $token")
    }.mapErrorMessage()

    suspend fun updateProfile(token: String, request: DriverProfileUpdateRequest) = runCatching {
        apiService.updateDriverProfile("Bearer $token", request)
    }.mapErrorMessage()

    suspend fun getWallet(token: String) = runCatching {
        apiService.getDriverWallet("Bearer $token")
    }.mapErrorMessage()

    suspend fun getAvailableOrders(token: String) = runCatching {
        apiService.getDriverAvailableOrders("Bearer $token")
    }.mapErrorMessage()

    suspend fun getAvailableOrdersByLocation(token: String, lat: Double, lng: Double) = runCatching {
        apiService.getAvailableOrdersByLocation("Bearer $token", lat, lng)
    }.mapErrorMessage()

    suspend fun getActiveOrders(token: String) = runCatching {
        apiService.getDriverActiveOrders("Bearer $token")
    }.mapErrorMessage()

    suspend fun getOrderHistory(token: String) = runCatching {
        apiService.getDriverOrderHistory("Bearer $token")
    }.mapErrorMessage()

    suspend fun acceptOrder(token: String, orderId: Int) = runCatching {
        apiService.acceptDriverOrder("Bearer $token", orderId)
    }.mapErrorMessage()

    suspend fun arrivePickup(token: String, orderId: Int) = runCatching {
        apiService.arrivePickup("Bearer $token", orderId)
    }.mapErrorMessage()

    suspend fun uploadPickupPhoto(token: String, context: Context, orderId: Int, photo: Uri) = runCatching {
        apiService.uploadPickupPhoto("Bearer $token", orderId, createPart(context, photo, "order_photo_pickup"))
    }.mapErrorMessage()

    suspend fun arriveDelivery(token: String, orderId: Int) = runCatching {
        apiService.arriveDelivery("Bearer $token", orderId)
    }.mapErrorMessage()

    suspend fun uploadDeliveryPhoto(token: String, context: Context, orderId: Int, photo: Uri) = runCatching {
        apiService.uploadDeliveryPhoto("Bearer $token", orderId, createPart(context, photo, "order_photo_delivery"))
    }.mapErrorMessage()

    suspend fun getEarnings(token: String, range: String, date: String?) = runCatching {
        apiService.getDriverEarnings("Bearer $token", range, date)
    }.mapErrorMessage()

    suspend fun updateLocation(token: String, lat: Double?, lng: Double?, isOnline: Int? = 1) = runCatching {
        apiService.updateDriverLocation(
            "Bearer $token",
            DriverLocationRequest(current_lat = lat, current_lng = lng, is_online = isOnline)
        )
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

private fun createPart(context: Context, uri: Uri, name: String): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    val fileName = "${name}_${System.currentTimeMillis()}"
    return MultipartBody.Part.createFormData(name, fileName, requestBody)
}
