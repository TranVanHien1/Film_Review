package com.example.danhgiaphim.Film

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.HomeActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.FilmProfileScreen
import com.example.danhgiaphim.ui.film.FilmProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilmProfileActivity : AppCompatActivity() {
    private val viewModel: FilmProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filmID = FilmSession.filmid ?: ""
        if (filmID.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID phim!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            DanhGiaPhimTheme {
                FilmProfileScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, HomeActivity::class.java)) },
                    onTrailer = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                )
            }
        }
        viewModel.load(filmID)
    }
}
