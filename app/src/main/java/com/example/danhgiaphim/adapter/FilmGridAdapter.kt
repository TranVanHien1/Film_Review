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
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.databinding.FilmGridItemBinding

class FilmGridAdapter(private val context: Context, private var filmList: List<Films>, private val itemWidth: Int ) :
    RecyclerView.Adapter<FilmGridAdapter.FilmViewHolder>() {

    private var fullFilmList: List<Films> = filmList.toList()
    private var ratingMap: Map<String, Rating?> = emptyMap()

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
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = params

        val posterParams = holder.binding.cardFilmPoster.layoutParams
        posterParams.height = (itemWidth * 1.5).toInt()
        holder.binding.cardFilmPoster.layoutParams = posterParams

        holder.binding.txtFilmTitle.text = film.title
        holder.binding.txtFilmYear.text = film.releaseYear.ifBlank { "N/A" }
        holder.binding.txtFilmDirector.text = film.director.ifBlank { "Đang cập nhật" }
        val rating = ratingMap[film.movieID]?.rating ?: 0F
        holder.binding.txtFilmRating.text = if (rating > 0F) "★ %.1f".format(rating) else "★ --"
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
            fullFilmList.filter { it.title.contains(query, ignoreCase = true) }
        }
        updateList(filtered)
    }

    fun setFullList(films: List<Films>) {
        fullFilmList = films
        updateList(films)
    }

    fun setRatings(ratings: Map<String, Rating?>) {
        ratingMap = ratings
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Films>) {
        filmList = newList.toMutableList()
        notifyDataSetChanged()
    }

}
