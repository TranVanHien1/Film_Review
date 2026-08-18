package com.example.danhgiaphim.ui.admin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.data.repository.AdminRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UserDetailUiState())
    val state: StateFlow<UserDetailUiState> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            try {
                _state.value = UserDetailUiState(user = adminRepository.loadUser(userId))
            } catch (e: Exception) {
                _state.value = UserDetailUiState(message = "Lỗi tải dữ liệu")
            }
        }
    }
}

data class UserDetailUiState(
    val user: Users? = null,
    val message: String? = null
)

@HiltViewModel
class FilmDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FilmDetailUiState())
    val state: StateFlow<FilmDetailUiState> = _state.asStateFlow()

    fun load(filmId: String) {
        viewModelScope.launch {
            try {
                _state.value = FilmDetailUiState(film = adminRepository.loadFilm(filmId))
            } catch (e: Exception) {
                _state.value = FilmDetailUiState(message = "Lỗi tải dữ liệu")
            }
        }
    }

    fun deleteFilm(filmId: String) {
        viewModelScope.launch {
            try {
                adminRepository.deleteFilm(filmId)
                _state.value = _state.value.copy(message = "Xóa phim thành công", deleted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Xóa phim thất bại")
            }
        }
    }

    fun clearDeleted() {
        _state.value = _state.value.copy(deleted = false)
    }
}

data class FilmDetailUiState(
    val film: Films? = null,
    val message: String? = null,
    val deleted: Boolean = false
)
