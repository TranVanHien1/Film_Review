package com.example.danhgiaphim.User

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.danhgiaphim.R
import com.example.danhgiaphim.adapter.UserCommentsAdapter
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ActivityLikeCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LikeCommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLikeCommentBinding
    private lateinit var database: FirebaseDatabase
    private val userCommentsList = mutableListOf<Triple<Comments, Users, String>>()
    private val currentUserID = UserSession.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        binding.recyclerViewLikedComments.layoutManager = LinearLayoutManager(this)

        binding.btnBackToUserFromLike.setOnClickListener(){
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }

        loadUserComments()
    }

    private fun loadUserComments() {
        // Lấy user hiện tại từ FirebaseAuth
        if (currentUserID == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        // Truy vấn bình luận có userID = currentUserID
        val commentsRef = database.getReference("Comments")
        val query = commentsRef.orderByChild("userID").equalTo(currentUserID)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userCommentsList.clear()
                if (!snapshot.exists()) {
                    Toast.makeText(this@LikeCommentActivity, "Bạn chưa đăng bình luận nào.", Toast.LENGTH_SHORT).show()
                    return
                }
                val totalComments = snapshot.childrenCount
                var loadedCount = 0

                for (child in snapshot.children) {
                    val comment = child.getValue(Comments::class.java)
                    if (comment != null) {
                        // Lấy tên phim từ Movies/[comment.movieID]/title
                        val movieRef = database.getReference("Films").child(comment.filmID).child("title")
                        movieRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(movieSnap: DataSnapshot) {
                                val movieTitle = movieSnap.getValue(String::class.java) ?: "Không rõ"
                                // Lấy thông tin user (hiện tại, vì đây là comment của người dùng hiện tại, ta chỉ cần load một lần)
                                val userRef = database.getReference("Users").child(currentUserID)
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnap: DataSnapshot) {
                                        val user = userSnap.getValue(Users::class.java)
                                        if (user != null) {
                                            userCommentsList.add(Triple(comment, user, movieTitle))
                                        }
                                        loadedCount++
                                        if (loadedCount == totalComments.toInt()) {
                                            binding.recyclerViewLikedComments.adapter =
                                                UserCommentsAdapter(this@LikeCommentActivity, userCommentsList)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    } else {
                        loadedCount++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LikeCommentActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}