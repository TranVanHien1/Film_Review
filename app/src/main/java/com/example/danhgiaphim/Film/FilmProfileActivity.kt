package com.example.danhgiaphim.Film

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.danhgiaphim.API.ProfanityFilter
import com.example.danhgiaphim.API.RetrofitClient
import com.example.danhgiaphim.API.SentimentAnalysisRequest
import com.example.danhgiaphim.API.ToxicCheckApi
import com.example.danhgiaphim.API.ToxicCheckRequest
import com.example.danhgiaphim.HomeActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.adapter.ActorAdapter
import com.example.danhgiaphim.adapter.CommentAdapter
import com.example.danhgiaphim.adapter.LoadingDialog
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ActivityFilmProfileBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FilmProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilmProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var filmID: String

    private var isLoadingComments = false
    private var lastCommentKey: String? = null
    private val commentsList = mutableListOf<Pair<Comments, Users>>()
    private lateinit var commentAdapter: CommentAdapter
    private var isSortDescending = true // Mặc định: sắp xếp mới nhất trước



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilmProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy filmID từ Session (Intent)
        filmID = FilmSession.filmid ?: ""
        commentAdapter = CommentAdapter(commentsList)
        binding.recyclerComments.adapter = commentAdapter
        binding.recyclerComments.layoutManager = LinearLayoutManager(this)


        if (filmID.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID phim!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Films")

        loadFilmDetails()
        checkIfUserHasRatedFilm(filmID)
        binding.btnLoadMore.setOnClickListener {
            if (!isLoadingComments) {
                loadMoreComments()
            }
        }

        loadMoreComments()

        binding.btnBackToHomeFromFilmProfile.setOnClickListener() {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        binding.buttonRate.setOnClickListener {
            showRatingDialog()
        }
        binding.btnSortDate.setOnClickListener {
            sortCommentsByDate()
        }


    }

    private fun loadFilmDetails() {
        database.child(filmID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val film = snapshot.getValue(Films::class.java)
                if (film != null) {
                    displayFilmData(film)
                } else {
                    Toast.makeText(
                        this@FilmProfileActivity,
                        "Không tìm thấy dữ liệu phim!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FilmProfileActivity, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun displayFilmData(film: Films) {
        binding.textTitle.text = film.title
        binding.textReleaseYear.text = "Năm phát hành: ${film.releaseYear}"
        binding.textDirector.text = "Đạo diễn: ${film.director}"
        binding.textGenre.text =
            "Thể loại: ${film.genre?.values?.joinToString { it.genreName } ?: "Không rõ"}"
        binding.textSynopsis.text = film.synopsis

        Glide.with(this)
            .load(film.posterURL)
            .placeholder(R.drawable.ic_user)
            .into(binding.imagePoster)

        // 👉 Hiển thị đánh giá

        loadRatingInfo(film.movieID)
        // 👉 Diễn viên (hiển thị trượt ngang)
        val actorList = film.actor?.values?.toList() ?: emptyList()
        val actorAdapter = ActorAdapter(actorList)
        binding.recyclerActors.apply {
            layoutManager = LinearLayoutManager(
                this@FilmProfileActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = actorAdapter
        }

        // 👉 Trailer
        binding.buttonWatchTrailer.setOnClickListener {
            if (film.trailerURL.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(film.trailerURL))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Trailer không khả dụng!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRatingInfo(movieID: String) {
        val ratingRef = FirebaseDatabase.getInstance().getReference("Rating").child(movieID)

        ratingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rating = snapshot.getValue(Rating::class.java)
                if (rating != null) {
                    // Giả sử rating từ 1-5, chia đôi nếu rating của bạn từ 1-10
                    binding.ratingBarContent.rating = rating.contentRating
                    binding.ratingBarEffect.rating = rating.effectRating
                    binding.ratingBarCast.rating = rating.castRating
                    binding.ratingBarOverall.rating = rating.rating
                    binding.textReviewCount.text = "Số lượt đánh giá: ${rating.reviewCount}"
                } else {
                    // Reset về 0 nếu không có rating
                    binding.ratingBarContent.rating = 0f
                    binding.ratingBarEffect.rating = 0f
                    binding.ratingBarCast.rating = 0f
                    binding.ratingBarOverall.rating = 0f
                    binding.textReviewCount.text = "Số lượt đánh giá: 0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FilmProfileActivity, "Lỗi tải đánh giá!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showRatingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rate_movie, null)
        val ratingContent = dialogView.findViewById<RatingBar>(R.id.ratingContent)
        val ratingEffect = dialogView.findViewById<RatingBar>(R.id.ratingEffect)
        val ratingCast = dialogView.findViewById<RatingBar>(R.id.ratingCast)
        val editComment = dialogView.findViewById<EditText>(R.id.editComment)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Đánh giá phim")
            .setView(dialogView)
            .setPositiveButton("Gửi", null) // gắn listener sau
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            val btnSend = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSend.setOnClickListener {
                val commentText = editComment.text.toString().trim()
                val uid = UserSession.uid

                if (uid == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (commentText.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Hiển thị loading dialog tùy chỉnh
                val loadingDialog = LoadingDialog(this)
                loadingDialog.show("Đang kiểm tra nội dung...")

                if (ProfanityFilter.containsProfanity(commentText)) {
                    Toast.makeText(
                        this,
                        "Bình luận chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadingDialog.dismiss()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    try {
                        // 1. Gọi API kiểm tra toxic
                        val response = RetrofitClient.toxicApi.checkComment(
                            ToxicCheckRequest(commentText)
                        )

                        if (response.is_toxic) {
                            loadingDialog.dismiss()
                            Toast.makeText(
                                this@FilmProfileActivity,
                                "Bình luận chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa.",
                                Toast.LENGTH_LONG
                            ).show()
                            loadingDialog.dismiss()
                            return@launch
                        }

                        val sentimentResponse = try {
                            RetrofitClient.sentimentApi.analyzeSentiment(
                                SentimentAnalysisRequest(commentText)
                            )
                        } catch (e: Exception) {
                            null // If sentiment analysis fails, proceed without it
                        }

                        // 2. Lưu comment vào Firebase
                        val reviewID = FirebaseDatabase.getInstance().getReference("Comments").push().key
                            ?: throw Exception("Không tạo được reviewID")

                        val comment = Comments(
                            filmID = filmID,
                            reviewID = reviewID,
                            userID = uid,
                            contentRating = ratingContent.rating.toFloat(),
                            effectRating = ratingEffect.rating.toFloat(),
                            castRating = ratingCast.rating.toFloat(),
                            comment = commentText,
                            like = 0,
                            reviewDate = getCurrentDate(),
                            sentimentLabel = sentimentResponse?.prediction?.let { pred ->
                                when(pred) {
                                    0 -> "toxic"
                                    1 -> "negative"
                                    2 -> "positive"
                                    else -> "neutral" // Hoặc giá trị mặc định
                                }
                            } ?: "unknown"
                        )

                        val db = FirebaseDatabase.getInstance()
                        db.getReference("Comments").child(reviewID).setValue(comment).await()
                        db.getReference("Films").child(filmID).child("Comment").child(reviewID).setValue(true).await()

                        val aiRatingAdjustment = when(sentimentResponse?.prediction) {
                            1 -> -0.25f // negative
                            2 -> 1.0f   // positive
                            else -> 0f    // neutral or unknown
                        }

                        updateFilmRating(
                            filmID,
                            ratingContent.rating.toFloat(),
                            ratingEffect.rating.toFloat(),
                            ratingCast.rating.toFloat(),
                            aiRatingAdjustment
                        )

                        loadingDialog.dismiss()
                        Toast.makeText(this@FilmProfileActivity, "Đánh giá đã được gửi!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                    } catch (e: Exception) {
                        loadingDialog.dismiss()
                        Toast.makeText(
                            this@FilmProfileActivity,
                            "Lỗi: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        dialog.show()
    }


    private fun updateFilmRating(
        filmID: String,
        contentRating: Float,
        effectRating: Float,
        castRating: Float,
        aiRatingAdjustment: Float = 0f
    ) {
        val ratingRef = FirebaseDatabase.getInstance().getReference("Rating").child(filmID)

        ratingRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val rating = currentData.getValue(Rating::class.java) ?: Rating(movieID = filmID)
                val reviewCount = rating.reviewCount + 1

                // Calculate new ratings
                val newContentRating = (rating.contentRating * rating.reviewCount + contentRating) / reviewCount
                val newEffectRating = (rating.effectRating * rating.reviewCount + effectRating) / reviewCount
                val newCastRating = (rating.castRating * rating.reviewCount + castRating) / reviewCount

                // Calculate average of the three ratings
                val averageRating = (newContentRating + newEffectRating + newCastRating) / 3

                // Apply AI adjustment
                val adjustedRating = averageRating + aiRatingAdjustment

                val updated = rating.copy(
                    contentRating = newContentRating,
                    effectRating = newEffectRating,
                    castRating = newCastRating,
                    rating = adjustedRating.coerceIn(0f, 5f), // Ensure rating stays between 0-5
                    reviewCount = reviewCount,
                    rateAI = (rating.rateAI ?: 0f) + aiRatingAdjustment
                )

                currentData.value = updated
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                error?.let {
                    Log.e("RatingUpdate", "Lỗi cập nhật rating: ${it.message}")
                }
            }
        })
    }



    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun checkIfUserHasRatedFilm(filmID: String) {
        val uid = UserSession.uid ?: run {
            binding.buttonRate.visibility = View.GONE
            return
        }

        val commentsRef = FirebaseDatabase.getInstance().getReference("Comments")

        // Tạo query để tìm comments của user hiện tại cho film này
        commentsRef.orderByChild("filmID").equalTo(filmID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var hasRated = false

                    for (commentSnapshot in snapshot.children) {
                        val comment = commentSnapshot.getValue(Comments::class.java)
                        if (comment?.userID == uid) {
                            hasRated = true
                            break
                        }
                    }

                    binding.buttonRate.visibility = if (hasRated) View.GONE else View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CheckRating", "Lỗi khi kiểm tra đánh giá: ${error.message}")
                    // Trong trường hợp lỗi, vẫn hiển thị nút đánh giá để người dùng có thể thử lại
                    binding.buttonRate.visibility = View.VISIBLE
                }
            })
    }

    private fun loadMoreComments() {
        isLoadingComments = true
        binding.btnLoadMore.isEnabled = false
        binding.btnLoadMore.text = "Đang tải..."

        val filmRef = FirebaseDatabase.getInstance()
            .getReference("Films").child(filmID).child("Comment")

        val query = if (lastCommentKey == null) {
            filmRef.orderByKey().limitToFirst(6)
        } else {
            filmRef.orderByKey().startAfter(lastCommentKey!!).limitToFirst(6)
        }

        query.get().addOnSuccessListener { snapshot ->
            val commentIDs = snapshot.children.mapNotNull { it.key }

            if (commentIDs.isNotEmpty()) {
                lastCommentKey = commentIDs.last()
                fetchCommentsWithUsers(commentIDs)
            } else {
                isLoadingComments = false
                binding.btnLoadMore.text = "Không còn bình luận"
                binding.btnLoadMore.isEnabled = false
            }
        }.addOnFailureListener {
            isLoadingComments = false
            binding.btnLoadMore.text = "Tải thêm bình luận"
            binding.btnLoadMore.isEnabled = true
        }
    }
    private fun fetchCommentsWithUsers(commentIDs: List<String>) {
        val commentRef = FirebaseDatabase.getInstance().getReference("Comments")
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        val tempList = mutableListOf<Pair<Comments, Users>>()

        var loadedCount = 0

        for (id in commentIDs) {
            commentRef.child(id).get().addOnSuccessListener { commentSnap ->
                val comment = commentSnap.getValue(Comments::class.java)
                if (comment != null) {
                    userRef.child(comment.userID).get().addOnSuccessListener { userSnap ->
                        val user = userSnap.getValue(Users::class.java)
                        if (user != null) {
                            tempList.add(comment to user)
                        }
                        loadedCount++
                        if (loadedCount == commentIDs.size) {
                            commentsList.addAll(tempList)
                            commentAdapter.notifyItemRangeInserted(
                                commentsList.size - tempList.size,
                                tempList.size
                            )
                            isLoadingComments = false
                            binding.btnLoadMore.text = "Tải thêm bình luận"
                            binding.btnLoadMore.isEnabled = true
                        }
                    }
                } else {
                    loadedCount++
                }
            }
        }
    }
    private fun sortCommentsByDate() {
        isSortDescending = !isSortDescending // Đảo chiều sắp xếp

        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())

        commentsList.sortWith(compareBy { (comment, _) ->
            dateFormat.parse(comment.reviewDate)
        })

        if (isSortDescending) {
            commentsList.reverse()
        }

        commentAdapter.notifyDataSetChanged()

        // Cập nhật nút để người dùng biết đang sắp theo kiểu gì
        binding.txtSortDate.text = if (isSortDescending) "Mới nhất" else "Cũ nhất"
    }




}

