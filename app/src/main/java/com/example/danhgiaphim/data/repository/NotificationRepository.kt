package com.example.danhgiaphim.data.repository

import com.example.danhgiaphim.data.Notification
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NotificationRepository @Inject constructor() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    suspend fun loadNotifications(): List<Notification> {
        return database.getReference("Notifications").get().await().children
            .mapNotNull { it.getValue(Notification::class.java) }
            .sortedByDescending { it.date }
    }

    suspend fun addNotification(title: String, content: String) {
        val id = database.reference.push().key ?: UUID.randomUUID().toString()
        database.getReference("Notifications").child(id)
            .setValue(Notification(id, title, content, System.currentTimeMillis()))
            .await()
    }

    suspend fun updateNotification(notification: Notification) {
        database.getReference("Notifications").child(notification.id).setValue(notification).await()
    }

    suspend fun deleteNotification(id: String) {
        database.getReference("Notifications").child(id).removeValue().await()
    }
}
