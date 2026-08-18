package com.example.danhgiaphim

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Film.FilmProfileActivity
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.HomeScreen
import com.example.danhgiaphim.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DanhGiaPhimTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onFilmClick = { film ->
                        FilmSession.filmid = film.movieID
                        startActivity(Intent(this, FilmProfileActivity::class.java))
                    },
                    onProfile = { startActivity(Intent(this, UserActivity::class.java)) },
                    onLogout = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                )
            }
        }
        viewModel.load()
    }
}
