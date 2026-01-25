package com.example.danhgiaphim.Notifi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.adapter.NotificationAdapter
import com.example.danhgiaphim.data.Notification
import com.example.danhgiaphim.databinding.ActivityAddNotificationBinding
import com.example.danhgiaphim.databinding.ActivityProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddNotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNotificationBinding
    private lateinit var recyclerView: RecyclerView
    private val notifications = mutableListOf<Notification>()
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Notifications")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)


        val adapter = NotificationAdapter(this, notifications) { notif ->
            showEditDialog(notif)
        }

        recyclerView.adapter = adapter

        // Load data
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notifications.clear()
                for (child in snapshot.children) {
                    val notif = child.getValue(Notification::class.java)
                    notif?.let { notifications.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.btnAddNot.setOnClickListener(){
            showAddNotificationDialog(this)
        }
        binding.btnBackToAdminFromNot.setOnClickListener(){
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
    }

    fun showAddNotificationDialog(activity: AppCompatActivity) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_notification, null)
        val edTitle = dialogView.findViewById<EditText>(R.id.edTitle)
        val edContent = dialogView.findViewById<EditText>(R.id.edContent)

        val dialog = AlertDialog.Builder(activity)
            .setTitle("Gửi thông báo mới")
            .setView(dialogView)
            .setPositiveButton("Gửi") { _, _ ->
                val title = edTitle.text.toString().trim()
                val content = edContent.text.toString().trim()

                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(activity, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val ref = FirebaseDatabase.getInstance().getReference("Notifications")
                val id = ref.push().key ?: return@setPositiveButton
                val notification = Notification(id, title, content, System.currentTimeMillis())

                ref.child(id).setValue(notification).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Gửi thông báo thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .create()

        dialog.show()
    }

    private fun showEditDialog(notification: Notification) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_notification, null)
        val edTitle = view.findViewById<EditText>(R.id.edEditTitle)
        val edContent = view.findViewById<EditText>(R.id.edEditContent)

        edTitle.setText(notification.title)
        edContent.setText(notification.content)

        AlertDialog.Builder(this)
            .setTitle("Sửa hoặc xoá thông báo")
            .setView(view)
            .setPositiveButton("Lưu") { _, _ ->
                val newTitle = edTitle.text.toString()
                val newContent = edContent.text.toString()
                val updatedNotif = notification.copy(title = newTitle, content = newContent)

                databaseRef.child(notification.id).setValue(updatedNotif)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Xoá") { _, _ ->
                databaseRef.child(notification.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNeutralButton("Huỷ", null)
            .show()
    }
}