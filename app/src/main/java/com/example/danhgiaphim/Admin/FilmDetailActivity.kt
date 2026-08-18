package com.example.danhgiaphim.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Film.EditFilmActivity
import com.example.danhgiaphim.Film.FilmRatingActivity
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.ui.admin.FilmDetailViewModel
import com.example.danhgiaphim.ui.compose.AdminFilmDetailScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilmDetailActivity : AppCompatActivity() {
    private val viewModel: FilmDetailViewModel by viewModels()
    private val filmID = FilmSession.filmid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (filmID.isNullOrEmpty()) {
            Toast.makeText(this, "Không có filmID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            DanhGiaPhimTheme {
                AdminFilmDetailScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, FilmListActivity::class.java)) },
                    onEdit = { startActivity(Intent(this, EditFilmActivity::class.java).putExtra("filmID", filmID)) },
                    onRating = {
                        val intent = Intent(this, FilmRatingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    },
                    onDeleted = {
                        val intent = Intent(this, FilmListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                )
            }
        }
        viewModel.load(filmID)
    }
}
