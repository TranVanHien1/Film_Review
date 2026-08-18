package com.example.danhgiaphim.ui.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.data.repository.CloudinaryRepository
import com.example.danhgiaphim.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UserUiState())
    val state: StateFlow<UserUiState> = _state.asStateFlow()

    fun load() {
        val uid = UserSession.uid ?: run {
            _state.value = UserUiState(message = "Không tìm thấy người dùng")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = UserUiState(user = userRepository.loadUser(uid))
            } catch (e: Exception) {
                _state.value = UserUiState(message = "Không tải được dữ liệu")
            }
        }
    }

    fun deleteAccount() {
        val uid = UserSession.uid ?: run {
            _state.value = _state.value.copy(message = "Chưa đăng nhập")
            return
        }
        viewModelScope.launch {
            try {
                userRepository.deleteCurrentAccount(uid)
                UserSession.uid = null
                _state.value = _state.value.copy(message = "Xóa tài khoản thành công", deleted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Xóa tài khoản thất bại: ${e.message}")
            }
        }
    }

    fun clearDeleted() {
        _state.value = _state.value.copy(deleted = false)
    }
}

data class UserUiState(
    val user: Users? = null,
    val message: String? = null,
    val deleted: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load() {
        val uid = UserSession.uid ?: run {
            _state.value = ProfileUiState(message = "Không tìm thấy người dùng")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = ProfileUiState(user = userRepository.loadUser(uid))
            } catch (e: Exception) {
                _state.value = ProfileUiState(message = "Không tải được dữ liệu")
            }
        }
    }

    fun save(username: String, gender: String, dateOfBirth: Long, avatarUri: Uri?) {
        val uid = UserSession.uid ?: run {
            _state.value = _state.value.copy(message = "Chưa đăng nhập")
            return
        }
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val upload = avatarUri?.let { cloudinaryRepository.uploadImage(it) }
                userRepository.updateProfile(
                    uid,
                    username,
                    gender,
                    dateOfBirth,
                    upload?.url,
                    upload?.publicId
                )
                _state.value = _state.value.copy(
                    isSaving = false,
                    message = "Cập nhật thành công",
                    saved = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, message = "Cập nhật thất bại")
            }
        }
    }

    fun clearSaved() {
        _state.value = _state.value.copy(saved = false)
    }
}

data class ProfileUiState(
    val user: Users? = null,
    val isSaving: Boolean = false,
    val message: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {
        val validation = validate(oldPass, newPass, confirmPass)
        if (validation != null) {
            _state.value = ChangePasswordUiState(message = validation)
            return
        }
        _state.value = ChangePasswordUiState(isLoading = true)
        viewModelScope.launch {
            try {
                userRepository.changePassword(oldPass, newPass)
                _state.value = ChangePasswordUiState(message = "Đổi mật khẩu thành công", changed = true)
            } catch (e: Exception) {
                _state.value = ChangePasswordUiState(message = "Mật khẩu cũ không đúng")
            }
        }
    }

    private fun validate(oldPass: String, newPass: String, confirmPass: String): String? {
        return when {
            oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank() ->
                "Vui lòng nhập đầy đủ thông tin"
            newPass != confirmPass -> "Mật khẩu mới không khớp"
            newPass.length < 6 || !newPass.first().isUpperCase() ->
                "Mật khẩu phải có ít nhất 6 kí tự và viết hoa chữ cái đầu tiên!"
            else -> null
        }
    }
}

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val changed: Boolean = false
)

@HiltViewModel
class LikeCommentViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LikeCommentUiState())
    val state: StateFlow<LikeCommentUiState> = _state.asStateFlow()

    fun load() {
        val uid = UserSession.uid ?: run {
            _state.value = LikeCommentUiState(message = "Người dùng chưa đăng nhập")
            return
        }
        viewModelScope.launch {
            try {
                val comments = userRepository.loadUserComments(uid)
                _state.value = LikeCommentUiState(
                    comments = comments,
                    message = if (comments.isEmpty()) "Bạn chưa đăng bình luận nào." else null
                )
            } catch (e: Exception) {
                _state.value = LikeCommentUiState(message = "Lỗi tải bình luận")
            }
        }
    }
}

data class LikeCommentUiState(
    val comments: List<Triple<Comments, Users, String>> = emptyList(),
    val message: String? = null
)
