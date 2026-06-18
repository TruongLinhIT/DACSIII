package com.example.dacsiii_v2.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.UserProfile
import com.example.dacsiii_v2.data.model.UserSummary
import com.example.dacsiii_v2.data.model.VerifyIdentityRequest
import com.example.dacsiii_v2.data.model.CommissionSummaryResponse
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.example.dacsiii_v2.data.repository.AdminRepository
import com.example.dacsiii_v2.data.repository.ReportRepository
import com.example.dacsiii_v2.data.model.DriverReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository(RetrofitClient.api)
    private val reportRepository = ReportRepository(RetrofitClient.api)

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

    fun lockUser(token: String, userId: Int, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.lockUser(token, userId, reason)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    val currentSelected = _uiState.value.selectedUser
                    val updatedUser = if (currentSelected?.user_id == userId) {
                        currentSelected.copy(is_locked = true)
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

    fun unlockUser(token: String, userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.unlockUser(token, userId)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    val currentSelected = _uiState.value.selectedUser
                    val updatedUser = if (currentSelected?.user_id == userId) {
                        currentSelected.copy(is_locked = false)
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

    fun revokeEkyc(token: String, userId: Int, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.revokeEkyc(token, userId, reason)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    val currentSelected = _uiState.value.selectedUser
                    val updatedUser = if (currentSelected?.user_id == userId) {
                        currentSelected.copy(
                            is_verified = "unverified",
                            id_card_front_url = null,
                            id_card_back_url = null,
                            portrait_url = null,
                            cccd_number = null,
                            identity_reject_reason = "Hủy duyệt: $reason"
                        )
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

    fun fetchCommissionSummary(token: String, range: String, date: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCommissionLoading = true, message = null)
            val result = repository.getCommissionSummary(token, range, date)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isCommissionLoading = false, commissionSummary = response)
                },
                onFailure = { error ->
                    _uiState.value.copy(isCommissionLoading = false, message = error.message)
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun fetchReports(token: String, status: String? = null, search: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReportsLoading = true, message = null)
            val result = reportRepository.getAllReports(token, status, search)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(isReportsLoading = false, reports = response.reports)
                },
                onFailure = { error ->
                    _uiState.value.copy(isReportsLoading = false, message = error.message)
                }
            )
        }
    }

    fun resolveReport(token: String, reportId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReportsLoading = true, message = null)
            val result = reportRepository.resolveReport(token, reportId)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    onSuccess()
                    val updatedReports = _uiState.value.reports.map {
                        if (it.report_id == reportId) it.copy(status = "resolved") else it
                    }
                    _uiState.value.copy(
                        isReportsLoading = false,
                        reports = updatedReports,
                        message = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(isReportsLoading = false, message = error.message)
                }
            )
        }
    }
}

data class AdminUiState(
    val users: List<UserSummary> = emptyList(),
    val selectedUser: UserProfile? = null,
    val isLoading: Boolean = false,
    val isCommissionLoading: Boolean = false,
    val commissionSummary: CommissionSummaryResponse? = null,
    val message: String? = null,
    val reports: List<DriverReport> = emptyList(),
    val isReportsLoading: Boolean = false
)