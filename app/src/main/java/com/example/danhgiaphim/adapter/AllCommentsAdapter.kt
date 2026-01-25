package com.example.danhgiaphim.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ItemCommentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllCommentsAdapter(
    private var comments: List<Pair<Comments, Users>>,
    private val onCommentClick: (commentID: String, movieID: String) -> Unit
) : RecyclerView.Adapter<AllCommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comments, user: Users) {
            binding.textUserName.text = user.username
            binding.textComment.text = comment.comment
            binding.textReviewDate.text = formatDate(comment.reviewDate)
            binding.txtLikeCount.text = comment.like.toString()

            Glide.with(binding.root.context)
                .load(user.avatarURL)
                .placeholder(R.drawable.ic_user)
                .into(binding.imageAvatar)

            when (comment.sentimentLabel?.lowercase()) {
                "negative" -> binding.root.setBackgroundResource(R.drawable.comment_negative_bg)
                "positive" -> binding.root.setBackgroundResource(R.drawable.comment_positive_bg)
                else -> binding.root.setBackgroundResource(R.drawable.comment_positive_bg)
            }

            binding.root.setOnClickListener {
                onCommentClick(comment.reviewID, comment.filmID)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val input = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = input.parse(dateString)
                input.format(date ?: Date())
            } catch (e: Exception) {
                "Không rõ"
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val (comment, user) = comments[position]
        holder.bind(comment, user)
    }
    fun updateList(newList: List<Pair<Comments, Users>>) {
        comments = newList
        notifyDataSetChanged()
    }

}