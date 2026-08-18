package com.example.danhgiaphim.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.repository.AuthRepository
import com.example.danhgiaphim.data.repository.LoginResult
import com.example.danhgiaphim.ui.auth.LoginDestination
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    fun restoreSession() {
        _state.value = MainUiState(isLoading = true)
        viewModelScope.launch {
            try {
                when (val result = authRepository.restoreSession()) {
                    is LoginResult.Admin -> {
                        UserSession.uid = result.uid
                        _state.value = MainUiState(destination = LoginDestination.Admin)
                    }
                    is LoginResult.User -> {
                        UserSession.uid = result.uid
                        _state.value = MainUiState(destination = LoginDestination.Home)
                    }
                    LoginResult.EmailNotVerified -> _state.value = MainUiState()
                    LoginResult.InvalidRole -> _state.value = MainUiState(message = "Vai trò người dùng không hợp lệ.")
                    null -> _state.value = MainUiState()
                }
            } catch (e: Exception) {
                _state.value = MainUiState(message = "Không thể khôi phục phiên đăng nhập")
            }
        }
    }

    fun clearNavigation() {
        _state.value = _state.value.copy(destination = null)
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val destination: LoginDestination? = null
)
