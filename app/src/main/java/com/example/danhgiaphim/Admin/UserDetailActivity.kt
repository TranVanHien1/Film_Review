package com.example.danhgiaphim.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.danhgiaphim.R
import com.example.danhgiaphim.databinding.ActivityUserDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class UserDetailActivity : AppCompatActivity() {

    lateinit var userDetailBinding: ActivityUserDetailBinding
    var database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val userRef : DatabaseReference = database.reference.child("Users")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDetailBinding = ActivityUserDetailBinding.inflate(layoutInflater)
        val view = userDetailBinding.root
        setContentView(view)


        val userID = intent.getStringExtra("userID")
        if (userID.isNullOrEmpty()) {
            Toast.makeText(this, "Không có userID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadData(userID)
        userDetailBinding.btnBackToUserListFromDetail.setOnClickListener(){
            val intent = Intent(this, UserListActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadData(userID: String){
        val ref = userRef.child(userID)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                val gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                val dobMillis = snapshot.child("dateOfBirth").getValue(Long::class.java) ?: 0L
                val avatarURL = snapshot.child("avatarURL").getValue(String::class.java) ?: ""
                val userID = snapshot.child("userID").getValue(String::class.java) ?: ""

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dobMillis
                val dobFormatted = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"

                userDetailBinding.dtUsername.setText(username)
                userDetailBinding.dtEmail.setText(email)
                userDetailBinding.dtGender.setText(gender)
                userDetailBinding.dtDateOfBirth.setText(dobFormatted)
                userDetailBinding.dtUserID.setText(userID)

                Glide.with(this@UserDetailActivity)
                    .load(avatarURL)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user)
                    .into(userDetailBinding.imgAvatar)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserDetailActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

}