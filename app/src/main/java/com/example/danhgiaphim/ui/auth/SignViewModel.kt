package com.example.danhgiaphim.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignUiState())
    val state: StateFlow<SignUiState> = _state.asStateFlow()

    fun register(email: String, password: String, repeatPassword: String) {
        val validationMessage = validate(email, password, repeatPassword)
        if (validationMessage != null) {
            _state.value = SignUiState(message = validationMessage)
            return
        }

        _state.value = SignUiState(isLoading = true)
        viewModelScope.launch {
            try {
                authRepository.register(email.trim(), password)
                _state.value = SignUiState(
                    message = "Tạo tài khoản thành công. Vui lòng xác minh email trước khi đăng nhập.",
                    isRegistered = true
                )
            } catch (e: Exception) {
                _state.value = SignUiState(message = e.message ?: "Tạo tài khoản thất bại")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun clearRegistered() {
        _state.value = _state.value.copy(isRegistered = false)
    }

    private fun validate(email: String, password: String, repeatPassword: String): String? {
        return when {
            email.isBlank() || password.isBlank() || repeatPassword.isBlank() ->
                "Vui lòng nhập đầy đủ thông tin"
            password != repeatPassword -> "Mật khẩu nhập lại không đúng"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Địa chỉ email không hợp lệ!"
            password.length < 6 || !Character.isUpperCase(password[0]) ->
                "Mật khẩu phải có ít nhất 6 kí tự và viết hoa chữ cái đầu tiên!"
            else -> null
        }
    }
}

data class SignUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isRegistered: Boolean = false
)
