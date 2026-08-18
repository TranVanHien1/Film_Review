package com.example.danhgiaphim.Admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.ui.admin.GenreListViewModel
import com.example.danhgiaphim.ui.compose.AdminGenreListScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenreListActivity : AppCompatActivity() {
    private val viewModel: GenreListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                AdminGenreListScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, AdminActivity::class.java)) }
                )
            }
        }
        viewModel.load()
    }
}
