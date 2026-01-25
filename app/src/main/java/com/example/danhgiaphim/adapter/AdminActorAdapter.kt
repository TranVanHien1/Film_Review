package com.example.danhgiaphim.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.Admin.ActorListActivity
import com.example.danhgiaphim.Admin.FilmDetailActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.databinding.ActorLayoutBinding
import com.example.danhgiaphim.databinding.FilmLayoutBinding

class AdminActorAdapter(
    val context: Context,
    var actorlist: ArrayList<Actors>
) : RecyclerView.Adapter<AdminActorAdapter.ActorViewHolder>() {

    inner class ActorViewHolder(val binding: ActorLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorViewHolder {
        val binding = ActorLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActorViewHolder, position: Int) {
        val actor = actorlist[position]
        holder.binding.txtActorName.text = actor.actorName

        Glide.with(context)
            .load(actor.actorAvatarURL)
            .circleCrop()
            .placeholder(R.drawable.ic_user)
            .into(holder.binding.imgAvatarActorList)

        holder.itemView.setOnClickListener {
            if (context is ActorListActivity) {
                context.showEditActorDialog(actor)
            }
        }
    }

    override fun getItemCount(): Int = actorlist.size
}
