package com.example.danhgiaphim.User

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.danhgiaphim.HomeActivity
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.databinding.ActivityProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.*

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var imageUri: Uri? = null
    private var dobTimestamp: Long = 0
    private lateinit var userRef: DatabaseReference
    private val uid = UserSession.uid

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val genderList = listOf("Male", "Female")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnGender.adapter = genderAdapter


        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.imgAvatar.setImageURI(it)
            }
        }

        binding.imgAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnBackToUser.setOnClickListener(){
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }



        binding.edtDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val picker = DatePickerDialog(this, { _, y, m, d ->
                binding.edtDateOfBirth.setText("$d/${m + 1}/$y")
                val selectedDate = Calendar.getInstance()
                selectedDate.set(y, m, d)
                dobTimestamp = selectedDate.timeInMillis
            }, year, month, day)
            picker.show()
        }

        binding.btnSave.setOnClickListener {
            uploadData()
        }

        if (uid != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
            loadUserData()
        } else {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                val gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                val dob = snapshot.child("dateOfBirth").getValue(Long::class.java) ?: 0L
                val avatarUrl = snapshot.child("avatarURL").getValue(String::class.java) ?: ""

                binding.edtUsername.setText(username)
                binding.edtEmail.setText(email)
                val genderIndex = if (gender.lowercase() == "female") 1 else 0
                binding.spnGender.setSelection(genderIndex)

                if (dob > 0) {
                    dobTimestamp = dob
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = dob
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = calendar.get(Calendar.MONTH) + 1
                    val year = calendar.get(Calendar.YEAR)
                    binding.edtDateOfBirth.setText("$day/$month/$year")
                }

                if (avatarUrl.isNotEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(binding.imgAvatar)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun uploadData() {
        val username = binding.edtUsername.text.toString()
        val email = binding.edtEmail.text.toString()
        val gender = binding.spnGender.selectedItem.toString()

        if (uid == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            uploadImageToCloudinary(imageUri!!) { avatarUrl, avatarID ->
                if (avatarUrl != null && avatarID != null) {
                    saveUserInfo(username, email, gender, dobTimestamp, avatarUrl, avatarID)
                    finish()
                } else {
                    Toast.makeText(this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            saveUserInfo(username, email, gender, dobTimestamp, "", "")
            finish()
        }
    }


    private fun saveUserInfo(
        username: String,
        email: String,
        gender: String,
        dateOfBirth: Long,
        avatarUrl: String,
        avatarID: String
    ) {
        val updateMap = mutableMapOf<String, Any>(
            "username" to username,
            "email" to email,
            "gender" to gender,
            "dateOfBirth" to dateOfBirth,
        )
        if (avatarUrl.isNotEmpty()) {
            updateMap["avatarURL"] = avatarUrl
        }
        if (avatarID.isNotEmpty()) {
            updateMap["avatarID"] = avatarID
        }


        userRef.updateChildren(updateMap).addOnSuccessListener {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
        }
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




}
