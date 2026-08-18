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
import java.util.Locale

class CommentAdapter(
    private val comments: List<Pair<Comments, Users>>,
    var likedCommentIds: Set<String>,
    private val onLikeClick: (Comments) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(inflater, parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val (comment, user) = comments[position]
        val isLiked = likedCommentIds.contains(comment.reviewID)

        holder.binding.textUserName.text = user.username
        holder.binding.textComment.text = comment.comment
        holder.binding.textReviewDate.text = formatDate(comment.reviewDate)
        holder.binding.txtLikeCount.text = comment.like.toString()
        holder.binding.imgLike.setImageResource(if (isLiked) R.drawable.ic_like else R.drawable.ic_unlike)
        holder.binding.imgLike.setOnClickListener { onLikeClick(comment) }

        Glide.with(holder.itemView)
            .load(user.avatarURL)
            .placeholder(R.drawable.ic_user)
            .into(holder.binding.imageAvatar)
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return runCatching {
            inputFormat.parse(dateString)?.let { outputFormat.format(it) }
        }.getOrNull() ?: "Không xác định"
    }
}
