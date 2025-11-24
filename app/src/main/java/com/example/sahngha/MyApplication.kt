package com.example.sahngha
import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Khởi tạo Cloudinary
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "dbrussgnn" // <-- DÁN CLOUD NAME BẠN VỪA COPY VÀO ĐÂY
        MediaManager.init(this, config)
    }
}
