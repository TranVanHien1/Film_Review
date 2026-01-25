package com.example.danhgiaphim.Admin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.danhgiaphim.Film.AddActorActivity
import com.example.danhgiaphim.AdminActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.adapter.AdminActorAdapter
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.databinding.ActivityActorListBinding
import com.google.firebase.database.*

class ActorListActivity : AppCompatActivity() {

    private lateinit var listActorBinding: ActivityActorListBinding
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.reference.child("Actors")

    private val actorList = ArrayList<Actors>()
    private lateinit var actorAdapter: AdminActorAdapter

    private var selectedImageUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var currentDialogImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listActorBinding = ActivityActorListBinding.inflate(layoutInflater)
        setContentView(listActorBinding.root)

        // Init adapter
        actorAdapter = AdminActorAdapter(this, actorList)
        listActorBinding.recyclerViewActor.layoutManager = LinearLayoutManager(this)
        listActorBinding.recyclerViewActor.adapter = actorAdapter

        // Init search
        listActorBinding.txtSearchActorInList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    fetchActor()
                } else {
                    searchActors(query)
                }
            }
        })

        // Init buttons
        listActorBinding.btnBackToAdminFromActorlist.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }

        listActorBinding.btnAddActor.setOnClickListener {
            startActivity(Intent(this, AddActorActivity::class.java))
        }

        // Image picker launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                selectedImageUri = it
                currentDialogImageView?.setImageURI(it)
            }
        }

        fetchActor()
    }

    private fun fetchActor() {
        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                actorList.clear()
                for (userSnapshot in snapshot.children) {
                    val actor = userSnapshot.getValue(Actors::class.java)
                    if (actor != null) {
                        actorList.add(actor)
                    }
                }
                actorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ActorListActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchActors(query: String) {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredList = mutableListOf<Actors>()
                for (child in snapshot.children) {
                    val actor = child.getValue(Actors::class.java)
                    if (actor != null && actor.actorName?.contains(query, ignoreCase = true) == true) {
                        filteredList.add(actor)
                    }
                }
                actorAdapter.actorlist = ArrayList(filteredList)
                actorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ActorListActivity, "Lỗi tìm kiếm diễn viên", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showEditActorDialog(actor: Actors) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_actor, null)
        builder.setView(view)

        val imgAvatar = view.findViewById<ImageView>(R.id.imgActor)
        val edtName = view.findViewById<EditText>(R.id.edtActorName)
        edtName.setText(actor.actorName)

        Glide.with(this).load(actor.actorAvatarURL).into(imgAvatar)

        selectedImageUri = null
        currentDialogImageView = imgAvatar

        imgAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        builder.setPositiveButton("Cập nhật") { dialog, _ ->
            val newName = edtName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Tên không được trống", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val actorID = actor.actorID ?: return@setPositiveButton
            val oldAvatarURL = actor.actorAvatarURL ?: ""

            if (selectedImageUri != null) {
                uploadNewAvatarToCloudinary(selectedImageUri!!) { newUrl ->
                    updateActorInFirebase(actorID, newName, newUrl)
                }
            } else {
                updateActorInFirebase(actorID, newName, oldAvatarURL)
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Xóa") { dialog, _ ->
            val actorID = actor.actorID ?: return@setNegativeButton
            deleteActorFromFirebase(actorID)
            dialog.dismiss()
        }

        builder.setNeutralButton("Hủy") { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    private fun uploadNewAvatarToCloudinary(uri: Uri, onSuccess: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    onSuccess(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@ActorListActivity, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show()
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun updateActorInFirebase(actorID: String, name: String, avatarUrl: String) {
        val updates = mapOf(
            "actorName" to name,
            "actorAvatarURL" to avatarUrl
        )
        myRef.child(actorID).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteActorFromFirebase(actorID: String) {
        myRef.child(actorID).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show()
            }
    }
}
