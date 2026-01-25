package com.example.danhgiaphim.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.Admin.FilmDetailActivity
import com.example.danhgiaphim.Film.FilmProfileActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.databinding.FilmGridItemBinding

class FilmGridAdapter(private val context: Context, private var filmList: List<Films>, private val itemWidth: Int ) :
    RecyclerView.Adapter<FilmGridAdapter.FilmViewHolder>() {

    private var fullFilmList: List<Films> = filmList.toList()

    inner class FilmViewHolder(val binding: FilmGridItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val binding = FilmGridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilmViewHolder(binding)
    }

    override fun getItemCount(): Int = filmList.size

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val film = filmList[position]

        // Auto scale item size
        val params = holder.itemView.layoutParams
        params.width = itemWidth
        params.height = (itemWidth * 1.5).toInt()
        holder.itemView.layoutParams = params

        holder.binding.txtFilmTitle.text = film.title
        Glide.with(context)
            .load(film.posterURL)
            .into(holder.binding.imgFilmPoster)

        holder.itemView.setOnClickListener(){
            val intent = Intent(context, FilmProfileActivity::class.java)
            FilmSession.filmid = film.movieID
            context.startActivity(intent)
        }
    }



    fun filter(query: String) {
        val filtered = if (query.isBlank()) {
            fullFilmList
        } else {
            fullFilmList.filter { it.title?.contains(query, ignoreCase = true) == true }
        }
        updateList(filtered)
    }

    fun setFullList(films: List<Films>) {
        fullFilmList = films
        updateList(films)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Films>) {
        filmList = newList.toMutableList()
        notifyDataSetChanged()
    }

}
