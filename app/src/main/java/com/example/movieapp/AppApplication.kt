package com.example.movieapp

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppAplication: Application(){
    override fun onCreate() {
        super.onCreate()
        val config = hashMapOf(
            "cloud_name" to "dkqc83kv7",
            "api_key" to "867119739127693",
            "api_secret" to "o1_1U0-LkdzwAHDyY1acSh1o7EQ"
        )
        MediaManager.init(this, config)
    }
}
