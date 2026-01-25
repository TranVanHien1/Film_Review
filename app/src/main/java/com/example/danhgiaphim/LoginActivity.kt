package com.example.danhgiaphim

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.MotionEffect
import com.example.danhgiaphim.adapter.LoadingDialog
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    lateinit var loginBinding: ActivityLoginBinding

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)

        FirebaseApp.initializeApp(this)

        loginBinding.btnLogin.setOnClickListener(){
            loginUser()

        }

        loginBinding.txtSignup.setOnClickListener(){
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }

        loginBinding.txtForgerPass.setOnClickListener(){
            showForgotPasswordDialog(this)
        }

    }


    private fun loginUser1() {
        val email = loginBinding.edemailLg.text.toString()
        val passwordHash = loginBinding.edpasswordLg.text.toString()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, passwordHash)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Đóng LoginActivity sau khi chuyển hướng
                } else {
                    // Xử lý lỗi đăng nhập
                    Toast.makeText(
                        applicationContext, "Vui lòng nhập đầy đủ thông tin!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun loginUser() {
        val email = loginBinding.edemailLg.text.toString()
        val passwordHash = loginBinding.edpasswordLg.text.toString()
        val auth = FirebaseAuth.getInstance()

        if (email.isEmpty() || passwordHash.isEmpty()) {
            Toast.makeText(applicationContext, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG).show()
        } else {
            val loadingDialog = LoadingDialog(this)
            loadingDialog.show("Đang đăng nhập...")

            auth.signInWithEmailAndPassword(email, passwordHash)
                .addOnCompleteListener { task ->
                    loadingDialog.dismiss()
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            val userId = currentUser.uid
                            checkUserRole(userId, currentUser.isEmailVerified)
                        }
                    } else {
                        Log.e("FirebaseAuth", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            applicationContext, "Sai Tài Khoản Hoặc Mật khẩu!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun checkUserRole(userId: String, isEmailVerified: Boolean) {
        val database = FirebaseDatabase.getInstance().getReference("Users/$userId/role")
        val auth = FirebaseAuth.getInstance()

        database.get().addOnSuccessListener { dataSnapshot ->
            val role = dataSnapshot.getValue(String::class.java)
            if (role == "Admin") {
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
                finish()
            } else if (role == "User") {
                if (isEmailVerified) {
                    val intent = Intent(this, HomeActivity::class.java)
                    UserSession.uid = userId
                    startActivity(intent)
                    finish()
                } else {
                    // ❗ Nếu chưa xác minh → hiện dialog gửi lại email
                    showResendVerificationDialog(auth)
                }
            } else {
                Toast.makeText(this, "Vai trò người dùng không hợp lệ.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi truy cập dữ liệu vai trò người dùng.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showResendVerificationDialog(user: FirebaseAuth) {
        val firebaseUser = user.currentUser
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Xác minh email chưa được hoàn tất")
        builder.setMessage("Tài khoản của bạn chưa xác minh email. Bạn có muốn gửi lại email xác minh không?")

        builder.setPositiveButton("Gửi lại") { dialog, _ ->
            firebaseUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Email xác minh đã được gửi lại. Vui lòng kiểm tra hộp thư.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Gửi email xác minh thất bại: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            dialog.dismiss()
        }

        builder.setNegativeButton("Để sau") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    fun showForgotPasswordDialog(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_forgot_password, null)
        val edEmail = view.findViewById<EditText>(R.id.edForgotEmail)

        AlertDialog.Builder(context)
            .setTitle("Quên mật khẩu")
            .setView(view)
            .setPositiveButton("Gửi") { _, _ ->
                val email = edEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }


}