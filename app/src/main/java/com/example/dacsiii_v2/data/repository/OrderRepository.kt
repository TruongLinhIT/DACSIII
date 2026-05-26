package com.example.dacsiii_v2.data.repository

import android.content.Context
import android.net.Uri
import com.example.dacsiii_v2.data.model.ApiErrorResponse
import com.example.dacsiii_v2.data.model.CreateOrderRequest
import com.example.dacsiii_v2.data.model.OrderDetailResponse
import com.example.dacsiii_v2.data.remote.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class OrderRepository(private val apiService: ApiService) {
    suspend fun createOrder(token: String, request: CreateOrderRequest) = runCatching {
        apiService.createOrder("Bearer $token", request)
    }.mapErrorMessage()

    suspend fun getCustomerOrders(token: String) = runCatching {
        apiService.getCustomerOrders("Bearer $token")
    }.mapErrorMessage()

    suspend fun getOrderDetails(token: String, orderId: Int): Result<OrderDetailResponse> = runCatching {
        apiService.getOrderDetails("Bearer $token", orderId)
    }.mapErrorMessage()

    suspend fun uploadOrderPhotoBefore(token: String, context: Context, photo: Uri) = runCatching {
        apiService.uploadOrderPhotoBefore("Bearer $token", createPart(context, photo, "order_photo_before"))
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
