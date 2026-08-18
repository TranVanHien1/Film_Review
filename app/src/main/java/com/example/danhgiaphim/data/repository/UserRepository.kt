package com.example.danhgiaphim.data.repository

import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Users
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class UserRepository @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    suspend fun loadUser(uid: String): Users? {
        return database.getReference("Users").child(uid).get().await().getValue(Users::class.java)
    }

    suspend fun updateProfile(
        uid: String,
        username: String,
        gender: String,
        dateOfBirth: Long,
        avatarUrl: String?,
        avatarId: String?
    ) {
        val updates = mutableMapOf<String, Any>(
            "username" to username,
            "gender" to gender,
            "dateOfBirth" to dateOfBirth
        )
        if (!avatarUrl.isNullOrBlank()) updates["avatarURL"] = avatarUrl
        if (!avatarId.isNullOrBlank()) updates["avatarID"] = avatarId
        database.getReference("Users").child(uid).updateChildren(updates).await()
    }

    suspend fun deleteCurrentAccount(uid: String) {
        database.getReference("Users").child(uid).removeValue().await()
        auth.currentUser?.delete()?.await() ?: throw IllegalStateException("Chưa đăng nhập")
    }

    suspend fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Không tìm thấy người dùng")
        val email = user.email ?: throw IllegalStateException("Phiên đăng nhập không hợp lệ")
        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    suspend fun loadUserComments(uid: String): List<Triple<Comments, Users, String>> = coroutineScope {
        val user = loadUser(uid) ?: return@coroutineScope emptyList()
        val comments = database.getReference("Comments")
            .orderByChild("userID")
            .equalTo(uid)
            .get()
            .await()
            .children
            .mapNotNull { it.getValue(Comments::class.java) }

        comments.map { comment ->
            async {
                val movieTitle = database.getReference("Films")
                    .child(comment.filmID)
                    .child("title")
                    .get()
                    .await()
                    .getValue(String::class.java) ?: "Không rõ"
                Triple(comment, user, movieTitle)
            }
        }.awaitAll()
    }
}
