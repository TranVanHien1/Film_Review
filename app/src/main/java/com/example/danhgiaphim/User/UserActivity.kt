package com.example.danhgiaphim.User

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.Film.FilmProfileActivity
import com.example.danhgiaphim.HomeActivity
import com.example.danhgiaphim.LoginActivity
import com.example.danhgiaphim.Notifi.NotificationListActivity
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.UserScreen
import com.example.danhgiaphim.ui.user.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                UserScreen(
                    viewModel = viewModel,
                    onBack = {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    },
                    onProfile = { startActivity(Intent(this, ProfileActivity::class.java)) },
                    onPassword = { startActivity(Intent(this, ChangePasswordActivity::class.java)) },
                    onComments = { startActivity(Intent(this, LikeCommentActivity::class.java)) },
                    onNotifications = { startActivity(Intent(this, NotificationListActivity::class.java)) },
                    onDeleted = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
        viewModel.load()
    }
}
