package com.example.danhgiaphim.Film

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.danhgiaphim.Admin.FilmListActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.databinding.ActivityAddFilmBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.HashMap
import java.util.Locale
import java.util.UUID

class AddFilmActivity : AppCompatActivity() {

    lateinit var addFilmBinding: ActivityAddFilmBinding
    private var posterUri: Uri? = null
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myReference : DatabaseReference = database.reference.child("Films")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFilmBinding = ActivityAddFilmBinding.inflate(layoutInflater)
        val view = addFilmBinding.root
        setContentView(view)



        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                posterUri = it
                addFilmBinding.imgPoster.setImageURI(it)
            }
        }

        addFilmBinding.imgPoster.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        addFilmBinding.btnBackToFilmListFromAddFilm.setOnClickListener(){
            val intent = Intent(this, FilmListActivity::class.java)
            startActivity(intent)
        }

        addFilmBinding.btnAddFilm.setOnClickListener {
            if (posterUri != null) {
                uploadPosterToCloudinary(posterUri!!)
            } else {
                Toast.makeText(this, "Vui lòng chọn ảnh poster", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun addFilm() {
        val title = addFilmBinding.edtTitle.text.toString()
        val year = addFilmBinding.edtReleaseYear.text.toString()
        val director = addFilmBinding.edtDirector.text.toString()
        val synopsis = addFilmBinding.edtSynopsis.text.toString()
        val trailer = addFilmBinding.edtTrailerURL.text.toString()
        val currentTimeMillis = System.currentTimeMillis()
        if (title.isEmpty() || year.isEmpty() || director.isEmpty() || synopsis.isEmpty() || trailer.isEmpty()) {
            Toast.makeText(applicationContext, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG)
                .show()
        }
    }


    private fun uploadPosterToCloudinary(uri: Uri) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    saveFilmToFirebase(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@AddFilmActivity, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun saveFilmToFirebase(posterUrl: String = "") {
        val movieId = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()

        val film = Films(
            movieID = movieId,
            title = addFilmBinding.edtTitle.text.toString(),
            releaseYear = addFilmBinding.edtReleaseYear.text.toString(),
            director = addFilmBinding.edtDirector.text.toString(),
            genre = null,
            posterURL = posterUrl,
            synopsis = addFilmBinding.edtSynopsis.text.toString(),
            trailerURL = addFilmBinding.edtTrailerURL.text.toString(),
            createdAt = getCurrentDate(),
            actor = null
        )

        FirebaseDatabase.getInstance()
            .getReference("Films")
            .child(movieId)
            .setValue(film)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm phim thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Thêm phim thất bại", Toast.LENGTH_SHORT).show()
            }
        val rating = Rating(movieID = movieId,
            contentRating = 0F,
            effectRating = 0F,
            castRating = 0F,
            rating = 0F,
            reviewCount = 0,
            ratePoint = 0F,
            rateAI = 0F)

        FirebaseDatabase.getInstance()
            .getReference("Rating")
            .child(movieId)
            .setValue(rating)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm phim thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Thêm phim thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(calendar.time)
    }
}