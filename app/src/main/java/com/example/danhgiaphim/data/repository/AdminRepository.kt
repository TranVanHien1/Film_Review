package com.example.danhgiaphim.data.repository

import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Genre
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.Users
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AdminRepository @Inject constructor() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    suspend fun loadUsers(): List<Users> {
        return database.getReference("Users").get().await().children
            .mapNotNull { it.getValue(Users::class.java) }
            .filter { it.role == "User" }
    }

    suspend fun loadUser(userId: String): Users? {
        return database.getReference("Users").child(userId).get().await().getValue(Users::class.java)
    }

    suspend fun loadFilms(): List<Films> {
        return database.getReference("Films").get().await().children
            .mapNotNull { it.getValue(Films::class.java) }
    }

    suspend fun loadFilm(filmId: String): Films? {
        return database.getReference("Films").child(filmId).get().await().getValue(Films::class.java)
    }

    suspend fun addFilm(filmInput: FilmInput, posterUrl: String) {
        val movieId = database.reference.push().key ?: UUID.randomUUID().toString()
        val film = Films(
            movieID = movieId,
            title = filmInput.title,
            releaseYear = filmInput.releaseYear,
            director = filmInput.director,
            genre = null,
            synopsis = filmInput.synopsis,
            posterURL = posterUrl,
            trailerURL = filmInput.trailerUrl,
            createdAt = currentDate(),
            actor = null
        )
        val rating = Rating(movieID = movieId)
        database.getReference("Films").child(movieId).setValue(film).await()
        database.getReference("Rating").child(movieId).setValue(rating).await()
    }

    suspend fun updateFilm(
        filmId: String,
        filmInput: FilmInput,
        posterUrl: String,
        genres: Map<String, Genre>,
        actors: Map<String, Actors>
    ) {
        val updatedFilm = mapOf(
            "title" to filmInput.title,
            "releaseYear" to filmInput.releaseYear,
            "director" to filmInput.director,
            "synopsis" to filmInput.synopsis,
            "trailerURL" to filmInput.trailerUrl,
            "posterURL" to posterUrl,
            "genre" to genres,
            "actor" to actors
        )
        database.getReference("Films").child(filmId).updateChildren(updatedFilm).await()
    }

    suspend fun deleteFilm(filmId: String) {
        database.getReference("Films").child(filmId).removeValue().await()
    }

    suspend fun loadGenres(): List<Genre> {
        return database.getReference("Genre").get().await().children
            .mapNotNull { it.getValue(Genre::class.java) }
    }

    suspend fun addGenre(name: String) {
        val id = database.reference.push().key ?: UUID.randomUUID().toString()
        database.getReference("Genre").child(id).setValue(Genre(id, name)).await()
    }

    suspend fun updateGenre(genreId: String, name: String) {
        database.getReference("Genre").child(genreId).child("genreName").setValue(name).await()
    }

    suspend fun deleteGenre(genreId: String) {
        database.getReference("Genre").child(genreId).removeValue().await()
    }

    suspend fun loadActors(): List<Actors> {
        return database.getReference("Actors").get().await().children
            .mapNotNull { it.getValue(Actors::class.java) }
    }

    suspend fun addActor(name: String, avatarUrl: String) {
        val actorId = database.reference.push().key ?: UUID.randomUUID().toString()
        database.getReference("Actors").child(actorId)
            .setValue(Actors(actorId, name, avatarUrl))
            .await()
    }

    suspend fun updateActor(actorId: String, name: String, avatarUrl: String) {
        database.getReference("Actors").child(actorId).updateChildren(
            mapOf("actorName" to name, "actorAvatarURL" to avatarUrl)
        ).await()
    }

    suspend fun deleteActor(actorId: String) {
        database.getReference("Actors").child(actorId).removeValue().await()
    }

    suspend fun loadRating(movieId: String): Rating {
        return database.getReference("Rating").child(movieId).get().await().getValue(Rating::class.java)
            ?: Rating(movieID = movieId)
    }

    suspend fun updateRatePoint(movieId: String, ratePoint: Float) {
        database.getReference("Rating").child(movieId).child("ratePoint").setValue(ratePoint).await()
    }

    suspend fun loadCommentsForFilm(movieId: String): List<Pair<Comments, Users>> = coroutineScope {
        val commentIds = database.getReference("Films").child(movieId).child("Comment")
            .get()
            .await()
            .children
            .mapNotNull { it.key }

        commentIds.map { commentId ->
            async {
                val comment = database.getReference("Comments").child(commentId).get().await()
                    .getValue(Comments::class.java) ?: return@async null
                val user = database.getReference("Users").child(comment.userID).get().await()
                    .getValue(Users::class.java) ?: return@async null
                comment to user
            }
        }.awaitAll().filterNotNull().sortedByDescending { it.first.reviewTimestamp }
    }

    suspend fun deleteComment(commentId: String, movieId: String) {
        database.getReference("Comments").child(commentId).removeValue().await()
        database.getReference("Films").child(movieId).child("Comment").child(commentId).removeValue().await()
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Calendar.getInstance().time)
    }
}

data class FilmInput(
    val title: String,
    val releaseYear: String,
    val director: String,
    val synopsis: String,
    val trailerUrl: String
) {
    fun validate(): String? {
        return if (
            title.isBlank() ||
            releaseYear.isBlank() ||
            director.isBlank() ||
            synopsis.isBlank() ||
            trailerUrl.isBlank()
        ) {
            "Vui lòng nhập đầy đủ thông tin"
        } else {
            null
        }
    }
}
