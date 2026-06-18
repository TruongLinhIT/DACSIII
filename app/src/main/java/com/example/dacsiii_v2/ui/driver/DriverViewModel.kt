package com.example.dacsiii_v2.ui.driver

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.DriverEarningsResponse
import com.example.dacsiii_v2.data.model.DriverOrderSummary
import com.example.dacsiii_v2.data.model.DriverProfile
import com.example.dacsiii_v2.data.model.DriverProfileUpdateRequest
import com.example.dacsiii_v2.data.model.DriverWallet
import com.example.dacsiii_v2.data.model.Order
import com.example.dacsiii_v2.data.model.OrderDetailResponse
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.DriverRepository
import com.example.dacsiii_v2.data.repository.OrderRepository
import com.example.dacsiii_v2.data.repository.UserRepository
import com.example.dacsiii_v2.data.model.ProfileUpdateRequest
import com.example.dacsiii_v2.service.NotificationHelper
import com.example.dacsiii_v2.service.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

class DriverViewModel : ViewModel() {
    private val driverRepository = DriverRepository(RetrofitClient.api)
    private val orderRepository = OrderRepository(RetrofitClient.api)
    private val userRepository = UserRepository(RetrofitClient.api)

    private var appContext: Context? = null
    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private var locationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var socket: Socket? = null
    private var lastFetchTimeMs = 0L
    private val realtimeStarted = AtomicBoolean(false)
    private var socketOnlineSent = false

    fun fetchProfile(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = driverRepository.getProfile(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    val profile = response.profile
                    if (socket?.connected() == true) {
                        emitDriverOnline(profile.user_id, _uiState.value.currentLat, _uiState.value.currentLng)
                        emitDriverPresence(profile.user_id, profileLat = _uiState.value.currentLat, profileLng = _uiState.value.currentLng)
                    }
                    appContext?.let { context ->
                        LocationService.start(context, profile.user_id)
                    }
                    _uiState.value.copy(isLoading = false, profile = profile)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun updateProfile(token: String, request: DriverProfileUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = driverRepository.updateProfile(token, request)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isLoading = false, message = response.message)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun fetchWallet(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = driverRepository.getWallet(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isLoading = false, wallet = response.wallet)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun fetchAvailableOrders(token: String, lat: Double?, lng: Double?, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            val result = if (isValidLatitude(lat) && isValidLongitude(lng)) {
                driverRepository.getAvailableOrdersByLocation(token, lat!!, lng!!)
            } else {
                driverRepository.getAvailableOrders(token)
            }

            _uiState.value = result.fold(
                onSuccess = { response ->
                    val sortedOrders = response.orders.sortedWith(
                        compareBy<DriverOrderSummary> { it.distance_from_driver == null }
                            .thenBy { it.distance_from_driver ?: Double.MAX_VALUE }
                    )
                    _uiState.value.copy(isLoading = false, availableOrders = sortedOrders)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun refreshAvailableOrders(token: String, showLoading: Boolean = false) {
        val lat = _uiState.value.currentLat
        val lng = _uiState.value.currentLng
        fetchAvailableOrders(token, lat, lng, showLoading)
    }

    fun fetchActiveOrders(token: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            val result = driverRepository.getActiveOrders(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isLoading = false, activeOrders = response.orders)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun fetchOrderHistory(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = driverRepository.getOrderHistory(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isLoading = false, historyOrders = response.orders)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun acceptOrder(token: String, orderId: Int) {
        viewModelScope.launch {
            val result = driverRepository.acceptOrder(token, orderId)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        message = response.message,
                        availableOrders = _uiState.value.availableOrders.filterNot { it.order_id == orderId }
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(message = error.message)
                }
            )
            if (result.isSuccess) {
                fetchActiveOrders(token, showLoading = false)
                refreshAvailableOrders(token)
            }
        }
    }

    fun arrivePickup(token: String, orderId: Int) {
        viewModelScope.launch {
            val result = driverRepository.arrivePickup(token, orderId)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(message = response.message)
                },
                onFailure = { error ->
                    _uiState.value.copy(message = error.message)
                }
            )
            if (result.isSuccess) {
                fetchOrderDetails(token, orderId)
                fetchActiveOrders(token, showLoading = false)
            }
        }
    }

    fun uploadPickupPhoto(token: String, context: Context, orderId: Int, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPhotoUploading = true)
            val result = driverRepository.uploadPickupPhoto(token, context, orderId, uri)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isPhotoUploading = false, message = response.message)
                },
                onFailure = { error ->
                    _uiState.value.copy(isPhotoUploading = false, message = error.message)
                }
            )
            if (result.isSuccess) {
                fetchOrderDetails(token, orderId)
                fetchActiveOrders(token, showLoading = false)
            }
        }
    }

    fun arriveDelivery(token: String, orderId: Int) {
        viewModelScope.launch {
            val result = driverRepository.arriveDelivery(token, orderId)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(message = response.message)
                },
                onFailure = { error ->
                    _uiState.value.copy(message = error.message)
                }
            )
            if (result.isSuccess) {
                fetchOrderDetails(token, orderId)
                fetchActiveOrders(token, showLoading = false)
            }
        }
    }

    fun uploadDeliveryPhoto(token: String, context: Context, orderId: Int, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPhotoUploading = true)
            val result = driverRepository.uploadDeliveryPhoto(token, context, orderId, uri)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isPhotoUploading = false, message = response.message)
                },
                onFailure = { error ->
                    _uiState.value.copy(isPhotoUploading = false, message = error.message)
                }
            )
            if (result.isSuccess) {
                fetchOrderDetails(token, orderId)
                fetchActiveOrders(token, showLoading = false)
            }
        }
    }

    fun fetchEarnings(token: String, range: String, date: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = driverRepository.getEarnings(token, range, date)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isLoading = false, earnings = response)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun fetchOrderDetails(token: String, orderId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOrderDetailLoading = true)
            val result = orderRepository.getOrderDetails(token, orderId)
            _uiState.value = result.fold(
                onSuccess = { response: OrderDetailResponse ->
                    _uiState.value.copy(isOrderDetailLoading = false, orderDetail = response.order)
                },
                onFailure = { error ->
                    _uiState.value.copy(isOrderDetailLoading = false, message = error.message)
                }
            )
        }
    }

    fun updateLocation(token: String, lat: Double?, lng: Double?, isOnline: Int = 1) {
        viewModelScope.launch {
            val result = driverRepository.updateLocation(token, lat, lng, isOnline)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(message = result.exceptionOrNull()?.message)
            }
        }
    }

    fun uploadIdentity(token: String, context: Context, cccdNumber: String, front: Uri, back: Uri, portrait: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIdentityUploading = true)
            val updateResult = userRepository.updateProfile(
                token,
                ProfileUpdateRequest(cccd_number = cccdNumber.trim())
            )

            if (updateResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isIdentityUploading = false,
                    message = updateResult.exceptionOrNull()?.message
                )
                return@launch
            }

            val uploadResult = userRepository.uploadIdentity(token, context, front, back, portrait)
            _uiState.value = uploadResult.fold(
                onSuccess = { response ->
                    fetchProfile(token)
                    _uiState.value.copy(
                        isIdentityUploading = false,
                        message = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isIdentityUploading = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun startRealtimeOrders(context: Context, token: String) {
        if (!realtimeStarted.compareAndSet(false, true)) {
            return
        }
        appContext = context.applicationContext
        if (_uiState.value.profile == null) {
            fetchProfile(token)
        } else {
            LocationService.start(appContext!!, _uiState.value.profile!!.user_id)
        }
        startLocationTracking(context, token)
        connectSocket()
    }

    fun stopRealtimeOrders() {
        locationCallback?.let { callback ->
            locationClient?.removeLocationUpdates(callback)
        }
        locationCallback = null
        locationClient = null
        appContext?.let { LocationService.stop(it) }
        socket?.disconnect()
        socket = null
        realtimeStarted.set(false)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking(context: Context, token: String) {
        if (locationClient != null) {
            return
        }

        locationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            locationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    applyLocationUpdate(token, location.latitude, location.longitude, forceFetch = true)
                }
            }

            val tokenSource = CancellationTokenSource()
            locationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                ?.addOnSuccessListener { location ->
                    if (location != null) {
                        applyLocationUpdate(token, location.latitude, location.longitude, forceFetch = true)
                    }
                }

            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000)
                .setMinUpdateIntervalMillis(10_000)
                .setMinUpdateDistanceMeters(10f)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    applyLocationUpdate(token, location.latitude, location.longitude, forceFetch = false)
                }
            }

            locationClient?.requestLocationUpdates(request, locationCallback!!, context.mainLooper)
        } catch (_: SecurityException) {
            _uiState.value = _uiState.value.copy(message = "Thiếu quyền truy cập vị trí")
        }
    }

    private fun applyLocationUpdate(token: String, lat: Double, lng: Double, forceFetch: Boolean) {
        if (!isValidLatitude(lat) || !isValidLongitude(lng)) {
            return
        }

        val isFirstLocation = _uiState.value.currentLat == null
        _uiState.value = _uiState.value.copy(currentLat = lat, currentLng = lng)
        emitDriverOnline(_uiState.value.profile?.user_id, lat, lng)
        emitDriverPresence(_uiState.value.profile?.user_id, lat, lng)

        if (socket?.connected() != true) {
            updateLocation(token, lat, lng, isOnline = 1)
        }

        val now = System.currentTimeMillis()
        if (forceFetch || isFirstLocation || (now - lastFetchTimeMs >= 15000)) {
            lastFetchTimeMs = now
            fetchAvailableOrders(token, lat, lng, showLoading = false)
        }
    }

    private fun connectSocket() {
        if (socket != null) {
            return
        }

        socket = IO.socket(RetrofitClient.SOCKET_URL).apply {
            on(Socket.EVENT_CONNECT) {
                socketOnlineSent = false
                emitDriverOnline(_uiState.value.profile?.user_id, _uiState.value.currentLat, _uiState.value.currentLng)
            }
            
            // Lắng nghe sự kiện thông báo mới từ MySQL (thay cho Firebase)
            on("new_db_notification") {
                _uiState.value = _uiState.value.copy(message = "Bạn có thông báo mới!")
                // Có thể tự động fetch lại danh sách thông báo ở đây nếu có UI list
            }

            on("new_order_nearby", Emitter.Listener { args ->
                val payload = args.firstOrNull() ?: return@Listener
                val json = when (payload) {
                    is JSONObject -> payload.toString()
                    else -> payload.toString()
                }
                val socketPayload = runCatching {
                    Gson().fromJson(json, SocketOrderPayload::class.java)
                }.getOrNull() ?: return@Listener

                val updatedOrder = socketPayload.order.copy(
                    distance_from_driver = socketPayload.distance_from_driver
                )

                val updatedList = _uiState.value.availableOrders.toMutableList()
                val existingIndex = updatedList.indexOfFirst { it.order_id == updatedOrder.order_id }
                if (existingIndex >= 0) {
                    updatedList[existingIndex] = updatedOrder
                } else {
                    updatedList.add(0, updatedOrder)
                }

                _uiState.value = _uiState.value.copy(availableOrders = updatedList)
                val distance = updatedOrder.distance_from_driver
                if (distance != null && distance <= 3.0) {
                    _uiState.value = _uiState.value.copy(message = "Co don hang moi gan ban")
                    appContext?.let { context ->
                        NotificationHelper.showOrderNotification(
                            context = context,
                            title = "Don hang moi",
                            body = "Don #${updatedOrder.order_id} gan vi tri cua ban",
                            type = "new_order",
                            orderId = updatedOrder.order_id.toString()
                        )
                    }
                }
            })
            connect()
        }
    }

    private fun emitDriverOnline(driverId: Int?, lat: Double?, lng: Double?) {
        if (driverId == null || lat == null || lng == null) {
            return
        }

        if (!socketOnlineSent) {
            socket?.emit(
                "driver:online",
                mapOf(
                    "driver_id" to driverId,
                    "lat" to lat,
                    "lng" to lng
                )
            )
            socketOnlineSent = true
        }
    }

    private fun emitDriverPresence(driverId: Int?, profileLat: Double?, profileLng: Double?) {
        if (driverId == null || profileLat == null || profileLng == null) {
            return
        }

        socket?.emit(
            "update_location",
            mapOf(
                "driver_id" to driverId,
                "lat" to profileLat,
                "lng" to profileLng,
                "is_online" to 1
            )
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun setDriverOnlineStatus(context: Context, token: String, isOnline: Int) {
        val profile = _uiState.value.profile ?: return
        appContext = context.applicationContext
        if (socket == null) {
            connectSocket()
        }
        val lat = _uiState.value.currentLat
        val lng = _uiState.value.currentLng

        if (isOnline == 1) {
            LocationService.start(appContext!!, profile.user_id)
            if (isValidLatitude(lat) && isValidLongitude(lng)) {
                emitDriverOnline(profile.user_id, lat, lng)
                emitDriverPresence(profile.user_id, lat, lng)
            }
        } else {
            LocationService.stop(appContext!!)
            socket?.emit(
                "driver:offline",
                mapOf("driver_id" to profile.user_id)
            )
        }

        _uiState.value = _uiState.value.copy(
            profile = profile.copy(is_online = isOnline)
        )

        updateLocation(token, lat, lng, isOnline)
    }
}

data class DriverUiState(
    val profile: DriverProfile? = null,
    val wallet: DriverWallet? = null,
    val availableOrders: List<DriverOrderSummary> = emptyList(),
    val activeOrders: List<DriverOrderSummary> = emptyList(),
    val historyOrders: List<DriverOrderSummary> = emptyList(),
    val earnings: DriverEarningsResponse? = null,
    val orderDetail: Order? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val isPhotoUploading: Boolean = false,
    val isOrderDetailLoading: Boolean = false,
    val isIdentityUploading: Boolean = false,
    val currentLat: Double? = null,
    val currentLng: Double? = null
)

private data class SocketOrderPayload(
    val order: DriverOrderSummary,
    @Suppress("PropertyName")
    val distance_from_driver: Double?
)

private fun isValidLatitude(value: Double?): Boolean {
    return value != null && value >= -90.0 && value <= 90.0
}

private fun isValidLongitude(value: Double?): Boolean {
    return value != null && value >= -180.0 && value <= 180.0
}
