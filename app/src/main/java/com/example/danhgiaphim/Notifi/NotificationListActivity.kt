package com.example.danhgiaphim.Notifi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.danhgiaphim.R
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.adapter.NotificationUserAdapter
import com.example.danhgiaphim.data.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: Button


    private val notifications = mutableListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_list)

        recyclerView = findViewById(R.id.recyclerViewNotif)
        val adapter = NotificationUserAdapter(this, notifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener(){
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }


        FirebaseDatabase.getInstance().getReference("Notifications")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notifications.clear()
                    for (child in snapshot.children) {
                        val notif = child.getValue(Notification::class.java)
                        notif?.let { notifications.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NotificationListActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            })
    }
}