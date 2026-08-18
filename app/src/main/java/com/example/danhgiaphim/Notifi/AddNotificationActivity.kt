package com.example.danhgiaphim.Notifi

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.NotificationManageScreen
import com.example.danhgiaphim.ui.notification.NotificationManageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNotificationActivity : AppCompatActivity() {
    private val viewModel: NotificationManageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                NotificationManageScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, AdminActivity::class.java)) }
                )
            }
        }
        viewModel.load()
    }
}
