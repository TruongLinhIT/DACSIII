package com.example.dacsiii_v2.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.LoginRequest
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository(RetrofitClient.api)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        val trimmed = value.trim()
        _uiState.value = _uiState.value.copy(
            email = trimmed,
            isEmailValid = Patterns.EMAIL_ADDRESS.matcher(trimmed).matches(),
            message = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            message = null
        )
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (!_uiState.value.isEmailValid) {
            _uiState.value = _uiState.value.copy(message = "Email không hợp lệ")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(message = "Mật khẩu ít nhất 6 ký tự")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.login(LoginRequest(email, password))
            _uiState.value = result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value.copy(
                            isLoading = false,
                            token = response.token,
                            userRole = response.user.role, // Lưu lại role để phân quyền
                            message = "Đăng nhập thành công"
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Đăng nhập thất bại"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isLoading = false,
                        message = error.message ?: "Lỗi kết nối server"
                    )
                }
            )
        }
    }

    fun clearToken() {
        _uiState.value = _uiState.value.copy(token = null)
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val userRole: String? = null, // Thêm role vào UI State
    val isEmailValid: Boolean = false
)
