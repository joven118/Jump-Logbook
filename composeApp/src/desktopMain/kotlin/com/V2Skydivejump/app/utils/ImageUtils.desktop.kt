package com.V2Skydivejump.app.utils

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object ImageUtils {
    actual suspend fun readBytesFromUri(uri: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // On desktop, the URI is usually a file path
            val path = if (uri.startsWith("file://")) uri.substring(7) else uri
            File(path).readBytes()
        } catch (e: Exception) {
            println("DIAGNOSTIC ERROR: ImageUtils Desktop failed: ${e.message}")
            null
        }
    }
}
