package com.example.danhgiaphim.User

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var userRef: DatabaseReference
    private val uid = UserSession.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (uid == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)

        binding.btnChangePassword.setOnClickListener {
            val oldPass = binding.edtOldPassword.text.toString()
            val newPass = binding.edtNewPassword.text.toString()
            val confirmPass = binding.edtConfirmPassword.text.toString()

            if (newPass != confirmPass) {
                Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentPassword = snapshot.child("passwordHash").getValue(String::class.java)

                    if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()){
                        Toast.makeText(applicationContext, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG).show()
                    }
                    else if (newPass != confirmPass){
                        Toast.makeText(applicationContext, "Mật khẩu nhập lại không đúng", Toast.LENGTH_LONG).show()
                    }

                    else if (newPass.length < 6 || !Character.isUpperCase(newPass.get(0))) {
                        Toast.makeText(
                            applicationContext,
                            "Mật khẩu phải có ít nhất 6 kí tự và viết hoa chữ cái đầu tiên!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else if (currentPassword == oldPass) {
                        userRef.child("passwordHash").setValue(newPass)
                            .addOnSuccessListener {
                                FirebaseAuth.getInstance().currentUser?.updatePassword(newPass)
                                    ?.addOnSuccessListener {
                                        Toast.makeText(this@ChangePasswordActivity, "Mật khẩu đã cập nhật trong Authentication", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    ?.addOnFailureListener { e ->
                                        Toast.makeText(this@ChangePasswordActivity, "Đổi mật khẩu thất bại trong Auth: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@ChangePasswordActivity,
                                    "Đổi mật khẩu thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@ChangePasswordActivity,
                                    "Đổi mật khẩu thất bại",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChangePasswordActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                }
            })
        }
        binding.btnBackToUserFromPass.setOnClickListener(){
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }
    }
}
