package com.example.danhgiaphim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.example.danhgiaphim.databinding.ActivityMainBinding
import com.example.danhgiaphim.databinding.ActivitySignBinding
import com.google.firebase.FirebaseApp
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        mainBinding.btnemail.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        mainBinding.btnOtp.setOnClickListener(){
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}