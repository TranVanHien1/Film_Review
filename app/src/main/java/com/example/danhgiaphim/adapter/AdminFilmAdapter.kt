package com.example.danhgiaphim.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.Admin.FilmDetailActivity
import com.example.danhgiaphim.Admin.UserDetailActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.FilmLayoutBinding

class AdminFilmAdapter(val context: Context,
                       var filmlist: ArrayList<Films>) : RecyclerView.Adapter<AdminFilmAdapter.FilmViewHolder>() {

    inner class FilmViewHolder(val adapterFilmBinding : FilmLayoutBinding) : RecyclerView.ViewHolder(adapterFilmBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        var binding = FilmLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return FilmViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filmlist.size
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = filmlist[position]
        holder.adapterFilmBinding.txtFilmName.text = film.title
        holder.adapterFilmBinding.txtFilmDirector.text = film.director

        Glide.with(holder.itemView.context)
            .load(film.posterURL)
            .circleCrop()
            .placeholder(R.drawable.ic_user) // bạn có thể thêm ảnh tạm thời
            .into(holder.adapterFilmBinding.imgAvatarUserList)
        holder.itemView.setOnClickListener(){
            val intent = Intent(context, FilmDetailActivity::class.java)
            FilmSession.filmid = film.movieID
            context.startActivity(intent)
        }


    }
}