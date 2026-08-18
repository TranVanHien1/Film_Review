package com.example.danhgiaphim.Film

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Admin.FilmDetailActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.FilmRatingScreen
import com.example.danhgiaphim.ui.film.FilmRatingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilmRatingActivity : AppCompatActivity() {
    private val viewModel: FilmRatingViewModel by viewModels()
    private var filmID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filmID = FilmSession.filmid ?: ""
        if (filmID.isBlank()) {
            Toast.makeText(this, "Không có filmID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            DanhGiaPhimTheme {
                FilmRatingScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, FilmDetailActivity::class.java)) }
                )
            }
        }
        viewModel.load(filmID)
    }
}
