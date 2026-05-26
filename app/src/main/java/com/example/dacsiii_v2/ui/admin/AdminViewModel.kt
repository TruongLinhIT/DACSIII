package com.example.dacsiii_v2.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.UserProfile
import com.example.dacsiii_v2.data.model.UserSummary
import com.example.dacsiii_v2.data.model.VerifyIdentityRequest
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository(RetrofitClient.api)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun fetchUsers(token: String, role: String? = null, isVerified: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.getAllUsers(token, role, isVerified)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isLoading = false, users = response.users)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun fetchUserDetail(token: String, userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null, selectedUser = null)
            val result = repository.getUserDetail(token, userId)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isLoading = false, selectedUser = response.user)
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun verifyUser(token: String, userId: Int, status: String, reason: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.verifyUserIdentity(token, userId, VerifyIdentityRequest(status, reason))
            _uiState.value = result.fold(
                onSuccess = { response ->
                    // Cập nhật lại thông tin user hiện tại trong state nếu đang xem detail
                    val currentSelected = _uiState.value.selectedUser
                    val updatedUser = if (currentSelected?.user_id == userId) {
                        currentSelected.copy(is_verified = status, identity_reject_reason = reason)
                    } else currentSelected

                    _uiState.value.copy(
                        isLoading = false, 
                        message = response.message,
                        selectedUser = updatedUser
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(isLoading = false, message = error.message)
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class AdminUiState(
    val users: List<UserSummary> = emptyList(),
    val selectedUser: UserProfile? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)
