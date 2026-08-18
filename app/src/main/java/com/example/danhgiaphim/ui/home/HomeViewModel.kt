package com.example.danhgiaphim.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.repository.AuthRepository
import com.example.danhgiaphim.data.repository.HomeRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var allPairs: List<Pair<Films, Rating?>> = emptyList()
    private var sortedFilms: List<Films> = emptyList()
    private var query: String = ""
    private var selectedGenre: String? = null
    private var currentPage = 1
    private val filmsPerPage = 15
    private var currentSortMode = HomeSortMode.BY_RATING

    fun load() {
        _state.value = _state.value.copy(isLoading = true, message = null)
        viewModelScope.launch {
            try {
                val uid = UserSession.uid
                val user = uid?.let { homeRepository.loadUser(it) }
                val genres = homeRepository.loadGenres()
                val catalog = homeRepository.loadFilmCatalog()

                allPairs = catalog.films.map { it to catalog.ratings[it.movieID] }
                _state.value = _state.value.copy(
                    userName = user?.username.orEmpty(),
                    avatarUrl = user?.avatarURL.orEmpty(),
                    genres = genres,
                    ratings = catalog.ratings,
                    isLoading = false
                )
                applySortAndFilter(resetPage = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Không tải được dữ liệu trang chủ: ${e.message}"
                )
            }
        }
    }

    fun setQuery(value: String) {
        query = value
        applySortAndFilter(resetPage = true)
    }

    fun setGenre(genre: String?) {
        selectedGenre = genre
        applySortAndFilter(resetPage = true)
    }

    fun setSortMode(mode: HomeSortMode) {
        currentSortMode = mode
        applySortAndFilter(resetPage = true)
    }

    fun nextPage() {
        val totalPages = _state.value.totalPages
        if (currentPage < totalPages) {
            currentPage++
            publishPage()
        }
    }

    fun previousPage() {
        if (currentPage > 1) {
            currentPage--
            publishPage()
        }
    }

    fun signOut() {
        authRepository.signOut()
        UserSession.uid = null
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun applySortAndFilter(resetPage: Boolean) {
        val sortedPairs = when (currentSortMode) {
            HomeSortMode.BY_RATING -> allPairs.sortedWith(
                compareByDescending<Pair<Films, Rating?>> { it.second?.ratePoint ?: 0F }
                    .thenByDescending { it.second?.rating ?: 0F }
            )
            HomeSortMode.BY_AI_RATING -> allPairs.sortedWith(
                compareByDescending<Pair<Films, Rating?>> { it.second?.ratePoint ?: 0F }
                    .thenByDescending { it.second?.rateAI ?: 0F }
            )
        }

        sortedFilms = sortedPairs.map { it.first }.filter { film ->
            val matchTitle = film.title.contains(query, ignoreCase = true)
            val matchGenre = selectedGenre?.let { selected ->
                film.genre?.values?.any { it.genreName == selected } ?: false
            } ?: true
            matchTitle && matchGenre
        }

        if (resetPage) currentPage = 1
        publishPage()
    }

    private fun publishPage() {
        val totalPages = if (sortedFilms.isEmpty()) 1 else (sortedFilms.size + filmsPerPage - 1) / filmsPerPage
        if (currentPage > totalPages) currentPage = totalPages

        val pageFilms = if (sortedFilms.isEmpty()) {
            emptyList()
        } else {
            val startIndex = (currentPage - 1) * filmsPerPage
            val endIndex = minOf(startIndex + filmsPerPage, sortedFilms.size)
            sortedFilms.subList(startIndex, endIndex)
        }

        _state.value = _state.value.copy(
            films = pageFilms,
            featuredFilms = sortedFilms.take(4),
            selectedGenre = selectedGenre,
            sortMode = currentSortMode,
            currentPage = currentPage,
            totalPages = totalPages,
            canPrevious = currentPage > 1,
            canNext = currentPage < totalPages,
            isEmpty = sortedFilms.isEmpty()
        )
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val userName: String = "",
    val avatarUrl: String = "",
    val genres: List<String> = emptyList(),
    val ratings: Map<String, Rating> = emptyMap(),
    val films: List<Films> = emptyList(),
    val featuredFilms: List<Films> = emptyList(),
    val selectedGenre: String? = null,
    val sortMode: HomeSortMode = HomeSortMode.BY_RATING,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val canPrevious: Boolean = false,
    val canNext: Boolean = false,
    val isEmpty: Boolean = false
)

enum class HomeSortMode {
    BY_RATING,
    BY_AI_RATING
}
