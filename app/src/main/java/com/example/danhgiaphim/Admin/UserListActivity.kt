package com.example.danhgiaphim.Admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.ui.admin.UserListViewModel
import com.example.danhgiaphim.ui.compose.AdminUserListScreen
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListActivity : AppCompatActivity() {
    private val viewModel: UserListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DanhGiaPhimTheme {
                AdminUserListScreen(
                    viewModel = viewModel,
                    onBack = { startActivity(Intent(this, AdminActivity::class.java)) },
                    onUserClick = {
                        startActivity(Intent(this, UserDetailActivity::class.java).putExtra("userID", it.userID))
                    }
                )
            }
        }
        viewModel.load()
    }
}
