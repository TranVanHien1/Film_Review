package com.example.danhgiaphim

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.ui.auth.LoginViewModel
import com.example.danhgiaphim.ui.compose.DanhGiaPhimTheme
import com.example.danhgiaphim.ui.compose.LoginScreen
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            DanhGiaPhimTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onRegister = { startActivity(Intent(this, SignActivity::class.java)) },
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
