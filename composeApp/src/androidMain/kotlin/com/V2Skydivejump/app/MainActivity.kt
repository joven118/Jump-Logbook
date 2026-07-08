package com.V2Skydivejump.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.V2Skydivejump.app.utils.ExternalShareManager
import com.V2Skydivejump.app.utils.initPdfManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ExternalShareManager.initialize(applicationContext)
        initPdfManager(applicationContext)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
