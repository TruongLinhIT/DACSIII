package com.example.dacsiii_v2.data.remote

import com.example.dacsiii_v2.data.model.ApiMessageResponse
import com.example.dacsiii_v2.data.model.AvatarUploadResponse
import com.example.dacsiii_v2.data.model.FirebaseLoginRequest
import com.example.dacsiii_v2.data.model.IdentityUploadResponse
import com.example.dacsiii_v2.data.model.LoginRequest
import com.example.dacsiii_v2.data.model.OrderDetailResponse
import com.example.dacsiii_v2.data.model.OrderPhotoUploadResponse
import com.example.dacsiii_v2.data.model.ProfileUpdateRequest
import com.example.dacsiii_v2.data.model.ResetPasswordRequest
import com.example.dacsiii_v2.data.model.SendOtpRequest
import com.example.dacsiii_v2.data.model.SendOtpResponse
import com.example.dacsiii_v2.data.model.UserMeResponse
import com.example.dacsiii_v2.data.model.VerifyOtpRequest
import com.example.dacsiii_v2.data.model.VerifyOtpResponse
import com.example.dacsiii_v2.data.model.AllUsersResponse
import com.example.dacsiii_v2.data.model.VerifyIdentityRequest
import com.example.dacsiii_v2.data.model.LockUserRequest
import com.example.dacsiii_v2.data.model.RevokeEkycRequest
import com.example.dacsiii_v2.data.model.CreateOrderRequest
import com.example.dacsiii_v2.data.model.OrderResponse
import com.example.dacsiii_v2.data.model.DriverEarningsResponse
import com.example.dacsiii_v2.data.model.DriverOrderListResponse
import com.example.dacsiii_v2.data.model.DriverProfileResponse
import com.example.dacsiii_v2.data.model.DriverProfileUpdateRequest
import com.example.dacsiii_v2.data.model.DriverWalletResponse
import com.example.dacsiii_v2.data.model.ChangePasswordRequest
import com.example.dacsiii_v2.data.model.DeviceTokenRequest
import com.example.dacsiii_v2.data.model.DriverLocationRequest
import com.example.dacsiii_v2.data.model.NotificationListResponse
import com.example.dacsiii_v2.data.model.CommissionSummaryResponse
import com.example.dacsiii_v2.data.model.ReportRequest
import com.example.dacsiii_v2.data.model.ReportListResponse
import com.example.dacsiii_v2.data.model.ReportDetailResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): VerifyOtpResponse

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiMessageResponse

    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): UserMeResponse

    @PUT("users/me/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): ApiMessageResponse

    @Multipart
    @POST("users/me/identity")
    suspend fun uploadIdentity(
        @Header("Authorization") token: String,
        @Part id_card_front: MultipartBody.Part,
        @Part id_card_back: MultipartBody.Part,
        @Part portrait: MultipartBody.Part
    ): IdentityUploadResponse

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): AvatarUploadResponse

    @POST("auth/firebase-login")
    suspend fun firebaseLogin(@Body request: FirebaseLoginRequest): VerifyOtpResponse

    @POST("users/me/password/otp")
    suspend fun sendChangePasswordOtp(
        @Header("Authorization") token: String
    ): ApiMessageResponse

    @POST("users/me/password")
    suspend fun changePasswordWithOtp(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): ApiMessageResponse

    @POST("users/me/device-token")
    suspend fun registerDeviceToken(
        @Header("Authorization") token: String,
        @Body request: DeviceTokenRequest
    ): ApiMessageResponse

    // --- Admin Endpoints ---
    @GET("admin/users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String,
        @Query("role") role: String? = null,
        @Query("is_verified") isVerified: String? = null
    ): AllUsersResponse

    @GET("admin/users/{id}")
    suspend fun getUserDetail(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): UserMeResponse

    @PUT("admin/users/{id}/verify")
    suspend fun verifyUserIdentity(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body request: VerifyIdentityRequest
    ): ApiMessageResponse

    @PUT("admin/users/{id}/lock")
    suspend fun lockUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body request: LockUserRequest
    ): ApiMessageResponse

    @PUT("admin/users/{id}/unlock")
    suspend fun unlockUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): ApiMessageResponse

    @PUT("admin/users/{id}/revoke-ekyc")
    suspend fun revokeEkyc(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body request: RevokeEkycRequest
    ): ApiMessageResponse

    @GET("admin/commission-summary")
    suspend fun getCommissionSummary(
        @Header("Authorization") token: String,
        @Query("range") range: String,
        @Query("date") date: String? = null
    ): CommissionSummaryResponse

    // --- Order Endpoints ---
    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): OrderResponse

    @GET("orders")
    suspend fun getCustomerOrders(
        @Header("Authorization") token: String
    ): OrderResponse

    @GET("orders/{id}")
    suspend fun getOrderDetails(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): OrderDetailResponse

    @Multipart
    @POST("orders/photo-before")
    suspend fun uploadOrderPhotoBefore(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): OrderPhotoUploadResponse

    // --- Driver Endpoints ---
    @GET("driver/profile")
    suspend fun getDriverProfile(
        @Header("Authorization") token: String
    ): DriverProfileResponse

    @PUT("driver/profile")
    suspend fun updateDriverProfile(
        @Header("Authorization") token: String,
        @Body request: DriverProfileUpdateRequest
    ): ApiMessageResponse

    @GET("driver/wallet")
    suspend fun getDriverWallet(
        @Header("Authorization") token: String
    ): DriverWalletResponse

    @GET("driver/orders/available")
    suspend fun getDriverAvailableOrders(
        @Header("Authorization") token: String
    ): DriverOrderListResponse

    @GET("driver/orders/active")
    suspend fun getDriverActiveOrders(
        @Header("Authorization") token: String
    ): DriverOrderListResponse

    @GET("driver/orders/history")
    suspend fun getDriverOrderHistory(
        @Header("Authorization") token: String
    ): DriverOrderListResponse

    @POST("driver/orders/{id}/accept")
    suspend fun acceptDriverOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): ApiMessageResponse

    @POST("driver/orders/{id}/arrive-pickup")
    suspend fun arrivePickup(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): ApiMessageResponse

    @Multipart
    @POST("driver/orders/{id}/pickup")
    suspend fun uploadPickupPhoto(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Part photo: MultipartBody.Part
    ): OrderPhotoUploadResponse

    @POST("driver/orders/{id}/arrive-delivery")
    suspend fun arriveDelivery(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): ApiMessageResponse

    @Multipart
    @POST("driver/orders/{id}/complete")
    suspend fun uploadDeliveryPhoto(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Part photo: MultipartBody.Part
    ): OrderPhotoUploadResponse

    @GET("driver/earnings")
    suspend fun getDriverEarnings(
        @Header("Authorization") token: String,
        @Query("range") range: String,
        @Query("date") date: String? = null
    ): DriverEarningsResponse

    @POST("driver/location")
    suspend fun updateDriverLocation(
        @Header("Authorization") token: String,
        @Body request: DriverLocationRequest
    ): ApiMessageResponse

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): NotificationListResponse

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("id") notificationId: Int
    ): ApiMessageResponse

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(
        @Header("Authorization") token: String
    ): ApiMessageResponse

    @GET("orders/available")
    suspend fun getAvailableOrdersByLocation(
        @Header("Authorization") token: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): DriverOrderListResponse

    // --- Report Endpoints ---
    @POST("reports")
    suspend fun submitReport(
        @Header("Authorization") token: String,
        @Body request: ReportRequest
    ): ApiMessageResponse

    @GET("reports")
    suspend fun getAllReports(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): ReportListResponse

    @GET("reports/{id}")
    suspend fun getReportDetail(
        @Header("Authorization") token: String,
        @Path("id") reportId: Int
    ): ReportDetailResponse

    @PUT("reports/{id}/resolve")
    suspend fun resolveReport(
        @Header("Authorization") token: String,
        @Path("id") reportId: Int
    ): ApiMessageResponse
}