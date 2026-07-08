package com.V2Skydivejump.app.data

import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SyncRepository(
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val jumpLogRepository: JumpLogRepository,
    private val scope: CoroutineScope
) {
    private val settings = Settings()
    private val syncedJumpIdsKey = "synced_local_jump_ids"

    init {
        observeLocalChanges()
    }

    private fun observeLocalChanges() {
        // Observe Jumps for Social Sync
        scope.launch {
            database.jumpLogDao().getAllJumps().collectLatest { jumps ->
                jumps.forEach { jump ->
                    if (!isJumpMarkedSynced(jump.jumpId)) {
                        uploadJumpToSupabase(jump)
                    }
                }
            }
        }
    }

    suspend fun uploadJumpToSupabase(jump: JumpLogEntity) {
        try {
            jumpLogRepository.insertJumpLog(jump)
            markJumpSynced(jump.jumpId)
            println("Sync: Jump #${jump.jumpNumber} uploaded to Supabase.")
        } catch (e: Exception) {
            println("Sync: Failed to upload jump: ${e.message}")
        }
    }

    suspend fun syncProfile() {
        userRepository.fetchProfile()
    }

    private fun isJumpMarkedSynced(jumpId: Long): Boolean {
        return SyncMarkers.contains(settings.getString(syncedJumpIdsKey, ""), jumpId)
    }

    private fun markJumpSynced(jumpId: Long) {
        settings.putString(syncedJumpIdsKey, SyncMarkers.add(settings.getString(syncedJumpIdsKey, ""), jumpId))
    }
}
