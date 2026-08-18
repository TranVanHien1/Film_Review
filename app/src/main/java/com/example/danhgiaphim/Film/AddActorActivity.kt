package com.example.danhgiaphim.Film

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Admin.ActorListActivity
import com.example.danhgiaphim.ui.compose.AddActorScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.film.AddActorViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActorActivity : AppCompatActivity() {
    private val viewModel: AddActorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                AddActorScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, ActorListActivity::class.java)) },
                    onDone = { finish() }
                )
            }
        }
    }
}
