package com.V2Skydivejump.app.data

import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.V2Skydivejump.app.database.entities.JumpMediaEntity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

class JumpRepository {

    private val client = SupabaseManager.client
    private val auth = client.auth
    private val db = client.postgrest
    private val storage = client.storage

    /**
     * Inserts a jump into the 'jumps' table.
     * Ensures the userId is set to the currently authenticated user for RLS.
     */
    suspend fun insertJump(jump: JumpLogEntity) {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        println("DIAGNOSTIC: insertJump - START for ${user.id}")
        try {
            db.from("jump_logs").insert(jump.copy(userId = user.id))
            println("DIAGNOSTIC: insertJump - SUCCESS")
        } catch (e: Exception) {
            println("DIAGNOSTIC: insertJump - ERROR: ${e.message}")
            throw e
        }
    }

    /**
     * Retrieves all jumps for the current user from the 'jumps' table.
     */
    suspend fun getJumps(): List<JumpLogEntity> {
        val user = auth.currentUserOrNull() ?: run {
            println("DIAGNOSTIC: getJumps - FAILED (No Session)")
            return emptyList()
        }
        println("DIAGNOSTIC: getJumps - START for ${user.id}")
        return try {
            val jumps = db.from("jump_logs") // Changed from "jumps" to match our Part 1 schema "jump_logs"
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<JumpLogEntity>()
            println("DIAGNOSTIC: getJumps - SUCCESS: Count=${jumps.size}")
            jumps
        } catch (e: Exception) {
            println("DIAGNOSTIC: getJumps - ERROR: ${e.message}")
            emptyList()
        }
    }

    /**
     * Uploads media to Supabase Storage and records metadata in 'jump_media' table.
     * @param jumpId The ID of the jump this media belongs to.
     * @param fileName The name of the file (e.g., "jump_video_123.mp4").
     * @param fileBytes The raw content of the file.
     * @param mediaType The type of media (IMAGE, VIDEO, etc.).
     * @return The public URL of the uploaded media.
     */
    suspend fun uploadJumpMedia(
        jumpId: Long,
        fileName: String,
        fileBytes: ByteArray,
        mediaType: String
    ): String {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        
        // 1. Define storage path: jumps/[uid]/[jumpId]/[filename]
        val path = "${user.id}/$jumpId/$fileName"
        val bucket = storage.from("jump-media")
        
        // 2. Upload to Supabase Storage
        bucket.upload(path, fileBytes) {
            upsert = true
        }
        
        // 3. Get Public URL
        val publicUrl = bucket.publicUrl(path)
        
        // 4. Insert record into 'jump_media' table
        val mediaRecord = JumpMediaEntity(
            jumpId = jumpId,
            userId = user.id,
            mediaUrl = publicUrl,
            mediaType = mediaType
        )
        
        db.from("jump_media").insert(mediaRecord)
        
        return publicUrl
    }
}
