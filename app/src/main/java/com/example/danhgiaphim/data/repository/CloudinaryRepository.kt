package com.example.danhgiaphim.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryRepository @Inject constructor() {
    suspend fun uploadImage(uri: Uri): CloudinaryUploadResult =
        suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        if (!continuation.isActive) return
                        val url = resultData["secure_url"] as? String
                        val publicId = resultData["public_id"] as? String
                        if (url.isNullOrBlank()) {
                            continuation.resumeWithException(IllegalStateException("Upload ảnh thất bại"))
                        } else {
                            continuation.resume(CloudinaryUploadResult(url, publicId.orEmpty()))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                IllegalStateException(error?.description ?: "Upload ảnh thất bại")
                            )
                        }
                    }

                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        }
}

data class CloudinaryUploadResult(
    val url: String,
    val publicId: String
)
