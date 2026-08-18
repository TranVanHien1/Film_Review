package com.example.danhgiaphim.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.repository.AuthRepository
import com.example.danhgiaphim.data.repository.LoginResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(message = "Vui lòng nhập đầy đủ thông tin")
            return
        }

        _state.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            try {
                handleLoginResult(authRepository.login(email.trim(), password))
            } catch (e: Exception) {
                _state.value = LoginUiState(message = "Sai tài khoản hoặc mật khẩu!")
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _state.value = _state.value.copy(message = "Vui lòng nhập email")
            return
        }

        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                authRepository.sendPasswordReset(email.trim())
                _state.value = LoginUiState(message = "Đã gửi email đặt lại mật khẩu")
            } catch (e: Exception) {
                _state.value = LoginUiState(message = "Lỗi: ${e.message}")
            }
        }
    }

    fun clearNavigation() {
        _state.value = _state.value.copy(destination = null)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun handleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Admin -> {
                UserSession.uid = result.uid
                _state.value = LoginUiState(destination = LoginDestination.Admin)
            }
            is LoginResult.User -> {
                UserSession.uid = result.uid
                _state.value = LoginUiState(destination = LoginDestination.Home)
            }
            LoginResult.EmailNotVerified -> {
                _state.value = LoginUiState(
                    message = "Tài khoản chưa xác minh email. Email xác minh đã được gửi lại."
                )
            }
            LoginResult.InvalidRole -> {
                _state.value = LoginUiState(message = "Vai trò người dùng không hợp lệ.")
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val destination: LoginDestination? = null
)

enum class LoginDestination {
    Admin,
    Home
}
