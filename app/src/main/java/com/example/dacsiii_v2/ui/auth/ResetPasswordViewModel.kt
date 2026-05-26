package com.example.dacsiii_v2.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.ResetPasswordRequest
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {
    private val repository = AuthRepository(RetrofitClient.api)
    private var otpTimerJob: Job? = null

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        val trimmed = value.trim()
        _uiState.value = _uiState.value.copy(
            email = trimmed,
            isEmailValid = Patterns.EMAIL_ADDRESS.matcher(trimmed).matches(),
            message = null
        )
    }

    fun onOtpChange(value: String) {
        _uiState.value = _uiState.value.copy(otp = value.trim(), message = null)
    }

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, message = null)
    }

    fun sendOtp() {
        val email = _uiState.value.email
        if (!_uiState.value.isEmailValid) {
            _uiState.value = _uiState.value.copy(message = "Email không hợp lệ")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.sendOtp(email)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        startOtpTimer(60)
                        _uiState.value.copy(isLoading = false, message = "Mã OTP đã được gửi", step = 2)
                    } else {
                        _uiState.value.copy(isLoading = false, message = response.message)
                    }
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        if (state.otp.length != 6) {
            _uiState.value = _uiState.value.copy(message = "Vui lòng nhập OTP 6 số")
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(message = "Mật khẩu mới phải ít nhất 6 ký tự")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val request = ResetPasswordRequest(state.email, state.otp, state.newPassword)
            val result = repository.resetPassword(request)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isLoading = false, 
                        message = response.message,
                        isSuccess = response.success
                    )
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
                _uiState.value = _uiState.value.copy(otpCountdown = remaining)
                delay(1000)
            }
            _uiState.value = _uiState.value.copy(otpCountdown = 0)
        }
    }
}

data class ResetPasswordUiState(
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val isEmailValid: Boolean = false,
    val otpCountdown: Int = 0,
    val step: Int = 1, // 1: Nhập email, 2: Nhập OTP & MK mới
    val isSuccess: Boolean = false
)
