package com.example.danhgiaphim.Admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.Film.AddFilmActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.ui.admin.FilmListViewModel
import com.example.danhgiaphim.ui.compose.AdminFilmListScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilmListActivity : AppCompatActivity() {
    private val viewModel: FilmListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                AdminFilmListScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, AdminActivity::class.java)) },
                    onAdd = { startActivity(Intent(this, AddFilmActivity::class.java)) },
                    onFilmClick = {
                        FilmSession.filmid = it.movieID
                        startActivity(Intent(this, FilmDetailActivity::class.java))
                    }
                )
            }
        }
        viewModel.load()
    }
}
