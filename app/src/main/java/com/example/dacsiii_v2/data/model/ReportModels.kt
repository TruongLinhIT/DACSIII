package com.example.dacsiii_v2.data.model

data class ReportRequest(
    val order_id: Int?,
    val driver_id: Int,
    val reason_type: String,
    val description: String
)

data class DriverReport(
    val report_id: Int,
    val order_id: Int?,
    val reporter_id: Int,
    val driver_id: Int,
    val reason_type: String,
    val description: String,
    val status: String,
    val created_at: String,
    val resolved_at: String?,
    // Joined fields from backend query
    val reporter_name: String?,
    val reporter_phone: String?,
    val driver_name: String?,
    val driver_phone: String?,
    val license_plate: String?
)

data class ReportListResponse(
    val success: Boolean,
    val reports: List<DriverReport>
)

data class ReportDetailResponse(
    val success: Boolean,
    val report: DriverReport
)
