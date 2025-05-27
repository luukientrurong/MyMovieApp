package com.example.movieapp.Repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CloudinaryRepository @Inject constructor(
    @ApplicationContext private val context:Context
) {
    suspend fun uploadFileToCloudinary(uri: Uri): String = suspendCoroutine { continuation ->
        Log.e("UserImg","da vao cloud")

        Log.e("UserImg","sau init")
        MediaManager.get().upload(uri)
            .option("resource_type", "auto")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val uploadedUrl = resultData?.get("secure_url").toString()
                    continuation.resume(uploadedUrl)
                    Log.e("UserImg","da tai len cloud")
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume("")
                    Log.e("UserImg","loi tai cloud")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    Log.e("UserImg","progress")
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.e("UserImg","Reschedule")
                }
                override fun onStart(requestId: String?) {
                    Log.e("UserImg","Start")
                }
            }).dispatch()
    }
}