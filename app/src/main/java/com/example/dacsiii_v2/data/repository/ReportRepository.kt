package com.example.dacsiii_v2.data.repository

import com.example.dacsiii_v2.data.model.ApiErrorResponse
import com.example.dacsiii_v2.data.model.ReportRequest
import com.example.dacsiii_v2.data.model.ReportListResponse
import com.example.dacsiii_v2.data.model.ReportDetailResponse
import com.example.dacsiii_v2.data.remote.ApiService
import com.google.gson.Gson
import retrofit2.HttpException

class ReportRepository(private val apiService: ApiService) {
    suspend fun submitReport(token: String, request: ReportRequest) = runCatching {
        apiService.submitReport("Bearer $token", request)
    }.mapErrorMessage()

    suspend fun getAllReports(token: String, status: String? = null, search: String? = null) = runCatching {
        apiService.getAllReports("Bearer $token", status, search)
    }.mapErrorMessage()

    suspend fun getReportDetail(token: String, reportId: Int) = runCatching {
        apiService.getReportDetail("Bearer $token", reportId)
    }.mapErrorMessage()

    suspend fun resolveReport(token: String, reportId: Int) = runCatching {
        apiService.resolveReport("Bearer $token", reportId)
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
