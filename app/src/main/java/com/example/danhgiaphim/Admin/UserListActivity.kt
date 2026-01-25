package com.example.danhgiaphim.Admin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.adapter.AdminUserAdapter
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.databinding.ActivityUserListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserListActivity : AppCompatActivity() {

    lateinit var userListBinding : ActivityUserListBinding
    var database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRef : DatabaseReference = database.reference.child("Users")

    val userList = ArrayList<Users>()

    lateinit var userAdapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userListBinding = ActivityUserListBinding.inflate(layoutInflater)
        val view = userListBinding.root
        setContentView(view)
        userListBinding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)

        userAdapter = AdminUserAdapter(this, userList).apply {
            // Adapter không cần sửa gì thêm, vì click đã xử lý bên trong adapter
        }

        fetchUsers()

        userListBinding.txtSearchUserInList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    fetchUsers()
                } else {
                    searchUsers(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        userListBinding.btnBackToAdminFromUserlist.setOnClickListener(){
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

    }

    private fun fetchUsers() {
        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(Users::class.java)
                    if (user != null && user.role == "User") {
                        userList.add(user)
                    }
                }
                userAdapter = AdminUserAdapter(this@UserListActivity, userList)
                userListBinding.recyclerViewUsers.layoutManager = LinearLayoutManager(this@UserListActivity)
                userListBinding.recyclerViewUsers.adapter = userAdapter
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun searchUsers(query: String) {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredList = ArrayList<Users>()
                for (child in snapshot.children) {
                    val user = child.getValue(Users::class.java)
                    if (user != null && user.role == "User" &&
                        user.username?.contains(query, ignoreCase = true) == true
                    ) {
                        filteredList.add(user)
                    }
                }
                userAdapter.userlist = filteredList
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        })
    }

}