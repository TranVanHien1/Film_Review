package com.example.danhgiaphim.Film

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Admin.FilmListActivity
import com.example.danhgiaphim.ui.compose.AddFilmScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.film.AddFilmViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFilmActivity : AppCompatActivity() {
    private val viewModel: AddFilmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                AddFilmScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, FilmListActivity::class.java)) },
                    onDone = { finish() }
                )
            }
        }
    }
}
