package com.example.danhgiaphim.data.repository

import android.util.Log
import com.example.danhgiaphim.API.ProfanityFilter
import com.example.danhgiaphim.API.RetrofitClient
import com.example.danhgiaphim.API.SentimentAnalysisRequest
import com.example.danhgiaphim.API.ToxicCheckRequest
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FilmRepository @Inject constructor() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val filmsRef = database.getReference("Films")
    private val commentsRef = database.getReference("Comments")
    private val ratingsRef = database.getReference("Rating")
    private val usersRef = database.getReference("Users")

    suspend fun loadFilmProfile(filmId: String): FilmProfileData = coroutineScope {
        val filmDeferred = async { filmsRef.child(filmId).get().await().getValue(Films::class.java) }
        val ratingDeferred = async { ratingsRef.child(filmId).get().await().getValue(Rating::class.java) }
        FilmProfileData(
            film = filmDeferred.await(),
            rating = ratingDeferred.await() ?: Rating(movieID = filmId)
        )
    }

    suspend fun loadLikedCommentIds(uid: String): Set<String> {
        return usersRef.child(uid).child("likedComments").get().await().children
            .mapNotNull { it.key }
            .toSet()
    }

    suspend fun hasUserRatedFilm(filmId: String, uid: String): Boolean = coroutineScope {
        val commentIds = filmsRef.child(filmId).child("Comment").get().await().children
            .mapNotNull { it.key }
        commentIds.map { commentId ->
            async {
                commentsRef.child(commentId).child("userID").get().await()
                    .getValue(String::class.java) == uid
            }
        }.awaitAll().any { it }
    }

    suspend fun loadCommentPage(
        filmId: String,
        lastCommentKey: String?,
        pageSize: Int
    ): CommentPage = coroutineScope {
        val filmCommentsRef = filmsRef.child(filmId).child("Comment")
        val queryLimit = pageSize + 1
        val query = if (lastCommentKey == null) {
            filmCommentsRef.orderByKey().limitToLast(queryLimit)
        } else {
            filmCommentsRef.orderByKey().endBefore(lastCommentKey).limitToLast(queryLimit)
        }

        val snapshot = query.get().await()
        val queriedCommentIds = snapshot.children.mapNotNull { it.key }
        if (queriedCommentIds.isEmpty()) {
            return@coroutineScope CommentPage(emptyList(), lastCommentKey, false)
        }

        val hasMore = queriedCommentIds.size > pageSize
        val commentIds = if (hasMore) queriedCommentIds.takeLast(pageSize) else queriedCommentIds
        val comments = commentIds.map { id ->
            async { loadCommentWithUser(id) }
        }.awaitAll().filterNotNull()

        CommentPage(
            comments = comments,
            nextKey = commentIds.firstOrNull(),
            hasMore = hasMore
        )
    }

    suspend fun submitReview(
        filmId: String,
        uid: String,
        contentRating: Float,
        effectRating: Float,
        castRating: Float,
        commentText: String
    ) {
        if (ProfanityFilter.containsProfanity(commentText)) {
            throw IllegalArgumentException("Bình luận chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa.")
        }
        if (hasUserRatedFilm(filmId, uid)) {
            throw IllegalStateException("Bạn đã đánh giá phim này.")
        }

        val toxicResponse = RetrofitClient.toxicApi.checkComment(ToxicCheckRequest(commentText))
        if (toxicResponse.is_toxic) {
            throw IllegalArgumentException("Bình luận chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa.")
        }

        val sentimentResponse = runCatching {
            RetrofitClient.sentimentApi.analyzeSentiment(SentimentAnalysisRequest(commentText))
        }.getOrNull()

        val reviewID = commentsRef.push().key ?: throw IllegalStateException("Không tạo được reviewID")
        val now = System.currentTimeMillis()
        val comment = Comments(
            filmID = filmId,
            reviewID = reviewID,
            userID = uid,
            contentRating = contentRating,
            effectRating = effectRating,
            castRating = castRating,
            comment = commentText,
            like = 0,
            reviewDate = formatDate(now),
            reviewTimestamp = now,
            sentimentLabel = sentimentResponse?.prediction?.let(::sentimentLabel) ?: "unknown"
        )

        database.reference.updateChildren(
            mapOf(
                "Comments/$reviewID" to comment,
                "Films/$filmId/Comment/$reviewID" to true
            )
        ).await()

        val aiRatingAdjustment = when (sentimentResponse?.prediction) {
            1 -> -0.25f
            2 -> 1.0f
            else -> 0f
        }
        updateFilmRating(filmId, contentRating, effectRating, castRating, aiRatingAdjustment)
    }

    suspend fun toggleCommentLike(uid: String, comment: Comments): CommentLikeResult {
        val commentId = comment.reviewID
        val userLikeRef = usersRef.child(uid).child("likedComments").child(commentId)
        val isLiked = userLikeRef.get().await().exists()
        val nextLikeCount = updateCommentLikeCount(commentId, isLiked)
        val nextLiked = !isLiked
        val likeValue = if (nextLiked) true else null
        database.reference.updateChildren(
            mapOf(
                "Likes/$commentId/$uid" to likeValue,
                "Users/$uid/likedComments/$commentId" to likeValue
            )
        ).await()
        return CommentLikeResult(commentId, nextLiked, nextLikeCount)
    }

    private suspend fun loadCommentWithUser(commentId: String): Pair<Comments, Users>? {
        val comment = commentsRef.child(commentId).get().await().getValue(Comments::class.java)
            ?: return null
        val user = usersRef.child(comment.userID).get().await().getValue(Users::class.java)
            ?: return null
        return comment to user
    }

    private suspend fun updateFilmRating(
        filmId: String,
        contentRating: Float,
        effectRating: Float,
        castRating: Float,
        aiRatingAdjustment: Float
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        ratingsRef.child(filmId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val rating = currentData.getValue(Rating::class.java) ?: Rating(movieID = filmId)
                val reviewCount = rating.reviewCount + 1
                val newContentRating =
                    (rating.contentRating * rating.reviewCount + contentRating) / reviewCount
                val newEffectRating =
                    (rating.effectRating * rating.reviewCount + effectRating) / reviewCount
                val newCastRating =
                    (rating.castRating * rating.reviewCount + castRating) / reviewCount
                val averageRating = (newContentRating + newEffectRating + newCastRating) / 3
                val adjustedRating = averageRating + aiRatingAdjustment

                currentData.value = rating.copy(
                    contentRating = newContentRating,
                    effectRating = newEffectRating,
                    castRating = newCastRating,
                    rating = adjustedRating.coerceIn(0f, 5f),
                    reviewCount = reviewCount,
                    rateAI = rating.rateAI + aiRatingAdjustment
                )

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (!continuation.isActive) return
                when {
                    error != null -> {
                        Log.e("RatingUpdate", "Lỗi cập nhật rating: ${error.message}")
                        continuation.resumeWithException(error.toException())
                    }
                    !committed -> continuation.resumeWithException(
                        IllegalStateException("Không thể cập nhật rating")
                    )
                    else -> continuation.resume(Unit)
                }
            }
        })
    }

    private suspend fun updateCommentLikeCount(
        commentId: String,
        isLiked: Boolean
    ): Long = suspendCancellableCoroutine { continuation ->
        commentsRef.child(commentId).child("like").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentLike = currentData.getValue(Long::class.java) ?: 0L
                currentData.value = if (isLiked) {
                    (currentLike - 1).coerceAtLeast(0L)
                } else {
                    currentLike + 1
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (!continuation.isActive) return
                when {
                    error != null -> continuation.resumeWithException(error.toException())
                    !committed -> continuation.resumeWithException(
                        IllegalStateException("Khong the cap nhat luot thich")
                    )
                    else -> continuation.resume(currentData?.getValue(Long::class.java) ?: 0L)
                }
            }
        })
    }

    private fun sentimentLabel(prediction: Int): String {
        return when (prediction) {
            0 -> "toxic"
            1 -> "negative"
            2 -> "positive"
            else -> "neutral"
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

data class FilmProfileData(
    val film: Films?,
    val rating: Rating
)

data class CommentPage(
    val comments: List<Pair<Comments, Users>>,
    val nextKey: String?,
    val hasMore: Boolean
)

data class CommentLikeResult(
    val commentId: String,
    val isLiked: Boolean,
    val likeCount: Long
)
