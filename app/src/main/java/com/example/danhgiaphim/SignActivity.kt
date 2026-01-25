package com.example.danhgiaphim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.danhgiaphim.adapter.LoadingDialog
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ActivitySignBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignActivity : AppCompatActivity() {

    lateinit var addUserBinding : ActivitySignBinding
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myReference : DatabaseReference = database.reference.child("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addUserBinding = ActivitySignBinding.inflate(layoutInflater)
        val view = addUserBinding.root

        setContentView(view)



        addUserBinding.btnsignup.setOnClickListener(){
            addUsers()
        }

        addUserBinding.txtLogin.setOnClickListener(){
            goLogin()
        }
    }

    fun addUsers() {
        val auth = FirebaseAuth.getInstance()
        val email = addUserBinding.edemail.text.toString()
        val pas = addUserBinding.edpassword.text.toString()
        val pas2 = addUserBinding.edrppassword.text.toString()
        val currentTimeMillis = System.currentTimeMillis()

        if (email.isEmpty() || pas.isEmpty() || pas2.isEmpty()) {
            Toast.makeText(applicationContext, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG).show()
            return
        }
        if (pas != pas2) {
            Toast.makeText(applicationContext, "Mật khẩu nhập lại không đúng", Toast.LENGTH_LONG).show()
            return
        }
        if (!isValidEmail(email)) {
            Toast.makeText(applicationContext, "Địa chỉ email không hợp lệ!", Toast.LENGTH_SHORT).show()
            return
        }
        if (pas.length < 6 || !Character.isUpperCase(pas[0])) {
            Toast.makeText(
                applicationContext,
                "Mật khẩu phải có ít nhất 6 kí tự và viết hoa chữ cái đầu tiên!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val loadingDialog = LoadingDialog(this)
        loadingDialog.show("Đang tạo tài khoản...")

        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods != null && signInMethods.isNotEmpty()) {
                    loadingDialog.dismiss()
                    Toast.makeText(applicationContext, "Email đã tồn tại", Toast.LENGTH_LONG).show()
                } else {
                    // Kiểm tra trong Realtime Database
                    myReference.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                if (snapshot.exists()) {
                                    loadingDialog.dismiss()
                                    Toast.makeText(applicationContext, "Email đã tồn tại", Toast.LENGTH_LONG).show()
                                } else {
                                    // Tạo tài khoản
                                    auth.createUserWithEmailAndPassword(email, pas)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val firebaseUser = auth.currentUser
                                                firebaseUser?.sendEmailVerification()
                                                    ?.addOnCompleteListener { verifyTask ->
                                                        if (verifyTask.isSuccessful) {
                                                            val userId = firebaseUser.uid
                                                            val user = Users(
                                                                userId, "", email, pas, "", "", currentTimeMillis, "", "User"
                                                            )
                                                            myReference.child(userId).setValue(user)
                                                                .addOnCompleteListener { dbTask ->
                                                                    loadingDialog.dismiss()
                                                                    if (dbTask.isSuccessful) {
                                                                        Toast.makeText(
                                                                            applicationContext,
                                                                            "Tạo tài khoản thành công. Vui lòng xác minh email trước khi đăng nhập.",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                        goLogin()
                                                                    } else {
                                                                        Toast.makeText(
                                                                            applicationContext,
                                                                            "Lỗi lưu dữ liệu: ${dbTask.exception?.message}",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                    }
                                                                }
                                                        } else {
                                                            loadingDialog.dismiss()
                                                            Toast.makeText(
                                                                applicationContext,
                                                                "Không thể gửi email xác minh: ${verifyTask.exception?.message}",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                            } else {
                                                loadingDialog.dismiss()
                                                Toast.makeText(applicationContext, "Tạo tài khoản thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                                Log.e("FirebaseAuth", "Lỗi khi tạo user", task.exception)
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                                loadingDialog.dismiss()
                                Toast.makeText(applicationContext, "Lỗi kiểm tra database: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                }
            } else {
                loadingDialog.dismiss()
                Toast.makeText(applicationContext, "Lỗi kiểm tra email", Toast.LENGTH_LONG).show()
            }
        }
    }



    fun goLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}