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
import com.example.danhgiaphim.Film.AddFilmActivity
import com.example.danhgiaphim.adapter.AdminFilmAdapter
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.databinding.ActivityFilmListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FilmListActivity : AppCompatActivity() {

    lateinit var listFilmBinding : ActivityFilmListBinding
    var database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRef : DatabaseReference = database.reference.child("Films")

    val filmList = ArrayList<Films>()

    lateinit var filmAdapter : AdminFilmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listFilmBinding = ActivityFilmListBinding.inflate(layoutInflater)
        val view = listFilmBinding.root
        setContentView(view)

        listFilmBinding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        fetchFilms()
        listFilmBinding.txtSearchFilmInList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    fetchAllFilms()
                } else {
                    searchFilms(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        filmAdapter = AdminFilmAdapter(this, filmList).apply {
            // Adapter không cần sửa gì thêm, vì click đã xử lý bên trong adapter
        }

        listFilmBinding.btnBackToAdminFromFilmlist.setOnClickListener(){
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
        listFilmBinding.btnAddFilm.setOnClickListener(){
            val intent = Intent(this, AddFilmActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchFilms() {
        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                filmList.clear()
                for (userSnapshot in snapshot.children) {
                    val film = userSnapshot.getValue(Films::class.java)
                    if (film != null) {
                        filmList.add(film)
                    }
                }
                filmAdapter = AdminFilmAdapter(this@FilmListActivity, filmList)
                listFilmBinding.recyclerViewUsers.layoutManager = LinearLayoutManager(this@FilmListActivity)
                listFilmBinding.recyclerViewUsers.adapter = filmAdapter
                filmAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FilmListActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAllFilms() {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                filmList.clear()
                for (filmSnapshot in snapshot.children) {
                    val film = filmSnapshot.getValue(Films::class.java)
                    film?.let { filmList.add(it) }
                }
                filmAdapter = AdminFilmAdapter(this@FilmListActivity, filmList)
                listFilmBinding.recyclerViewUsers.layoutManager = LinearLayoutManager(this@FilmListActivity)
                listFilmBinding.recyclerViewUsers.adapter = filmAdapter
                filmAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FilmListActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchFilms(query: String) {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                filmList.clear()
                for (filmSnapshot in snapshot.children) {
                    val film = filmSnapshot.getValue(Films::class.java)
                    if (film != null && film.title?.contains(query, ignoreCase = true) == true) {
                        filmList.add(film)
                    }
                }
                filmAdapter = AdminFilmAdapter(this@FilmListActivity, filmList)
                listFilmBinding.recyclerViewUsers.layoutManager = LinearLayoutManager(this@FilmListActivity)
                listFilmBinding.recyclerViewUsers.adapter = filmAdapter
                filmAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FilmListActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}