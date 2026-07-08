package com.V2Skydivejump.app.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidFileReader(private val context: Context) : FileReader {
    override suspend fun readBytes(uri: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val contentUri = Uri.parse(uri)
            context.contentResolver.openInputStream(contentUri)?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private var internalFileReader: FileReader? = null

fun initFileReader(context: Context) {
    internalFileReader = AndroidFileReader(context)
}

actual fun getFileReader(): FileReader {
    return internalFileReader ?: throw IllegalStateException("FileReader not initialized")
}
