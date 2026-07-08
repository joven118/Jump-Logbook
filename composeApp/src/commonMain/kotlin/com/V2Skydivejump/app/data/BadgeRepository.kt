package com.V2Skydivejump.app.data

import io.github.jan.supabase.postgrest.postgrest
import com.V2Skydivejump.app.database.entities.UserBadgeEntity
import com.V2Skydivejump.app.database.entities.BadgeEntity
import com.V2Skydivejump.app.database.entities.BadgeVerificationStatus
import io.github.jan.supabase.auth.auth
import com.V2Skydivejump.app.utils.MediaUploadService
import com.V2Skydivejump.app.TimeUtils

class BadgeRepository {

    private val auth = SupabaseManager.client.auth
    private val db = SupabaseManager.client.postgrest

    suspend fun getAllBadges(): List<BadgeEntity> {
        return db.from("badges").select().decodeList<BadgeEntity>()
    }

    suspend fun getUserBadges(): List<UserBadgeEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return db.from("user_badges").select {
            filter {
                eq("user_id", user.id)
            }
        }.decodeList<UserBadgeEntity>()
    }

    suspend fun claimBadge(badgeId: String, documentUri: String) {
        val user = auth.currentUserOrNull() ?: return
        val finalUrl = MediaUploadService.uploadLocalUri(
            localUri = documentUri,
            bucketName = "badge-proof-media",
            entityType = "badge_claim",
            entityId = badgeId,
            mediaKind = "image",
            forcePrivatePath = true
        ).publicUrl

        db.from("user_badges").insert(
            UserBadgeEntity(
                userId = user.id,
                badgeId = badgeId,
                dateEarned = TimeUtils.nowEpochMillis(),
                verificationStatus = BadgeVerificationStatus.PENDING,
                supportingDocumentUrl = finalUrl
            )
        )
    }
}
