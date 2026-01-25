package com.example.danhgiaphim

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "dsgx4conh"
        config["api_key"] = "764752389264726"
        config["api_secret"] = "3vugQqQEwlyAC1SNutohAkZGiU0"
        MediaManager.init(this, config)
    }
}
