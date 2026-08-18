package com.example.danhgiaphim

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.MainScreen
import com.example.danhgiaphim.ui.main.MainViewModel
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        viewModel.restoreSession()

        setContent {
            DanhGiaPhimTheme {
                MainScreen(
                    viewModel = viewModel,
                    onLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    onRegister = {
                        startActivity(Intent(this, SignActivity::class.java))
                        finish()
                    },
                    onAdmin = { openAndFinish(AdminActivity::class.java) },
                    onHome = { openAndFinish(HomeActivity::class.java) }
                )
            }
        }
    }

    private fun openAndFinish(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }
}
