package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.data.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

object MediaUploadService {
    private const val MAX_IMAGE_BYTES = 10 * 1024 * 1024
    private const val MAX_VIDEO_BYTES = 75 * 1024 * 1024

    data class UploadedMedia(
        val publicUrl: String,
        val storagePath: String,
        val bucketName: String
    )

    suspend fun uploadLocalUri(
        localUri: String,
        bucketName: String,
        entityType: String,
        entityId: String,
        mediaKind: String = "image",
        forcePrivatePath: Boolean = false
    ): UploadedMedia {
        if (localUri.isBlank()) {
            throw IllegalArgumentException("No media file selected.")
        }

        if (localUri.startsWith("http://") || localUri.startsWith("https://")) {
            return UploadedMedia(
                publicUrl = localUri,
                storagePath = localUri,
                bucketName = bucketName
            )
        }

        val user = SupabaseManager.client.auth.currentUserOrNull()
            ?: throw IllegalStateException("User must be signed in to upload media.")

        val bytes = getFileReader().readBytes(localUri)
            ?: throw IllegalArgumentException("Could not read the selected media file.")

        if (bytes.isEmpty()) {
            throw IllegalArgumentException("Selected media file is empty.")
        }

        val maxBytes = if (mediaKind == "video") MAX_VIDEO_BYTES else MAX_IMAGE_BYTES
        if (bytes.size > maxBytes) {
            val maxMb = maxBytes / (1024 * 1024)
            throw IllegalArgumentException("Selected $mediaKind is too large. Maximum size is ${maxMb}MB.")
        }

        val extension = extensionFromUri(localUri, mediaKind)
        val path = buildStoragePath(
            userId = user.id,
            entityType = entityType,
            entityId = entityId,
            extension = extension,
            forcePrivatePath = forcePrivatePath
        )

        val bucket = SupabaseManager.client.storage.from(bucketName)
        bucket.upload(path, bytes) {
            upsert = false
        }

        return UploadedMedia(
            publicUrl = bucket.publicUrl(path),
            storagePath = path,
            bucketName = bucketName
        )
    }

    private fun buildStoragePath(
        userId: String,
        entityType: String,
        entityId: String,
        extension: String,
        forcePrivatePath: Boolean
    ): String {
        val safeEntityType = sanitizePathSegment(entityType)
        val safeEntityId = sanitizePathSegment(entityId)
        val timestamp = TimeUtils.nowEpochMillis()
        val prefix = if (forcePrivatePath) "private" else "public"
        return "$prefix/$userId/$safeEntityType/$safeEntityId/$timestamp.$extension"
    }

    private fun extensionFromUri(uri: String, mediaKind: String): String {
        val trimmed = uri.substringBefore('?').substringBefore('#')
        val extension = trimmed.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
            .filter { it.isLetterOrDigit() }

        val allowedImages = setOf("jpg", "jpeg", "png", "webp", "heic")
        val allowedVideos = setOf("mp4", "mov", "m4v", "webm")
        val allowed = if (mediaKind == "video") allowedVideos else allowedImages

        if (extension in allowed) return if (extension == "jpeg") "jpg" else extension
        return if (mediaKind == "video") "mp4" else "jpg"
    }

    private fun sanitizePathSegment(value: String): String {
        return value
            .trim()
            .ifBlank { "media" }
            .map { char -> if (char.isLetterOrDigit() || char == '-' || char == '_') char else '_' }
            .joinToString("")
            .take(80)
    }
}
