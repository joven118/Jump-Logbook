package com.V2Skydivejump.app.utils

import androidx.compose.runtime.Composable

expect object ImageUtils {
    /**
     * Reads bytes from a local URI (platform specific)
     */
    suspend fun readBytesFromUri(uri: String): ByteArray?
}
