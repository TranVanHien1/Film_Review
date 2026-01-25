package com.example.danhgiaphim.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.adapter.AdminGenreAdapter
import com.example.danhgiaphim.data.Genre
import com.example.danhgiaphim.databinding.ActivityGenreListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GenreListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenreListBinding
    private lateinit var genreAdapter: AdminGenreAdapter
    private lateinit var genreRef: DatabaseReference

    private var isAscending = true // true: A-Z, false: Z-A


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenreListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        genreRef = FirebaseDatabase.getInstance().getReference("Genre")
        genreAdapter = AdminGenreAdapter(mutableListOf()) { genre ->
            showEditGenreDialog(genre)
        }

        binding.recyclerViewGenre.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewGenre.adapter = genreAdapter

        binding.btnAddGenre.setOnClickListener {
            showAddGenreDialog()
        }
        binding.btnBackToAdminFromGenrelist.setOnClickListener(){
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        listenToGenres()

        binding.txtSearchGenreInList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    listenToGenres() // Tải toàn bộ danh sách
                } else {
                    searchGenres(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnSortGenre.setOnClickListener {
            sortGenres()
        }


    }

    private fun listenToGenres() {
        genreRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val genres = mutableListOf<Genre>()
                for (child in snapshot.children) {
                    val genre = child.getValue(Genre::class.java)
                    genre?.let { genres.add(it) }
                }
                genreAdapter.setGenres(genres)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GenreListActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddGenreDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_genre, null)
        builder.setView(dialogView)

        val edtGenreName = dialogView.findViewById<EditText>(R.id.edtGenreName)

        builder.setPositiveButton("OK") { dialog, _ ->
            val name = edtGenreName.text.toString().trim()
            if (name.isNotEmpty()) {
                val id = genreRef.push().key ?: return@setPositiveButton
                val genre = Genre(genreID = id, genreName = name)
                genreRef.child(id).setValue(genre)
            } else {
                Toast.makeText(this, "Tên thể loại không được trống", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
    private fun showEditGenreDialog(genre: Genre) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_genre, null)
        val edtGenreName = view.findViewById<EditText>(R.id.edtGenreName)
        edtGenreName.setText(genre.genreName)

        builder.setView(view)
            .setTitle("Chỉnh sửa thể loại")
            .setPositiveButton("Cập nhật") { dialog, _ ->
                val newName = edtGenreName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    genreRef.child(genre.genreID!!).child("genreName").setValue(newName)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Tên thể loại không được trống", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Xóa") { dialog, _ ->
                // Xác nhận trước khi xóa
                AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa thể loại này?")
                    .setPositiveButton("Xóa") { confirmDialog, _ ->
                        genreRef.child(genre.genreID!!).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Đã xóa thể loại", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show()
                            }
                        confirmDialog.dismiss()
                    }
                    .setNegativeButton("Hủy") { confirmDialog, _ ->
                        confirmDialog.dismiss()
                    }
                    .show()
                dialog.dismiss()
            }
            .show()
    }

    private fun searchGenres(query: String) {
        genreRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredGenres = mutableListOf<Genre>()
                for (child in snapshot.children) {
                    val genre = child.getValue(Genre::class.java)
                    if (genre != null && genre.genreName?.contains(query, ignoreCase = true) == true) {
                        filteredGenres.add(genre)
                    }
                }
                genreAdapter.setGenres(filteredGenres)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GenreListActivity, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sortGenres() {
        val currentList = genreAdapter.getGenres()
        val sortedList = if (isAscending) {
            currentList.sortedBy { it.genreName?.lowercase() }
        } else {
            currentList.sortedByDescending { it.genreName?.lowercase() }
        }

        genreAdapter.setGenres(sortedList)
        isAscending = !isAscending
    }


}