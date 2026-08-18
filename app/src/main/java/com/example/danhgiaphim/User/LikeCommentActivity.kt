package com.example.danhgiaphim.User

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Film.FilmProfileActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.UserCommentsScreen
import com.example.danhgiaphim.ui.user.LikeCommentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikeCommentActivity : AppCompatActivity() {
    private val viewModel: LikeCommentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                UserCommentsScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, UserActivity::class.java)) },
                    onMovieClick = {
                        FilmSession.filmid = it.filmID
                        startActivity(Intent(this, FilmProfileActivity::class.java))
                    }
                )
            }
        }
        viewModel.load()
    }
}
