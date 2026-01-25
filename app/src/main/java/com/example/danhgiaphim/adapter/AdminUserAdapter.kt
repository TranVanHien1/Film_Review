package com.example.danhgiaphim.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.R
import com.example.danhgiaphim.Admin.UserDetailActivity
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.TableLayoutBinding


class AdminUserAdapter(val context: Context,
                        var userlist: ArrayList<Users>) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val adapterUserBinding : TableLayoutBinding) : RecyclerView.ViewHolder(adapterUserBinding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        var binding = TableLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userlist.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userlist[position]
        holder.adapterUserBinding.txtUsername.text = user.username
        holder.adapterUserBinding.txtEmailList.text = user.email

        Glide.with(holder.itemView.context)
            .load(user.avatarURL)
            .circleCrop()
            .placeholder(R.drawable.ic_user) // bạn có thể thêm ảnh tạm thời
            .into(holder.adapterUserBinding.imgAvatarUserList)
        holder.itemView.setOnClickListener(){
            val intent = Intent(context, UserDetailActivity::class.java)
            intent.putExtra("userID", user.userID)
            context.startActivity(intent)
        }
    }

}