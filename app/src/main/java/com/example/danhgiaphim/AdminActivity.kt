package com.example.danhgiaphim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cloudinary.android.MediaManager
import com.example.danhgiaphim.Admin.ActorListActivity
import com.example.danhgiaphim.Admin.FilmListActivity
import com.example.danhgiaphim.Admin.GenreListActivity
import com.example.danhgiaphim.Admin.UserListActivity
import com.example.danhgiaphim.Notifi.AddNotificationActivity
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.databinding.ActivityAdminBinding
import java.util.HashMap

class AdminActivity : AppCompatActivity() {

    lateinit var adminBinding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adminBinding = ActivityAdminBinding.inflate(layoutInflater)
        val view = adminBinding.root
        setContentView(view)

        adminBinding.btnDanhSachUser.setOnClickListener(){
            val intent = Intent(this, UserListActivity::class.java)
            startActivity(intent)
        }
        adminBinding.btnDanhSachPhim.setOnClickListener(){
            val intent = Intent(this, FilmListActivity::class.java)
            startActivity(intent)
        }
        adminBinding.btnDanhSachDienVien.setOnClickListener(){
            val intent = Intent(this, ActorListActivity::class.java)
            startActivity(intent)
        }
        adminBinding.btnDanhSachTheLoai.setOnClickListener(){
            val intent = Intent(this, GenreListActivity::class.java)
            startActivity(intent)
        }

        adminBinding.btnDanhSachThongbao.setOnClickListener(){
            val intent = Intent(this, AddNotificationActivity::class.java)
            startActivity(intent)
        }
        adminBinding.btnlogout.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}