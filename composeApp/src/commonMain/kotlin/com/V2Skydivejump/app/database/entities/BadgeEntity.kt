package com.V2Skydivejump.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

object BadgeCriteriaType {
    const val AUTO_JUMPS = "AUTO_JUMPS"
    const val AUTO_FREEFALL = "AUTO_FREEFALL"
    const val AUTO_DZS = "AUTO_DZS"
    const val AUTO_COUNTRIES = "AUTO_COUNTRIES"
    const val AUTO_CONTINENTS = "AUTO_CONTINENTS"
    const val AUTO_AIRCRAFTS = "AUTO_AIRCRAFTS"
    const val MANUAL = "MANUAL"
    const val VERIFIED = "VERIFIED"
}

object BadgeVerificationStatus {
    const val NOT_VERIFIED = "NOT_VERIFIED"
    const val PENDING = "PENDING"
    const val VERIFIED = "VERIFIED"
}

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val category: String, // "JUMP_MILESTONES", "WINGSUIT", "MILITARY", etc.
    val badgeName: String,
    val description: String,
    val criteriaType: String = BadgeCriteriaType.MANUAL,
    val criteriaValue: Int,
    val level: Int, // Priority within category (1, 2, 3...)
    val prestigeScore: Int // Global prestige for Featured Badge selection
)

@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val badgeId: String,
    val dateEarned: Long,
    val isNew: Boolean = true,
    val verificationStatus: String = BadgeVerificationStatus.VERIFIED,
    val supportingDocumentUrl: String? = null
)
