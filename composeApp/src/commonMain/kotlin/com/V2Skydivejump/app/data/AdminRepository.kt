package com.V2Skydivejump.app.data

import io.github.jan.supabase.postgrest.postgrest
import com.V2Skydivejump.app.database.entities.*
import io.github.jan.supabase.auth.auth

class AdminRepository {

    private val db = SupabaseManager.client.postgrest

    suspend fun getUnverifiedUsers(): List<UserEntity> {
        return db.from("users").select {
            filter {
                eq("is_verified", false)
            }
        }.decodeList<UserEntity>()
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return db.from("users").select().decodeList<UserEntity>()
    }

    suspend fun getPendingBadges(): List<UserBadgeEntity> {
        return db.from("user_badges").select {
            filter {
                eq("verification_status", BadgeVerificationStatus.PENDING)
            }
        }.decodeList<UserBadgeEntity>()
    }

    suspend fun verifyUser(userId: String) {
        db.from("users").update({
            set("is_verified", true)
        }) {
            filter {
                eq("user_id", userId)
            }
        }
    }

    suspend fun verifyBadge(badgeId: Long) {
        db.from("user_badges").update({
            set("verification_status", BadgeVerificationStatus.VERIFIED)
        }) {
            filter {
                eq("id", badgeId)
            }
        }
    }

    suspend fun promoteToAdmin(email: String) {
        db.from("users").update({
            set("role", "ADMIN")
        }) {
            filter {
                eq("email", email)
            }
        }
    }

    suspend fun getSystemMetrics(): Map<String, Int> {
        return try {
            val userCount = db.from("users").select().decodeList<UserEntity>().size
            val jumpCount = db.from("jump_logs").select().decodeList<JumpLogEntity>().size
            // Use count if possible, but decodeList is safer for generic queries here
            val dzCount = db.from("users").select {
                filter {
                    eq("role", "DZ_OPERATOR")
                }
            }.decodeList<UserEntity>().size
            
            mapOf(
                "Total Users" to userCount,
                "Total Jumps" to jumpCount,
                "Registered DZs" to dzCount
            )
        } catch (e: Exception) {
            println("AdminRepository Error: ${e.message}")
            emptyMap()
        }
    }
}
