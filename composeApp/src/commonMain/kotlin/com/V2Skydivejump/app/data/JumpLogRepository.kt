package com.V2Skydivejump.app.data

import io.github.jan.supabase.postgrest.postgrest
import com.V2Skydivejump.app.database.entities.JumpLogEntity
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class JumpLogRepository {

    private val auth = SupabaseManager.client.auth
    private val db = SupabaseManager.client.postgrest

    suspend fun getJumps(): List<JumpLogEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return try {
            db.from("jump_logs").select {
                filter {
                    eq("user_id", user.id)
                }
            }.decodeList<JumpLogEntity>()
        } catch (e: Exception) {
            println("JumpLogRepository ERROR: ${e.message}")
            emptyList()
        }
    }

    suspend fun insertJumpLog(jump: JumpLogEntity) {
        val currentUser = auth.currentUserOrNull() ?: return
        // If current user is DZO, they might be inserting for another user
        val finalJump = if (jump.userId.isBlank()) {
            jump.copy(userId = currentUser.id)
        } else {
            jump // Trust the provided userId (Aviation-Grade DZO Push)
        }
        
        try {
            db.from("jump_logs").insert(finalJump)
        } catch (e: Exception) {
            println("JumpLogRepository Error: Failed to insert jump: ${e.message}")
        }
    }

    suspend fun updateJumpLog(jump: JumpLogEntity) {
        val user = auth.currentUserOrNull() ?: return
        db.from("jump_logs").update(jump) {
            filter {
                eq("jump_id", jump.jumpId)
                eq("user_id", user.id)
            }
        }
    }

    suspend fun deleteJumpLog(jumpId: Long) {
        val user = auth.currentUserOrNull() ?: return
        db.from("jump_logs").delete {
            filter {
                eq("jump_id", jumpId)
                eq("user_id", user.id)
            }
        }
    }
}
