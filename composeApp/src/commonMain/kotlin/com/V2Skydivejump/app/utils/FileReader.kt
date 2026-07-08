package com.V2Skydivejump.app.utils

interface FileReader {
    suspend fun readBytes(uri: String): ByteArray?
}

expect fun getFileReader(): FileReader
