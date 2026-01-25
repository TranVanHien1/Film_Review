package com.example.danhgiaphim.Film

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.danhgiaphim.Admin.FilmDetailActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.data.FilmSession
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Genre
import com.example.danhgiaphim.databinding.ActivityEditFilmBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.HashMap

class EditFilmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditFilmBinding
    private var imageUri: Uri? = null
    private lateinit var filmRef: DatabaseReference
    private lateinit var genreRef: DatabaseReference
    private lateinit var actorRef: DatabaseReference
    private var selectedGenres = mutableMapOf<String, Genre>()
    private var selectedActors = mutableMapOf<String, Actors>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFilmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filmID = FilmSession.filmid ?: return

        filmRef = FirebaseDatabase.getInstance().getReference("Films").child(filmID)
        genreRef = FirebaseDatabase.getInstance().getReference("Genre")
        actorRef = FirebaseDatabase.getInstance().getReference("Actors")


        loadFilmData()

        binding.btnSelectGenres.setOnClickListener { selectGenres() }
        binding.btnSelectActors.setOnClickListener { selectActors() }

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.imgAvatarFilmEdit.setImageURI(it)
            }
        }

        binding.imgAvatarFilmEdit.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener { saveChanges() }
        binding.btnBackToFilmDetailFromFilmEdit.setOnClickListener(){
            val intent = Intent(this, FilmDetailActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadFilmData() {
        filmRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val film = snapshot.getValue(Films::class.java)
                film?.let {
                    binding.edtTitle.setText(it.title)
                    binding.edtYear.setText(it.releaseYear)
                    binding.edtDirector.setText(it.director)
                    binding.edtSynopsis.setText(it.synopsis)
                    binding.edtTrailerUrl.setText(it.trailerURL)

                    val avatarUrl = snapshot.child("posterURL").getValue(String::class.java) ?: ""
                    if (avatarUrl.isNotEmpty()) {
                        Glide.with(this@EditFilmActivity)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(binding.imgAvatarFilmEdit)
                    }

                    selectedGenres = (it.genre ?: mutableMapOf()) as MutableMap<String, Genre>
                    selectedActors = (it.actor ?: mutableMapOf()) as MutableMap<String, Actors>

                    updateGenreText()
                    updateActorText()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun selectGenres() {
        genreRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val genres = snapshot.children.mapNotNull { it.getValue(Genre::class.java) }
                val genreNames = genres.map { it.genreName }
                val genreMap = genres.associateBy { it.genreID }

                val checkedItems = genres.map { selectedGenres.containsKey(it.genreID) }.toBooleanArray()

                AlertDialog.Builder(this@EditFilmActivity)
                    .setTitle("Chọn thể loại")
                    .setMultiChoiceItems(genreNames.toTypedArray(), checkedItems) { _, which, isChecked ->
                        val genre = genres[which]
                        if (isChecked) selectedGenres[genre.genreID!!] = genre
                        else selectedGenres.remove(genre.genreID)
                    }
                    .setPositiveButton("OK") { dialog, _ ->
                        updateGenreText()
                        dialog.dismiss()
                    }
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun selectActors() {
        actorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allActors = snapshot.children.mapNotNull { it.getValue(Actors::class.java) }
                val selectedIDs = selectedActors.keys.toMutableSet()

                val dialogView = layoutInflater.inflate(R.layout.dialog_select_actor, null)
                val searchEditText = dialogView.findViewById<EditText>(R.id.searchEditText)
                val listView = dialogView.findViewById<ListView>(R.id.actorListView)

                val actorAdapter = ArrayAdapter<String>(
                    this@EditFilmActivity,
                    android.R.layout.simple_list_item_multiple_choice
                )

                // full data to keep reference
                val actorMap = allActors.associateBy { it.actorName }

                var filteredActors = allActors.toList()
                fun updateList(filter: String) {
                    filteredActors = allActors.filter {
                        it.actorName?.contains(filter, ignoreCase = true) == true
                    }
                    actorAdapter.clear()
                    actorAdapter.addAll(filteredActors.map { it.actorName ?: "" })
                    listView.adapter = actorAdapter

                    // restore checked state
                    for (i in filteredActors.indices) {
                        val actor = filteredActors[i]
                        listView.setItemChecked(i, selectedIDs.contains(actor.actorID))
                    }
                }

                updateList("")

                searchEditText.addTextChangedListener {
                    updateList(it.toString())
                }

                AlertDialog.Builder(this@EditFilmActivity)
                    .setTitle("Chọn diễn viên")
                    .setView(dialogView)
                    .setPositiveButton("OK") { dialog, _ ->
                        // Cập nhật danh sách chọn
                        for (i in filteredActors.indices) {
                            val actor = filteredActors[i]
                            if (listView.isItemChecked(i)) {
                                selectedActors[actor.actorID!!] = actor
                            } else {
                                selectedActors.remove(actor.actorID)
                            }
                        }
                        updateActorText()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun updateGenreText() {
        binding.txtSelectedGenres.text = selectedGenres.values.joinToString { it.genreName ?: "" }
    }

    private fun updateActorText() {
        binding.txtSelectedActors.text = selectedActors.values.joinToString { it.actorName ?: "" }
    }

    private fun uploadImageToCloudinary(uri: Uri, onResult: (String?, String?) -> Unit) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    val publicId = resultData["public_id"] as? String
                    onResult(url, publicId)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                    onResult(null, null)
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
    private fun saveChanges() {
        val title = binding.edtTitle.text.toString()
        val year = binding.edtYear.text.toString()
        val director = binding.edtDirector.text.toString()
        val synopsis = binding.edtSynopsis.text.toString()
        val trailerUrl = binding.edtTrailerUrl.text.toString()

        // Nếu có ảnh mới được chọn => upload lên Cloudinary
        if (imageUri != null) {
            uploadImageToCloudinary(imageUri!!) { imageUrl, _ ->
                if (imageUrl != null) {
                    saveFilmDataToFirebase(title, year, director, synopsis, trailerUrl, imageUrl)
                } else {
                    Toast.makeText(this, "Tải ảnh lên thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Không có ảnh mới => lấy lại URL cũ từ Firebase rồi lưu lại thông tin khác
            filmRef.child("posterURL").get().addOnSuccessListener { snapshot ->
                val existingPosterUrl = snapshot.getValue(String::class.java) ?: ""
                saveFilmDataToFirebase(title, year, director, synopsis, trailerUrl, existingPosterUrl)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Không thể lấy poster hiện tại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveFilmDataToFirebase(
        title: String,
        year: String,
        director: String,
        synopsis: String,
        trailerUrl: String,
        posterUrl: String
    ) {
        val updatedFilm = mapOf(
            "title" to title,
            "releaseYear" to year,
            "director" to director,
            "synopsis" to synopsis,
            "trailerURL" to trailerUrl,
            "posterURL" to posterUrl,
            "genre" to selectedGenres,
            "actor" to selectedActors
        )

        filmRef.updateChildren(updatedFilm).addOnSuccessListener {
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
        }
    }

}