package com.example.danhgiaphim.Admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.Film.AddActorActivity
import com.example.danhgiaphim.ui.admin.ActorListViewModel
import com.example.danhgiaphim.ui.compose.AdminActorListScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActorListActivity : AppCompatActivity() {
    private val viewModel: ActorListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                AdminActorListScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, AdminActivity::class.java)) },
                    onAdd = { startActivity(Intent(this, AddActorActivity::class.java)) }
                )
            }
        }
        viewModel.load()
    }
}
