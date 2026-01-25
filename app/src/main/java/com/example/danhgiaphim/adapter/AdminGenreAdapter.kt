package com.example.danhgiaphim.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Genre

class AdminGenreAdapter(private val genres: MutableList<Genre>, private val onItemClick: (Genre) -> Unit) :
    RecyclerView.Adapter<AdminGenreAdapter.GenreViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setGenres(newGenres: List<Genre>) {
        genres.clear()
        genres.addAll(newGenres)
        notifyDataSetChanged()
    }

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtGenreName: TextView = itemView.findViewById(R.id.txtGenreName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.genre_layout, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.txtGenreName.text = genre.genreName
        holder.itemView.setOnClickListener {
            onItemClick(genre) // Gọi callback khi bấm
        }
    }

    override fun getItemCount(): Int = genres.size

    fun getGenres(): List<Genre> = genres

}