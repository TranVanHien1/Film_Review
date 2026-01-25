package com.example.danhgiaphim.Film

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.danhgiaphim.Admin.ActorListActivity
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.databinding.ActivityAddActorBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.HashMap
import java.util.UUID

class AddActorActivity : AppCompatActivity() {

    private lateinit var addActorBinding: ActivityAddActorBinding
    private var posterUri: Uri? = null
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myReference : DatabaseReference = database.reference.child("Actors")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addActorBinding = ActivityAddActorBinding.inflate(layoutInflater)
        val view = addActorBinding.root
        setContentView(view)

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                posterUri = it
                addActorBinding.imgAddActorPoster.setImageURI(it)
            }
        }

        addActorBinding.imgAddActorPoster.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        addActorBinding.btnBackToActorListFromAddActor.setOnClickListener(){
            val intent = Intent(this, ActorListActivity::class.java)
            startActivity(intent)
        }

        addActorBinding.btnAddActor.setOnClickListener {
            if (posterUri != null) {
                uploadPosterToCloudinary1(posterUri!!)
            } else {
                Toast.makeText(this, "Vui lòng chọn ảnh poster", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPosterToCloudinary1(uri: Uri) {
        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "dsgx4conh"
        config["api_key"] = "764752389264726"
        config["api_secret"] = "3vugQqQEwlyAC1SNutohAkZGiU0"
        MediaManager.init(this, config)

        val compressedFile = compressImage(uri, this)
        if (compressedFile != null) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    saveActorToFirebase(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@AddActorActivity, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
        }
    }

    private fun saveActorToFirebase(posterUrl: String) {
        val actorId = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()

        val actor = Actors(
            actorID = actorId,
            actorName = addActorBinding.edtAddActorName.text.toString(),
            actorAvatarURL = posterUrl
        )

        FirebaseDatabase.getInstance()
            .getReference("Actors")
            .child(actorId)
            .setValue(actor)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm diễn viên thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Thêm diễn viên thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    fun compressImage(uri: Uri, context: Context): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Giảm kích thước
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)

        // Lưu lại file nén
        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()

        return file
    }

}