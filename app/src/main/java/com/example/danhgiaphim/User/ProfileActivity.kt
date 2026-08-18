package com.example.danhgiaphim.User

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.ProfileScreen
import com.example.danhgiaphim.ui.user.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, UserActivity::class.java)) },
                    onSaved = { finish() }
                )
            }
        }
        viewModel.load()
    }
}
