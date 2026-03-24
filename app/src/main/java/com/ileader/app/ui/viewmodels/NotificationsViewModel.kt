package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.NotificationDto
import com.ileader.app.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val repo = NotificationRepository()

    private val _state = MutableStateFlow<UiState<List<NotificationDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<NotificationDto>>> = _state

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _showOnlyUnread = MutableStateFlow(false)
    val showOnlyUnread: StateFlow<Boolean> = _showOnlyUnread

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val notifications = repo.getNotifications(userId)
                _state.value = UiState.Success(notifications)
                _unreadCount.value = notifications.count { !it.read }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun loadUnreadCount(userId: String) {
        viewModelScope.launch {
            try {
                _unreadCount.value = repo.getUnreadCount(userId)
            } catch (_: Exception) {}
        }
    }

    fun toggleFilter() {
        _showOnlyUnread.value = !_showOnlyUnread.value
    }

    fun markAsRead(notificationId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.markAsRead(notificationId)
                load(userId)
            } catch (_: Exception) {}
        }
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            try {
                repo.markAllAsRead(userId)
                load(userId)
            } catch (_: Exception) {}
        }
    }

    fun deleteNotification(notificationId: String, userId: String) {
        viewModelScope.launch {
            try {
                repo.deleteNotification(notificationId)
                load(userId)
            } catch (_: Exception) {}
        }
    }
}
