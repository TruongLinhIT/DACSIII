package com.example.dacsiii_v2.ui.customer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.DriverInfo
import com.example.dacsiii_v2.data.model.Order
import com.example.dacsiii_v2.data.model.UserMeResponse
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.UserRepository
import com.example.dacsiii_v2.data.model.CreateOrderRequest
import com.example.dacsiii_v2.data.repository.OrderRepository
import com.example.dacsiii_v2.data.model.ProfileUpdateRequest
import com.example.dacsiii_v2.data.model.ChangePasswordRequest
import com.example.dacsiii_v2.data.model.OrderDetailResponse
import com.example.dacsiii_v2.data.local.AddressBookStore
import com.example.dacsiii_v2.data.model.FavoriteAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class CustomerViewModel : ViewModel() {
    private val userRepository = UserRepository(RetrofitClient.api)
    private val orderRepository = OrderRepository(RetrofitClient.api)

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    fun fetchProfile(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = userRepository.getMe(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isLoading = false,
                        userProfile = response
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isLoading = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun updateProfile(token: String, request: ProfileUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = userRepository.updateProfile(token, request)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isLoading = false,
                        message = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isLoading = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun fetchOrders(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = orderRepository.getCustomerOrders(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isLoading = false,
                        orders = response.orders ?: emptyList()
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isLoading = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun createOrder(token: String, request: CreateOrderRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = orderRepository.createOrder(token, request)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = response.message ?: "Đặt hàng thành công!"
                    )
                    onSuccess()
                    _uiState.value
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isLoading = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun setPickupLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(pickupLat = lat, pickupLng = lng)
    }

    fun setDeliveryLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(deliveryLat = lat, deliveryLng = lng)
    }

    fun updateEstimate(weightKg: Double?) {
        val state = _uiState.value
        val pickupLat = state.pickupLat
        val pickupLng = state.pickupLng
        val deliveryLat = state.deliveryLat
        val deliveryLng = state.deliveryLng

        if (pickupLat == null || pickupLng == null || deliveryLat == null || deliveryLng == null) {
            _uiState.value = state.copy(distanceKm = null, totalPrice = null)
            return
        }

        val distanceKm = calculateDistanceKm(pickupLat, pickupLng, deliveryLat, deliveryLng)
        val totalPrice = if (weightKg == null) null else calculateTotalPrice(distanceKm, weightKg)
        _uiState.value = state.copy(distanceKm = distanceKm, totalPrice = totalPrice)
    }

    private fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return round(earthRadiusKm * c * 100) / 100
    }

    private fun calculateTotalPrice(distanceKm: Double, weightKg: Double): Double {
        val baseFare = 10000.0
        val pricePerKm = 5000.0
        val pricePerKg = 2000.0
        val total = baseFare + (distanceKm * pricePerKm) + (weightKg * pricePerKg)
        return round(total * 100) / 100
    }

    fun uploadOrderPhotoBefore(token: String, context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPhotoUploading = true)
            val result = orderRepository.uploadOrderPhotoBefore(token, context, uri)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isPhotoUploading = false,
                        orderPhotoUrl = response.photo_url,
                        message = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isPhotoUploading = false,
                        message = error.message
                    )
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
                    _uiState.value.copy(
                        isOrderDetailLoading = false,
                        orderDetail = response.order,
                        driverDetail = response.driver
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isOrderDetailLoading = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun loadFavorites(context: Context) {
        viewModelScope.launch {
            val favorites = AddressBookStore.getFavorites(context)
            _uiState.value = _uiState.value.copy(favoriteAddresses = favorites)
        }
    }

    fun addFavorite(context: Context, label: String, address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val normalizedAddress = address.trim()
            if (normalizedAddress.isBlank()) return@launch

            val current = AddressBookStore.getFavorites(context)
            val newFavorite = FavoriteAddress(
                id = System.currentTimeMillis().toString(),
                label = label.trim().ifBlank { "Yêu thích" },
                address = normalizedAddress,
                lat = lat,
                lng = lng
            )
            val updated = current.filterNot {
                it.address.equals(normalizedAddress, ignoreCase = true) &&
                    it.label.equals(newFavorite.label, ignoreCase = true)
            } + newFavorite

            AddressBookStore.saveFavorites(context, updated)
            _uiState.value = _uiState.value.copy(favoriteAddresses = updated)
        }
    }

    fun removeFavorite(context: Context, favoriteId: String) {
        viewModelScope.launch {
            val current = AddressBookStore.getFavorites(context)
            val updated = current.filterNot { it.id == favoriteId }
            AddressBookStore.saveFavorites(context, updated)
            _uiState.value = _uiState.value.copy(favoriteAddresses = updated)
        }
    }

    fun uploadAvatar(token: String, context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAvatarUploading = true)
            val result = userRepository.uploadAvatar(token, context, uri)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    val currentProfile = _uiState.value.userProfile
                    val updatedProfile = currentProfile?.copy(
                        user = currentProfile.user.copy(avatar_url = response.avatar_url)
                    )
                    _uiState.value.copy(
                        isAvatarUploading = false,
                        userProfile = updatedProfile,
                        message = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isAvatarUploading = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun sendChangePasswordOtp(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPasswordOtpSending = true)
            val result = userRepository.sendChangePasswordOtp(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isPasswordOtpSending = false,
                        message = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isPasswordOtpSending = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun changePasswordWithOtp(token: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPasswordChanging = true)
            val result = userRepository.changePasswordWithOtp(
                token,
                ChangePasswordRequest(otp = otp, newPassword = newPassword)
            )
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isPasswordChanging = false,
                        passwordChanged = true,
                        message = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isPasswordChanging = false,
                        message = error.message
                    )
                }
            )
        }
    }

    fun clearPasswordChanged() {
        _uiState.value = _uiState.value.copy(passwordChanged = false)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun uploadIdentity(token: String, context: Context, front: Uri, back: Uri, portrait: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIdentityUploading = true)
            val result = userRepository.uploadIdentity(token, context, front, back, portrait)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    val currentProfile = _uiState.value.userProfile
                    val updatedProfile = currentProfile?.copy(
                        user = currentProfile.user.copy(
                            id_card_front_url = response.files.id_card_front_url,
                            id_card_back_url = response.files.id_card_back_url,
                            portrait_url = response.files.portrait_url,
                            is_verified = response.files.is_verified
                        )
                    )
                    _uiState.value.copy(
                        isIdentityUploading = false,
                        userProfile = updatedProfile,
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
}

data class CustomerUiState(
    val userProfile: UserMeResponse? = null,
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,
    val orderPhotoUrl: String? = null,
    val isPhotoUploading: Boolean = false,
    val distanceKm: Double? = null,
    val totalPrice: Double? = null,
    val orderDetail: Order? = null,
    val driverDetail: DriverInfo? = null,
    val isOrderDetailLoading: Boolean = false,
    val favoriteAddresses: List<FavoriteAddress> = emptyList(),
    val isAvatarUploading: Boolean = false,
    val isPasswordOtpSending: Boolean = false,
    val isPasswordChanging: Boolean = false,
    val passwordChanged: Boolean = false,
    val isIdentityUploading: Boolean = false
)
