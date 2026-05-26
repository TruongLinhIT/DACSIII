package com.example.dacsiii_v2.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacsiii_v2.data.model.NotificationItem
import com.example.dacsiii_v2.data.repository.UserRepository
import com.example.dacsiii_v2.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

class NotificationViewModel : ViewModel() {
    private val repository = UserRepository(RetrofitClient.api)
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications(token: String, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getNotifications(token, status)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    _uiState.value.copy(
                        isLoading = false,
                        notifications = response.notifications
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

    fun markRead(token: String, notificationId: Int) {
        viewModelScope.launch {
            val result = repository.markNotificationRead(token, notificationId)
            _uiState.value = result.fold(
                onSuccess = {
                    val updated = _uiState.value.notifications.map { item ->
                        if (item.notification_id == notificationId) {
                            item.copy(is_read = 1)
                        } else {
                            item
                        }
                    }
                    _uiState.value.copy(notifications = updated)
                },
                onFailure = { error ->
                    _uiState.value.copy(message = error.message)
                }
            )
        }
    }

    fun markAllRead(token: String) {
        viewModelScope.launch {
            val result = repository.markAllNotificationsRead(token)
            _uiState.value = result.fold(
                onSuccess = {
                    val updated = _uiState.value.notifications.map { it.copy(is_read = 1) }
                    _uiState.value.copy(notifications = updated)
                },
                onFailure = { error ->
                    _uiState.value.copy(message = error.message)
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

