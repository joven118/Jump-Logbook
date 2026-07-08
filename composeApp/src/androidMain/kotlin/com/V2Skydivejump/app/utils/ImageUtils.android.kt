package com.V2Skydivejump.app.utils

import android.net.Uri
import com.V2Skydivejump.app.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object ImageUtils {
    /**
     * Reads bytes from a local URI using Android's ContentResolver
     */
    actual suspend fun readBytesFromUri(uri: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val contentUri = Uri.parse(uri)
            appContext.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            println("DIAGNOSTIC ERROR: ImageUtils failed to read bytes: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
