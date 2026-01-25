package com.example.danhgiaphim.Admin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.danhgiaphim.Film.EditFilmActivity
import com.example.danhgiaphim.Film.FilmRatingActivity
import com.example.danhgiaphim.LoginActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.databinding.ActivityFilmDetailBinding

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar


class FilmDetailActivity : AppCompatActivity() {

    lateinit var filmDetailBinding: ActivityFilmDetailBinding
    var database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val userRef : DatabaseReference = database.reference.child("Films")
    private val filmID = FilmSession.filmid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filmDetailBinding = ActivityFilmDetailBinding.inflate(layoutInflater)
        val view = filmDetailBinding.root
        setContentView(view)

        if (filmID.isNullOrEmpty()) {
            Toast.makeText(this, "Không có filmID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadData(filmID)

        filmDetailBinding.btnBackToFilmListFromDetail.setOnClickListener(){
            val intent = Intent(this, FilmListActivity::class.java)
            startActivity(intent)
        }

        filmDetailBinding.btnDetail.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.film_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_updateFilm -> {
                        Toast.makeText(this, "Chỉnh sửa", Toast.LENGTH_SHORT).show()
                        // TODO: Chuyển sang màn hình Thông tin người dùng
                        val intent = Intent(this, EditFilmActivity::class.java)
                        intent.putExtra("filmID", filmID)
                        startActivity(intent)
                        true
                    }
                    R.id.menu_filmRating -> {
                        Toast.makeText(this, "Đánh giá", Toast.LENGTH_SHORT).show()
                        // TODO: Xóa session, chuyển về màn hình đăng nhập
                        val intent = Intent(this, FilmRatingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }
                    R.id.menu_filmDelete -> {
                        AlertDialog.Builder(this)
                            .setTitle("Xác nhận xóa phim")
                            .setMessage("Bạn có chắc chắn muốn xóa phim này?")
                            .setPositiveButton("Xóa") { _, _ ->
                                userRef.child(filmID).removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Xóa phim thành công", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, FilmListActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Xóa phim thất bại", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .setNegativeButton("Hủy", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

    }
    private fun loadData(filmID: String){
        val ref = userRef.child(filmID)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val filmName = snapshot.child("title").getValue(String::class.java) ?: ""
                val year = snapshot.child("releaseYear").getValue(String::class.java) ?: ""
                val director = snapshot.child("director").getValue(String::class.java) ?: ""
                val synopsis = snapshot.child("synopsis").getValue(String::class.java) ?: ""
                val posterURL = snapshot.child("posterURL").getValue(String::class.java) ?: ""
                val trailerURL = snapshot.child("trailerURL").getValue(String::class.java) ?: ""


                filmDetailBinding.dtFilmName.setText(filmName)
                filmDetailBinding.dtDirector.setText(director)
                filmDetailBinding.dtSynopsis.setText(synopsis)
                filmDetailBinding.dtTrailer.setText(trailerURL)
                filmDetailBinding.dtYear.setText(year)

                Glide.with(this@FilmDetailActivity)
                    .load(posterURL)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user)
                    .into(filmDetailBinding.imgAvatarFilm)
                val genreSnapshot = snapshot.child("genre")
                val genreList = mutableListOf<String>()
                for (child in genreSnapshot.children) {
                    val genreName = child.child("genreName").getValue(String::class.java)
                    if (!genreName.isNullOrEmpty()) {
                        genreList.add(genreName)
                    }
                }
                filmDetailBinding.dtGenres.text = " ${genreList.joinToString(", ")}"

                // ➕ Hiển thị diễn viên
                val actorSnapshot = snapshot.child("actor")
                val actorList = mutableListOf<String>()
                for (child in actorSnapshot.children) {
                    val actorName = child.child("actorName").getValue(String::class.java)
                    if (!actorName.isNullOrEmpty()) {
                        actorList.add(actorName)
                    }
                }
                filmDetailBinding.dtActors.text = " ${actorList.joinToString(", ")}"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FilmDetailActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }
}