package com.V2Skydivejump.app

import android.app.Application
import android.content.Context
import com.V2Skydivejump.app.data.SupabaseManager

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        
        // Initialize Platform Utils
        com.V2Skydivejump.app.utils.initPdfManager(this)
        com.V2Skydivejump.app.utils.initFileReader(this)
        
        try {
            SupabaseManager.init(
                url = BuildConfig.SUPABASE_URL,
                key = BuildConfig.SUPABASE_KEY
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Optionally log to a file or standard log
            android.util.Log.e("AndroidApp", "Supabase init failed: ${e.message}")
        }
    }
}

lateinit var appContext: Context
