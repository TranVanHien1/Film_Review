package com.example.danhgiaphim

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Admin.ActorListActivity
import com.example.danhgiaphim.Admin.FilmListActivity
import com.example.danhgiaphim.Admin.GenreListActivity
import com.example.danhgiaphim.Admin.UserListActivity
import com.example.danhgiaphim.Notifi.AddNotificationActivity
import com.example.danhgiaphim.ui.admin.AdminHomeViewModel
import com.example.danhgiaphim.ui.compose.AdminHomeScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActivity : AppCompatActivity() {
    private val viewModel: AdminHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                AdminHomeScreen(
                    onUsers = { startActivity(Intent(this, UserListActivity::class.java)) },
                    onFilms = { startActivity(Intent(this, FilmListActivity::class.java)) },
                    onActors = { startActivity(Intent(this, ActorListActivity::class.java)) },
                    onGenres = { startActivity(Intent(this, GenreListActivity::class.java)) },
                    onNotifications = { startActivity(Intent(this, AddNotificationActivity::class.java)) },
                    onLogout = {
                        viewModel.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
