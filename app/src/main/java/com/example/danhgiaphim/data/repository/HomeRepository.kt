package com.example.danhgiaphim.data.repository

import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class HomeRepository @Inject constructor() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    suspend fun loadUser(uid: String): Users? {
        return database.getReference("Users").child(uid).get().await().getValue(Users::class.java)
    }

    suspend fun loadGenres(): List<String> {
        val snapshot = database.getReference("Genre").get().await()
        return snapshot.children.mapNotNull { it.child("genreName").getValue(String::class.java) }
    }

    suspend fun loadFilmCatalog(): FilmCatalog = coroutineScope {
        val filmsDeferred = async { loadFilms() }
        val ratingsDeferred = async { loadRatings() }
        FilmCatalog(filmsDeferred.await(), ratingsDeferred.await())
    }

    private suspend fun loadFilms(): List<Films> {
        val snapshot = database.getReference("Films").get().await()
        return snapshot.children.mapNotNull { it.getValue(Films::class.java) }
            .filter { it.movieID.isNotBlank() }
    }

    private suspend fun loadRatings(): Map<String, Rating> {
        val snapshot = database.getReference("Rating").get().await()
        return snapshot.children.mapNotNull(::parseRating).associateBy { it.movieID }
    }

    private fun parseRating(snapshot: DataSnapshot): Rating? {
        val movieID = snapshot.child("movieID").getValue(String::class.java).orEmpty()
        if (movieID.isBlank()) return null

        return Rating(
            movieID = movieID,
            ratePoint = snapshot.child("ratePoint").getValue(Float::class.java) ?: 0F,
            castRating = snapshot.child("castRating").getValue(Float::class.java) ?: 0F,
            contentRating = snapshot.child("contentRating").getValue(Float::class.java) ?: 0F,
            effectRating = snapshot.child("effectRating").getValue(Float::class.java) ?: 0F,
            rating = snapshot.child("rating").getValue(Float::class.java) ?: 0F,
            reviewCount = snapshot.child("reviewCount").getValue(Long::class.java) ?: 0L,
            rateAI = snapshot.child("rateAI").getValue(Float::class.java) ?: 0F
        )
    }
}

data class FilmCatalog(
    val films: List<Films>,
    val ratings: Map<String, Rating>
)
