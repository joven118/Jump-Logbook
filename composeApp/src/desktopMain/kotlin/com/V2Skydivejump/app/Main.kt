package com.V2Skydivejump.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.V2Skydivejump.app.data.SupabaseManager
import java.util.Properties
import java.io.File

fun main() = application {
    val localProperties = Properties()
    val localPropertiesFile = File("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }

    SupabaseManager.init(
        url = localProperties.getProperty("SUPABASE_URL") ?: "",
        key = localProperties.getProperty("SUPABASE_KEY") ?: ""
    )

    Window(onCloseRequest = ::exitApplication, title = "Jump Logbook") {
        App()
    }
}
