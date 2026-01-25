package com.example.danhgiaphim.Film

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.danhgiaphim.R
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.danhgiaphim.Admin.FilmDetailActivity
import com.example.danhgiaphim.adapter.AllCommentsAdapter
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ActivityFilmRatingBinding
import com.example.danhgiaphim.databinding.ActivityHomeBinding
import com.example.danhgiaphim.databinding.DialogRateMovieBinding
import com.example.danhgiaphim.databinding.DialogRatingInfoBinding
import com.google.firebase.database.*

class FilmRatingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilmRatingBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: AllCommentsAdapter

    private val allComments = mutableListOf<Pair<Comments, Users>>()
    private var filteredComments = mutableListOf<Pair<Comments, Users>>()
    private val commentList = mutableListOf<Pair<Comments, Users>>()

    private var currentPage = 0
    private val pageSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilmRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filmID = FilmSession.filmid ?: return
        database = FirebaseDatabase.getInstance()

        adapter = AllCommentsAdapter(commentList, ::onCommentClicked)
        binding.recyclerViewComments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewComments.adapter = adapter

        loadAllComments(filmID)

        binding.btnNext.setOnClickListener {
            if ((currentPage + 1) * pageSize < filteredComments.size) {
                loadPage(currentPage + 1)
            }
        }

        binding.btnBack.setOnClickListener {
            if (currentPage > 0) {
                loadPage(currentPage - 1)
            }
        }

        binding.btnBackToFilmListFromRating.setOnClickListener {
            startActivity(Intent(this, FilmDetailActivity::class.java))
        }

        binding.txtSearchCommentInList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterComments(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnRating.setOnClickListener {
            showRatingDialog(FilmSession.filmid ?: return@setOnClickListener)
        }
    }

    private fun showRatingDialog(movieID: String) {
        val rateBinding = DialogRatingInfoBinding.inflate(layoutInflater)

        val dialogView = rateBinding.root

        val ratingRef = database.getReference("Rating").child(movieID)

        ratingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val castRating = snapshot.child("castRating").getValue(Float::class.java) ?: 0
                val contentRating = snapshot.child("contentRating").getValue(Float::class.java) ?: 0
                val effectRating = snapshot.child("effectRating").getValue(Float::class.java) ?: 0
                val rating = snapshot.child("rating").getValue(Float::class.java) ?: 0
                val reviewCount = snapshot.child("reviewCount").getValue(Long::class.java) ?: 0
                val ratePoint = snapshot.child("ratePoint").getValue(Float::class.java) ?: 0.0

                rateBinding.txtCastRating.text = "Cast Rating: $castRating"
                rateBinding.txtContentRating.text = "Content Rating: $contentRating"
                rateBinding.txtEffectRating.text = "Effect Rating: $effectRating"
                rateBinding.txtRating.text = "Average Rating: $rating"
                rateBinding.txtReviewCount.text = "Số lượt đánh giá: $reviewCount"
                rateBinding.edtRatePoint.setText(ratePoint.toString())

                val dialog = AlertDialog.Builder(this@FilmRatingActivity)
                    .setTitle("Thông tin đánh giá")
                    .setView(dialogView)
                    .setPositiveButton("Lưu") { _, _ ->
                        val newRatePoint = rateBinding.edtRatePoint.text.toString().toDoubleOrNull()
                        if (newRatePoint != null) {
                            ratingRef.child("ratePoint").setValue(newRatePoint)
                                .addOnSuccessListener {
                                    Toast.makeText(this@FilmRatingActivity, "Lưu thành công", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@FilmRatingActivity, "Lỗi khi lưu", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this@FilmRatingActivity, "Giá trị không hợp lệ", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Hủy", null)
                    .create()

                dialog.show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FilmRatingActivity, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadAllComments(movieID: String) {
        val commentsRef = database.getReference("Films").child(movieID).child("Comment")

        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allComments.clear()
                commentList.clear()
                var totalComments = snapshot.childrenCount
                var loadedCount = 0

                for (child in snapshot.children) {
                    val commentID = child.key ?: continue
                    database.getReference("Comments").child(commentID)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(cSnap: DataSnapshot) {
                                val comment = cSnap.getValue(Comments::class.java)
                                if (comment != null) {
                                    database.getReference("Users").child(comment.userID)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(uSnap: DataSnapshot) {
                                                val user = uSnap.getValue(Users::class.java)
                                                if (user != null) {
                                                    allComments.add(Pair(comment, user))
                                                }
                                                loadedCount++
                                                if (loadedCount == totalComments.toInt()) {
                                                    allComments.sortByDescending { it.first.reviewDate }
                                                    filteredComments = allComments.toMutableList()
                                                    loadPage(0)
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                } else {
                                    loadedCount++
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPage(page: Int) {
        val start = page * pageSize
        val end = minOf(start + pageSize, filteredComments.size)

        commentList.clear()
        if (start < end) {
            commentList.addAll(filteredComments.subList(start, end))
        }
        adapter.notifyDataSetChanged()
        currentPage = page
        updatePageDisplay()
    }

    private fun updatePageDisplay() {
        val totalPages = if (filteredComments.isEmpty()) 1 else (filteredComments.size - 1) / pageSize + 1
        binding.txtPageNumber.text = "Trang ${currentPage + 1}/$totalPages"
    }

    private fun onCommentClicked(commentID: String, movieID: String) {
        AlertDialog.Builder(this)
            .setTitle("Xóa bình luận?")
            .setMessage("Bạn có chắc muốn xóa bình luận này không?")
            .setPositiveButton("Xóa") { _, _ -> deleteComment(commentID, movieID) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteComment(commentID: String, movieID: String) {
        val commentRef = database.getReference("Comments").child(commentID)
        val movieCommentRef = database.getReference("Films").child(movieID).child("Comment").child(commentID)

        commentRef.removeValue()
        movieCommentRef.removeValue()
        Toast.makeText(this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show()

        allComments.removeAll { it.first.reviewID == commentID }
        filteredComments.removeAll { it.first.reviewID == commentID }
        commentList.removeAll { it.first.reviewID == commentID }

        loadPage(currentPage.coerceAtMost((filteredComments.size - 1) / pageSize))
    }

    private fun filterComments(query: String) {
        filteredComments = if (query.isBlank()) {
            allComments.toMutableList()
        } else {
            allComments.filter { (_, user) ->
                user.username.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        loadPage(0)
    }
}
