package com.example.danhgiaphim.Notifi

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.NotificationListScreen
import com.example.danhgiaphim.ui.notification.NotificationListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationListActivity : AppCompatActivity() {
    private val viewModel: NotificationListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                NotificationListScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, UserActivity::class.java)) }
                )
            }
        }
        viewModel.load()
    }
}
