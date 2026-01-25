package com.example.danhgiaphim.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.Film.FilmProfileActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ItemLikedCommentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class UserCommentsAdapter(private val context: Context,
    private val userComments: List<Triple<Comments, Users, String>>
) : RecyclerView.Adapter<UserCommentsAdapter.UserCommentViewHolder>() {

    inner class UserCommentViewHolder(val binding: ItemLikedCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCommentViewHolder {
        val binding = ItemLikedCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserCommentViewHolder(binding)
    }

    override fun getItemCount(): Int = userComments.size

    override fun onBindViewHolder(holder: UserCommentViewHolder, position: Int) {
        val (comment, user, movieTitle) = userComments[position]

        // Hiển thị tên phim
        holder.binding.textMovieTitle.text = movieTitle
        // Hiển thị thông tin người dùng
        holder.binding.textUserName.text = user.username
        // Nội dung bình luận
        holder.binding.textComment.text = comment.comment
        // Số lượt thích
        holder.binding.txtLikeCount.text = comment.like.toString()
        // Định dạng ngày đăng: ta giả sử comment.reviewDate có định dạng "dd/MM/yyyy HH:mm"
        holder.binding.textReviewDate.text = formatDate(comment.reviewDate)

        // Load avatar người dùng
        Glide.with(holder.itemView.context)
            .load(user.avatarURL)
            .placeholder(R.drawable.ic_user)
            .into(holder.binding.imageAvatar)

        holder.itemView.setOnClickListener(){
            val intent = Intent(context, FilmProfileActivity::class.java)
            FilmSession.filmid = comment.filmID
            context.startActivity(intent)
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            "Không xác định"
        }
    }
}