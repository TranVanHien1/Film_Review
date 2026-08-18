package com.example.danhgiaphim.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.ui.admin.UserDetailViewModel
import com.example.danhgiaphim.ui.compose.AdminUserDetailScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailActivity : AppCompatActivity() {
    private val viewModel: UserDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userID = intent.getStringExtra("userID")
        if (userID.isNullOrEmpty()) {
            Toast.makeText(this, "Không có userID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            DanhGiaPhimTheme {
                AdminUserDetailScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, UserListActivity::class.java)) }
                )
            }
        }
        viewModel.load(userID)
    }
}
