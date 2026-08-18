package com.example.danhgiaphim.ui.notification

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.Notification
import com.example.danhgiaphim.data.repository.NotificationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try {
                _state.value = NotificationUiState(notifications = notificationRepository.loadNotifications())
            } catch (e: Exception) {
                _state.value = NotificationUiState(message = "Lỗi tải dữ liệu")
            }
        }
    }
}

@HiltViewModel
class NotificationManageViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try {
                _state.value = NotificationUiState(notifications = notificationRepository.loadNotifications())
            } catch (e: Exception) {
                _state.value = NotificationUiState(message = "Lỗi tải dữ liệu")
            }
        }
    }

    fun add(title: String, content: String) {
        if (title.isBlank() || content.isBlank()) {
            _state.value = _state.value.copy(message = "Vui lòng nhập đầy đủ thông tin")
            return
        }
        viewModelScope.launch {
            try {
                notificationRepository.addNotification(title.trim(), content.trim())
                _state.value = _state.value.copy(message = "Gửi thông báo thành công")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Gửi thông báo thất bại")
            }
        }
    }

    fun update(notification: Notification) {
        viewModelScope.launch {
            try {
                notificationRepository.updateNotification(notification)
                _state.value = _state.value.copy(message = "Đã cập nhật")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Cập nhật thất bại")
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(id)
                _state.value = _state.value.copy(message = "Đã xoá")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Xoá thất bại")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val message: String? = null
)
