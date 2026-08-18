package com.example.danhgiaphim.data.repository

import com.example.danhgiaphim.data.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class AuthRepository @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    suspend fun login(email: String, password: String): LoginResult {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Không tìm thấy người dùng")
        val role = getUserRole(user.uid)

        return when (role) {
            "Admin" -> LoginResult.Admin(user.uid)
            "User" -> {
                if (user.isEmailVerified) {
                    LoginResult.User(user.uid)
                } else {
                    user.sendEmailVerification().await()
                    LoginResult.EmailNotVerified
                }
            }
            else -> LoginResult.InvalidRole
        }
    }

    suspend fun restoreSession(): LoginResult? {
        val user = auth.currentUser ?: return null
        val role = getUserRole(user.uid)
        return when (role) {
            "Admin" -> LoginResult.Admin(user.uid)
            "User" -> {
                if (user.isEmailVerified) {
                    LoginResult.User(user.uid)
                } else {
                    signOut()
                    LoginResult.EmailNotVerified
                }
            }
            else -> LoginResult.InvalidRole
        }
    }

    suspend fun register(email: String, password: String) {
        val existingMethods = auth.fetchSignInMethodsForEmail(email).await().signInMethods
        if (!existingMethods.isNullOrEmpty()) {
            throw IllegalStateException("Email đã tồn tại")
        }

        val existingUser = database.getReference("Users")
            .orderByChild("email")
            .equalTo(email)
            .get()
            .await()
        if (existingUser.exists()) {
            throw IllegalStateException("Email đã tồn tại")
        }

        val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: throw IllegalStateException("Không thể tạo tài khoản")
        firebaseUser.sendEmailVerification().await()

        val user = Users(
            userID = firebaseUser.uid,
            email = email,
            dateOfBirth = System.currentTimeMillis(),
            role = "User"
        )
        database.getReference("Users").child(firebaseUser.uid).setValue(user).await()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun getUserRole(uid: String): String? {
        return database.getReference("Users").child(uid).child("role")
            .get()
            .await()
            .getValue(String::class.java)
    }
}

sealed class LoginResult {
    data class Admin(val uid: String) : LoginResult()
    data class User(val uid: String) : LoginResult()
    object EmailNotVerified : LoginResult()
    object InvalidRole : LoginResult()
}
