package com.example.danhgiaphim.ui.film

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Genre
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.data.repository.AdminRepository
import com.example.danhgiaphim.data.repository.CloudinaryRepository
import com.example.danhgiaphim.data.repository.FilmInput
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddFilmViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ManageSubmitState())
    val state: StateFlow<ManageSubmitState> = _state.asStateFlow()

    fun addFilm(input: FilmInput, posterUri: Uri?) {
        val validation = input.validate()
        if (validation != null) {
            _state.value = ManageSubmitState(message = validation)
            return
        }
        if (posterUri == null) {
            _state.value = ManageSubmitState(message = "Vui lòng chọn ảnh poster")
            return
        }

        _state.value = ManageSubmitState(isLoading = true)
        viewModelScope.launch {
            try {
                val posterUrl = cloudinaryRepository.uploadImage(posterUri).url
                adminRepository.addFilm(input, posterUrl)
                _state.value = ManageSubmitState(message = "Thêm phim thành công", isSuccess = true)
            } catch (e: Exception) {
                _state.value = ManageSubmitState(message = "Thêm phim thất bại: ${e.message}")
            }
        }
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}

@HiltViewModel
class AddActorViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ManageSubmitState())
    val state: StateFlow<ManageSubmitState> = _state.asStateFlow()

    fun addActor(name: String, imageUri: Uri?) {
        if (name.isBlank()) {
            _state.value = ManageSubmitState(message = "Tên không được trống")
            return
        }
        if (imageUri == null) {
            _state.value = ManageSubmitState(message = "Vui lòng chọn ảnh")
            return
        }
        _state.value = ManageSubmitState(isLoading = true)
        viewModelScope.launch {
            try {
                val avatarUrl = cloudinaryRepository.uploadImage(imageUri).url
                adminRepository.addActor(name.trim(), avatarUrl)
                _state.value = ManageSubmitState(message = "Thêm diễn viên thành công", isSuccess = true)
            } catch (e: Exception) {
                _state.value = ManageSubmitState(message = "Thêm diễn viên thất bại")
            }
        }
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}

data class ManageSubmitState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EditFilmViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EditFilmUiState())
    val state: StateFlow<EditFilmUiState> = _state.asStateFlow()

    fun load(filmId: String) {
        viewModelScope.launch {
            try {
                val film = adminRepository.loadFilm(filmId)
                _state.value = EditFilmUiState(
                    film = film,
                    allGenres = adminRepository.loadGenres(),
                    allActors = adminRepository.loadActors(),
                    selectedGenres = film?.genre ?: emptyMap(),
                    selectedActors = film?.actor ?: emptyMap()
                )
            } catch (e: Exception) {
                _state.value = EditFilmUiState(message = "Lỗi tải dữ liệu")
            }
        }
    }

    fun setSelectedGenres(genres: Map<String, Genre>) {
        _state.value = _state.value.copy(selectedGenres = genres)
    }

    fun setSelectedActors(actors: Map<String, Actors>) {
        _state.value = _state.value.copy(selectedActors = actors)
    }

    fun save(filmId: String, input: FilmInput, imageUri: Uri?) {
        val validation = input.validate()
        if (validation != null) {
            _state.value = _state.value.copy(message = validation)
            return
        }

        val current = _state.value
        _state.value = current.copy(isSaving = true, message = null)
        viewModelScope.launch {
            try {
                val posterUrl = imageUri?.let { cloudinaryRepository.uploadImage(it).url }
                    ?: current.film?.posterURL.orEmpty()
                adminRepository.updateFilm(
                    filmId,
                    input,
                    posterUrl,
                    current.selectedGenres,
                    current.selectedActors
                )
                _state.value = current.copy(isSaving = false, message = "Cập nhật thành công!", isSaved = true)
            } catch (e: Exception) {
                _state.value = current.copy(isSaving = false, message = "Cập nhật thất bại!")
            }
        }
    }

    fun clearSaved() {
        _state.value = _state.value.copy(isSaved = false)
    }
}

data class EditFilmUiState(
    val film: Films? = null,
    val allGenres: List<Genre> = emptyList(),
    val allActors: List<Actors> = emptyList(),
    val selectedGenres: Map<String, Genre> = emptyMap(),
    val selectedActors: Map<String, Actors> = emptyMap(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class FilmRatingViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FilmRatingUiState())
    val state: StateFlow<FilmRatingUiState> = _state.asStateFlow()
    private var allComments = emptyList<Pair<Comments, Users>>()
    private var filteredComments = emptyList<Pair<Comments, Users>>()
    private var currentPage = 0
    private val pageSize = 10

    fun load(movieId: String) {
        viewModelScope.launch {
            try {
                val rating = adminRepository.loadRating(movieId)
                allComments = adminRepository.loadCommentsForFilm(movieId)
                filteredComments = allComments
                _state.value = FilmRatingUiState(rating = rating)
                publishPage()
            } catch (e: Exception) {
                _state.value = FilmRatingUiState(message = "Lỗi tải dữ liệu")
            }
        }
    }

    fun filter(query: String) {
        filteredComments = if (query.isBlank()) {
            allComments
        } else {
            allComments.filter { (_, user) -> user.username.contains(query, ignoreCase = true) }
        }
        currentPage = 0
        publishPage()
    }

    fun nextPage() {
        if ((currentPage + 1) * pageSize < filteredComments.size) {
            currentPage++
            publishPage()
        }
    }

    fun previousPage() {
        if (currentPage > 0) {
            currentPage--
            publishPage()
        }
    }

    fun updateRatePoint(movieId: String, ratePoint: Float) {
        viewModelScope.launch {
            try {
                adminRepository.updateRatePoint(movieId, ratePoint)
                _state.value = _state.value.copy(
                    rating = adminRepository.loadRating(movieId),
                    message = "Lưu thành công"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Lỗi khi lưu")
            }
        }
    }

    fun deleteComment(commentID: String, movieID: String) {
        viewModelScope.launch {
            try {
                adminRepository.deleteComment(commentID, movieID)
                allComments = allComments.filterNot { it.first.reviewID == commentID }
                filteredComments = filteredComments.filterNot { it.first.reviewID == commentID }
                _state.value = _state.value.copy(message = "Đã xóa bình luận")
                publishPage()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Xóa bình luận thất bại")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun publishPage() {
        val start = currentPage * pageSize
        val end = minOf(start + pageSize, filteredComments.size)
        val page = if (start < end) filteredComments.subList(start, end) else emptyList()
        val totalPages = if (filteredComments.isEmpty()) 1 else (filteredComments.size - 1) / pageSize + 1
        _state.value = _state.value.copy(
            comments = page,
            pageText = "Trang ${currentPage + 1}/$totalPages"
        )
    }
}

data class FilmRatingUiState(
    val rating: Rating = Rating(),
    val comments: List<Pair<Comments, Users>> = emptyList(),
    val pageText: String = "Trang 1/1",
    val message: String? = null
)
