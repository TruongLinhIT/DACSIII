package com.example.dacsiii_v2.ui.auth

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.ProfileUpdateRequest
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.AuthRepository
import com.example.dacsiii_v2.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository(RetrofitClient.api)
    private val userRepository = UserRepository(RetrofitClient.api)
    private var otpTimerJob: Job? = null

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        val trimmed = value.trim()
        val isValid = isValidEmail(trimmed)
        otpTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            email = trimmed,
            otp = "",
            message = null,
            canVerify = false,
            otpCountdown = 0,
            canResend = isValid,
            isEmailValid = isValid
        )
    }

    fun onOtpChange(value: String) {
        _uiState.value = _uiState.value.copy(otp = value.trim(), message = null)
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, message = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, message = null)
    }

    fun onCccdChange(value: String) {
        _uiState.value = _uiState.value.copy(cccdNumber = value, message = null)
    }

    fun onAvatarSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(avatarUri = uri, message = null)
    }

    fun onFrontSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(idCardFrontUri = uri, message = null)
    }

    fun onBackSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(idCardBackUri = uri, message = null)
    }

    fun onPortraitSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(portraitUri = uri, message = null)
    }

    fun sendOtp() {
        val email = _uiState.value.email.trim()
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(message = "Email không hợp lệ")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = authRepository.sendOtp(email)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        startOtpTimer(60)
                        _uiState.value.copy(
                            isLoading = false,
                            message = response.message,
                            canVerify = true
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            message = response.message,
                            canVerify = false,
                            canResend = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isLoading = false,
                        message = error.message,
                        canVerify = false,
                        canResend = true
                    )
                }
            )
        }
    }

    fun verifyOtp() {
        val email = _uiState.value.email.trim()
        val otp = _uiState.value.otp.trim()

        if (!isValidEmail(email) || otp.length != 6) {
            _uiState.value = _uiState.value.copy(message = "Vui lòng nhập OTP 6 số")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = authRepository.verifyOtp(email, otp)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value.copy(
                            isLoading = false,
                            token = response.token,
                            message = "Xác thực OTP thành công"
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Xác thực OTP thất bại"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun updateProfile() {
        val token = _uiState.value.token
        if (token.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(message = "Thiếu token, vui lòng xác thực OTP")
            return
        }

        val fullName = _uiState.value.fullName.trim()
        val password = _uiState.value.password.trim()
        val cccd = _uiState.value.cccdNumber.trim()
        if (fullName.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Vui lòng nhập họ tên")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(message = "Mật khẩu phải từ 6 ký tự")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val request = ProfileUpdateRequest(
                full_name = fullName,
                password = password,
                email = _uiState.value.email,
                cccd_number = cccd.takeIf { it.isNotBlank() },
                avatar_url = null
            )
            val result = userRepository.updateProfile(token, request)
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

    fun uploadAvatar(context: Context) {
        val token = _uiState.value.token
        val avatar = _uiState.value.avatarUri
        if (token.isNullOrBlank() || avatar == null) {
            _uiState.value = _uiState.value.copy(message = "Chưa chọn ảnh avatar")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = userRepository.uploadAvatar(token, context, avatar)
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

    fun uploadIdentity(context: Context) {
        val token = _uiState.value.token
        val front = _uiState.value.idCardFrontUri
        val back = _uiState.value.idCardBackUri
        val portrait = _uiState.value.portraitUri

        if (token.isNullOrBlank() || front == null || back == null || portrait == null) {
            _uiState.value = _uiState.value.copy(message = "Vui lòng chọn đủ 3 ảnh CCCD và chân dung")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = userRepository.uploadIdentity(token, context, front, back, portrait)
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

    private fun startOtpTimer(seconds: Int) {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            for (remaining in seconds downTo 1) {
                _uiState.value = _uiState.value.copy(
                    otpCountdown = remaining,
                    canResend = false
                )
                delay(1000)
            }
            _uiState.value = _uiState.value.copy(otpCountdown = 0, canResend = true)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

data class RegisterUiState(
    val email: String = "",
    val otp: String = "",
    val fullName: String = "",
    val password: String = "",
    val cccdNumber: String = "",
    val avatarUri: Uri? = null,
    val idCardFrontUri: Uri? = null,
    val idCardBackUri: Uri? = null,
    val portraitUri: Uri? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val canVerify: Boolean = false,
    val otpCountdown: Int = 0,
    val canResend: Boolean = false,
    val isEmailValid: Boolean = false
)
