package com.example.danhgiaphim.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ItemCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(private val comments: List<Pair<Comments, Users>>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(inflater, parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val (comment, user) = comments[position]
        val commentID = comment.reviewID

        holder.binding.textUserName.text = user.username
        holder.binding.textComment.text = comment.comment

        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val parsedDate = try {
            inputFormat.parse(comment.reviewDate)
        } catch (e: Exception) {
            null
        }

        val dateText = parsedDate?.let { outputFormat.format(it) } ?: "Không xác định"
        holder.binding.textReviewDate.text = dateText
        holder.binding.txtLikeCount.text = comment.like.toString()

        // Kiểm tra xem người dùng đã thích comment này chưa
        val likeStatusRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(currentUserID ?: "")
            .child("likedComments")
            .child(commentID)

        likeStatusRef.get().addOnSuccessListener { snapshot ->
            val isLiked = snapshot.exists()
            holder.binding.imgLike.setImageResource(
                if (isLiked) R.drawable.ic_like else R.drawable.ic_unlike
            )
            holder.binding.imgLike.tag = isLiked
        }

        holder.binding.imgLike.setOnClickListener {
            val isLiked = holder.binding.imgLike.tag as? Boolean ?: false
            val database = FirebaseDatabase.getInstance()
            val commentRef = database.getReference("Comments").child(commentID)
            val likeRef = database.getReference("Likes").child(commentID).child(currentUserID ?: "")
            val userLikeRef = database.getReference("Users").child(currentUserID ?: "").child("likedComments").child(commentID)

            if (!isLiked) {
                // Thích
                val newLikeCount = comment.like + 1
                commentRef.child("like").setValue(newLikeCount)
                likeRef.setValue(true)
                userLikeRef.setValue(true).addOnSuccessListener {
                    holder.binding.imgLike.setImageResource(R.drawable.ic_like)
                    holder.binding.txtLikeCount.text = newLikeCount.toString()
                    holder.binding.imgLike.tag = true
                    comment.like = newLikeCount
                }
            } else {
                // Bỏ thích
                val newLikeCount = if (comment.like > 0) comment.like - 1 else 0
                commentRef.child("like").setValue(newLikeCount)
                likeRef.removeValue()
                userLikeRef.removeValue().addOnSuccessListener {
                    holder.binding.imgLike.setImageResource(R.drawable.ic_unlike)
                    holder.binding.txtLikeCount.text = newLikeCount.toString()
                    holder.binding.imgLike.tag = false
                    comment.like = newLikeCount
                }
            }
        }

        Glide.with(holder.itemView)
            .load(user.avatarURL)
            .placeholder(R.drawable.ic_user)
            .into(holder.binding.imageAvatar)
    }
}
