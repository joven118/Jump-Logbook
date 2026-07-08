package com.V2Skydivejump.app.utils

import java.io.File

class DesktopFileReader : FileReader {
    override suspend fun readBytes(uri: String): ByteArray? {
        return try {
            val path = if (uri.startsWith("file://")) uri.substring(7) else uri
            File(path).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

actual fun getFileReader(): FileReader = DesktopFileReader()
