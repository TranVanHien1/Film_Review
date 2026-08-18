package com.example.danhgiaphim.ui.film

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.data.repository.FilmRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Locale

@HiltViewModel
class FilmProfileViewModel @Inject constructor(
    private val filmRepository: FilmRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FilmProfileUiState())
    val state: StateFlow<FilmProfileUiState> = _state.asStateFlow()

    private var filmId: String = ""
    private var lastCommentKey: String? = null
    private var isSortDescending = true
    private val loadedComments = mutableListOf<Pair<Comments, Users>>()

    fun load(filmId: String) {
        this.filmId = filmId
        if (filmId.isBlank()) {
            _state.value = FilmProfileUiState(message = "Không tìm thấy ID phim!")
            return
        }

        _state.value = _state.value.copy(isLoading = true, message = null)
        viewModelScope.launch {
            try {
                val profile = filmRepository.loadFilmProfile(filmId)
                val uid = UserSession.uid
                val canRate = uid?.let { !filmRepository.hasUserRatedFilm(filmId, it) } ?: false
                val likedCommentIds = uid?.let { filmRepository.loadLikedCommentIds(it) } ?: emptySet()
                _state.value = _state.value.copy(
                    isLoading = false,
                    film = profile.film,
                    rating = profile.rating,
                    canRate = canRate,
                    likedCommentIds = likedCommentIds
                )
                refreshComments()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = "Lỗi tải dữ liệu: ${e.message}"
                )
            }
        }
    }

    fun toggleCommentLike(comment: Comments) {
        val uid = UserSession.uid ?: run {
            _state.value = _state.value.copy(message = "Vui lòng đăng nhập để thích bình luận")
            return
        }
        viewModelScope.launch {
            try {
                val likeResult = filmRepository.toggleCommentLike(uid, comment)
                val likedCommentIds = if (likeResult.isLiked) {
                    _state.value.likedCommentIds + likeResult.commentId
                } else {
                    _state.value.likedCommentIds - likeResult.commentId
                }
                val updatedComments = loadedComments.map { (itemComment, user) ->
                    if (itemComment.reviewID == likeResult.commentId) {
                        itemComment.copy(like = likeResult.likeCount) to user
                    } else {
                        itemComment to user
                    }
                }
                loadedComments.clear()
                loadedComments.addAll(updatedComments)
                _state.value = _state.value.copy(likedCommentIds = likedCommentIds)
                publishComments(_state.value.hasMoreComments)
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Không cập nhật được lượt thích")
            }
        }
    }

    fun loadMoreComments() {
        if (_state.value.isLoadingComments || filmId.isBlank()) return

        _state.value = _state.value.copy(isLoadingComments = true)
        viewModelScope.launch {
            try {
                val page = filmRepository.loadCommentPage(filmId, lastCommentKey, COMMENT_PAGE_SIZE)
                lastCommentKey = page.nextKey
                loadedComments.addAll(page.comments)
                publishComments(hasMoreComments = page.hasMore)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingComments = false,
                    message = "Lỗi tải bình luận: ${e.message}"
                )
            }
        }
    }

    fun submitReview(
        contentRating: Float,
        effectRating: Float,
        castRating: Float,
        commentText: String
    ) {
        val uid = UserSession.uid
        if (uid == null) {
            _state.value = _state.value.copy(message = "Vui lòng đăng nhập để đánh giá")
            return
        }
        if (commentText.isBlank()) {
            _state.value = _state.value.copy(message = "Vui lòng nhập bình luận")
            return
        }

        _state.value = _state.value.copy(isSubmittingReview = true, message = null)
        viewModelScope.launch {
            try {
                filmRepository.submitReview(
                    filmId = filmId,
                    uid = uid,
                    contentRating = contentRating,
                    effectRating = effectRating,
                    castRating = castRating,
                    commentText = commentText.trim()
                )
                val profile = filmRepository.loadFilmProfile(filmId)
                _state.value = _state.value.copy(
                    isSubmittingReview = false,
                    rating = profile.rating,
                    canRate = false,
                    shouldDismissRatingDialog = true,
                    message = "Đánh giá đã được gửi!"
                )
                refreshComments()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmittingReview = false,
                    message = e.message ?: "Không gửi được đánh giá"
                )
            }
        }
    }

    fun toggleSortDate() {
        isSortDescending = !isSortDescending
        publishComments(_state.value.hasMoreComments)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun clearDismissRatingDialog() {
        _state.value = _state.value.copy(shouldDismissRatingDialog = false)
    }

    private fun refreshComments() {
        loadedComments.clear()
        lastCommentKey = null
        _state.value = _state.value.copy(comments = emptyList(), hasMoreComments = true)
        loadMoreComments()
    }

    private fun publishComments(hasMoreComments: Boolean) {
        val sorted = loadedComments.sortedBy { (comment, _) -> commentSortValue(comment) }
            .let { if (isSortDescending) it.reversed() else it }
        _state.value = _state.value.copy(
            comments = sorted,
            isLoadingComments = false,
            hasMoreComments = hasMoreComments,
            isSortDescending = isSortDescending
        )
    }

    private fun commentSortValue(comment: Comments): Long {
        if (comment.reviewTimestamp > 0L) return comment.reviewTimestamp
        return runCatching {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .parse(comment.reviewDate)
                ?.time ?: 0L
        }.getOrDefault(0L)
    }

    companion object {
        private const val COMMENT_PAGE_SIZE = 6
    }
}

data class FilmProfileUiState(
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val message: String? = null,
    val film: Films? = null,
    val rating: Rating = Rating(),
    val comments: List<Pair<Comments, Users>> = emptyList(),
    val likedCommentIds: Set<String> = emptySet(),
    val canRate: Boolean = false,
    val hasMoreComments: Boolean = true,
    val isSortDescending: Boolean = true,
    val shouldDismissRatingDialog: Boolean = false
)
