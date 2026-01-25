package com.example.danhgiaphim.User

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.danhgiaphim.HomeActivity
import com.example.danhgiaphim.LoginActivity
import com.example.danhgiaphim.Notifi.NotificationListActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.databinding.ActivityHomeBinding
import com.example.danhgiaphim.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class UserActivity : AppCompatActivity() {

    lateinit var userBinding: ActivityUserBinding

    private lateinit var userRef: DatabaseReference
    private val uid = UserSession.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userBinding = ActivityUserBinding.inflate(layoutInflater)
        val view = userBinding.root
        setContentView(view)

        if (uid != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
            loadUserData()
        } else {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
        }

        userBinding.btnback.setOnClickListener(){
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        userBinding.layoutUpdate.setOnClickListener(){
            val intent = Intent(this, ProfileActivity::class.java)

            startActivity(intent)
        }

        userBinding.layoutPassword.setOnClickListener(){
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        userBinding.layoutDeleteAccount.setOnClickListener(){
            showConfirmDeleteDialog()
        }

        userBinding.layoutComment.setOnClickListener(){
            val intent = Intent(this, LikeCommentActivity::class.java)
            startActivity(intent)
        }
        userBinding.layoutNot.setOnClickListener(){
            val intent = Intent(this, NotificationListActivity::class.java)
            startActivity(intent)
        }


    }
    private fun loadUserData() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val avatarUrl = snapshot.child("avatarURL").getValue(String::class.java) ?: ""

                if (avatarUrl.isNotEmpty()) {
                    Glide.with(this@UserActivity)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(userBinding.imgAvatarUser)
                }

                val userName = snapshot.child("username").getValue(String::class.java) ?: ""
                userBinding.txtUser.text = userName
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserActivity, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun deleteUserAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        // Bước 1: Xóa dữ liệu user trong Realtime Database
        userRef.removeValue()
            .addOnSuccessListener {
                // Bước 2: Xóa tài khoản Authentication
                currentUser.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Xóa tài khoản thành công", Toast.LENGTH_SHORT).show()
                        // Chuyển về màn hình đăng nhập hoặc màn hình chính tùy app
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Xóa tài khoản thất bại: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Xóa dữ liệu người dùng thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmDeleteDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Xác nhận")
        builder.setMessage("Bạn có chắc chắn muốn xóa tài khoản không?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            deleteUserAccount()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}