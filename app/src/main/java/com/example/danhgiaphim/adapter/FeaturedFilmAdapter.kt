package com.example.danhgiaphim.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.danhgiaphim.Film.FilmProfileActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.databinding.ItemFeaturedFilmBinding

class FeaturedFilmAdapter(
    private val context: Context,
    private var filmList: List<Films> = emptyList()
) : RecyclerView.Adapter<FeaturedFilmAdapter.FeaturedFilmViewHolder>() {

    private var ratingMap: Map<String, Rating?> = emptyMap()

    inner class FeaturedFilmViewHolder(val binding: ItemFeaturedFilmBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedFilmViewHolder {
        val binding = ItemFeaturedFilmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeaturedFilmViewHolder(binding)
    }

    override fun getItemCount(): Int = filmList.size

    override fun onBindViewHolder(holder: FeaturedFilmViewHolder, position: Int) {
        val film = filmList[position]
        val rating = ratingMap[film.movieID]?.rating ?: 0F

        holder.binding.txtFeaturedTitle.text = film.title
        holder.binding.txtFeaturedMeta.text = listOf(film.releaseYear, film.director)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
            .ifBlank { "Đang cập nhật" }
        holder.binding.txtFeaturedRating.text = if (rating > 0F) "★ %.1f".format(rating) else "★ --"

        Glide.with(context)
            .load(film.posterURL)
            .into(holder.binding.imgFeaturedPoster)

        holder.itemView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.97F).scaleY(0.97F).setDuration(90).start()
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    view.animate().scaleX(1F).scaleY(1F).setDuration(120).start()
                }
            }
            false
        }

        holder.itemView.setOnClickListener {
            FilmSession.filmid = film.movieID
            context.startActivity(Intent(context, FilmProfileActivity::class.java))
        }
    }

    fun setRatings(ratings: Map<String, Rating?>) {
        ratingMap = ratings
        notifyDataSetChanged()
    }

    fun updateList(newList: List<Films>) {
        filmList = newList
        notifyDataSetChanged()
    }
}
