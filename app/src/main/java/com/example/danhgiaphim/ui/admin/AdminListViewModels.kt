package com.example.danhgiaphim.ui.admin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Genre
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.data.repository.AdminRepository
import com.example.danhgiaphim.data.repository.CloudinaryRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UserListUiState())
    val state: StateFlow<UserListUiState> = _state.asStateFlow()
    private var allUsers = emptyList<Users>()

    fun load() {
        viewModelScope.launch {
            try {
                allUsers = adminRepository.loadUsers()
                _state.value = UserListUiState(users = allUsers)
            } catch (e: Exception) {
                _state.value = UserListUiState(message = "Lỗi: ${e.message}")
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(
            users = if (query.isBlank()) allUsers else allUsers.filter {
                it.username.contains(query, ignoreCase = true)
            }
        )
    }
}

data class UserListUiState(
    val users: List<Users> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class FilmListViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FilmListUiState())
    val state: StateFlow<FilmListUiState> = _state.asStateFlow()
    private var allFilms = emptyList<Films>()

    fun load() {
        viewModelScope.launch {
            try {
                allFilms = adminRepository.loadFilms()
                _state.value = FilmListUiState(films = allFilms)
            } catch (e: Exception) {
                _state.value = FilmListUiState(message = "Lỗi: ${e.message}")
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(
            films = if (query.isBlank()) allFilms else allFilms.filter {
                it.title.contains(query, ignoreCase = true)
            }
        )
    }
}

data class FilmListUiState(
    val films: List<Films> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class GenreListViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(GenreListUiState())
    val state: StateFlow<GenreListUiState> = _state.asStateFlow()
    private var allGenres = emptyList<Genre>()
    private var isAscending = true

    fun load() {
        viewModelScope.launch {
            try {
                allGenres = adminRepository.loadGenres()
                _state.value = GenreListUiState(genres = allGenres)
            } catch (e: Exception) {
                _state.value = GenreListUiState(message = "Lỗi tải dữ liệu")
            }
        }
    }

    fun addGenre(name: String) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(message = "Tên thể loại không được trống")
            return
        }
        viewModelScope.launch {
            try {
                adminRepository.addGenre(name.trim())
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Thêm thất bại")
            }
        }
    }

    fun updateGenre(genreId: String, name: String) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(message = "Tên thể loại không được trống")
            return
        }
        viewModelScope.launch {
            try {
                adminRepository.updateGenre(genreId, name.trim())
                _state.value = _state.value.copy(message = "Cập nhật thành công")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Cập nhật thất bại")
            }
        }
    }

    fun deleteGenre(genreId: String) {
        viewModelScope.launch {
            try {
                adminRepository.deleteGenre(genreId)
                _state.value = _state.value.copy(message = "Đã xóa thể loại")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Lỗi khi xóa")
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(
            genres = if (query.isBlank()) allGenres else allGenres.filter {
                it.genreName.contains(query, ignoreCase = true)
            }
        )
    }

    fun sort() {
        val current = _state.value.genres
        val sorted = if (isAscending) {
            current.sortedBy { it.genreName.lowercase() }
        } else {
            current.sortedByDescending { it.genreName.lowercase() }
        }
        isAscending = !isAscending
        _state.value = _state.value.copy(genres = sorted)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}

data class GenreListUiState(
    val genres: List<Genre> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class ActorListViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ActorListUiState())
    val state: StateFlow<ActorListUiState> = _state.asStateFlow()
    private var allActors = emptyList<Actors>()

    fun load() {
        viewModelScope.launch {
            try {
                allActors = adminRepository.loadActors()
                _state.value = ActorListUiState(actors = allActors)
            } catch (e: Exception) {
                _state.value = ActorListUiState(message = "Lỗi: ${e.message}")
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(
            actors = if (query.isBlank()) allActors else allActors.filter {
                it.actorName.contains(query, ignoreCase = true)
            }
        )
    }

    fun updateActor(actor: Actors, name: String, imageUri: android.net.Uri?) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(message = "Tên không được trống")
            return
        }
        viewModelScope.launch {
            try {
                val avatarUrl = imageUri?.let { cloudinaryRepository.uploadImage(it).url } ?: actor.actorAvatarURL
                adminRepository.updateActor(actor.actorID, name.trim(), avatarUrl)
                _state.value = _state.value.copy(message = "Cập nhật thành công")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Cập nhật thất bại")
            }
        }
    }

    fun deleteActor(actorId: String) {
        viewModelScope.launch {
            try {
                adminRepository.deleteActor(actorId)
                _state.value = _state.value.copy(message = "Xóa thành công")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Xóa thất bại")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}

data class ActorListUiState(
    val actors: List<Actors> = emptyList(),
    val message: String? = null
)
