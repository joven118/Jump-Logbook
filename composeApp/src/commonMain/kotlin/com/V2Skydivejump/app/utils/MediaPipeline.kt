package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.TimeUtils

/**
 * Professional Media Upload Architecture
 */
object MediaPipeline {

    data class MediaResult(
        val fullResUrl: String,
        val thumbnailUrl: String
    )

    /**
     * Entry point for the upload pipeline
     */
    suspend fun processAndUpload(localUri: String, jumpId: Long): MediaResult {
        println("DIAGNOSTIC: MediaPipeline - Starting for $localUri")

        val entityId = if (jumpId > 0) "jump_$jumpId" else "pending_jump_${TimeUtils.nowEpochMillis()}"
        val uploaded = MediaUploadService.uploadLocalUri(
            localUri = localUri,
            bucketName = "jump-media",
            entityType = "jump",
            entityId = entityId,
            mediaKind = "image"
        )

        println("DIAGNOSTIC: MediaPipeline - SUCCESS: ${uploaded.publicUrl}")

        return MediaResult(
            fullResUrl = uploaded.publicUrl,
            thumbnailUrl = uploaded.publicUrl
        )
    }
}
