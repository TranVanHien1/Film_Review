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
import com.example.danhgiaphim.ui.compose.EditFilmScreen
import com.example.danhgiaphim.ui.film.EditFilmViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditFilmActivity : AppCompatActivity() {
    private val viewModel: EditFilmViewModel by viewModels()
    private var filmID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filmID = FilmSession.filmid ?: intent.getStringExtra("filmID").orEmpty()
        if (filmID.isBlank()) {
            Toast.makeText(this, "Không có filmID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            DanhGiaPhimTheme {
                EditFilmScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, FilmDetailActivity::class.java)) },
                    onDone = { finish() }
                )
            }
        }
        viewModel.load(filmID)
    }
}
