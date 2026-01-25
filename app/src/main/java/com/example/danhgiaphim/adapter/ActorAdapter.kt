package com.example.danhgiaphim.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Actors

class ActorAdapter(private val actorList: List<Actors>) :
    RecyclerView.Adapter<ActorAdapter.ActorViewHolder>() {

    inner class ActorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.imageActorAvatar)
        val name: TextView = itemView.findViewById(R.id.textActorName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor, parent, false)
        return ActorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActorViewHolder, position: Int) {
        val actor = actorList[position]
        holder.name.text = actor.actorName
        Glide.with(holder.itemView.context)
            .load(actor.actorAvatarURL)
            .placeholder(R.drawable.ic_user)
            .into(holder.avatar)
    }

    override fun getItemCount(): Int = actorList.size
}
