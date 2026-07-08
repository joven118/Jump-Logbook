package com.V2Skydivejump.app.data

import io.github.jan.supabase.postgrest.postgrest
import com.V2Skydivejump.app.database.entities.UserGearEntity
import io.github.jan.supabase.auth.auth

class GearRepository {

    private val auth = SupabaseManager.client.auth
    private val db = SupabaseManager.client.postgrest

    suspend fun getUserGear(): List<UserGearEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return db.from("user_gear").select {
            filter {
                eq("user_id", user.id)
            }
        }.decodeList<UserGearEntity>()
    }

    suspend fun saveGear(gear: UserGearEntity) {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        db.from("user_gear").upsert(gear.copy(userId = user.id)) {
            filter {
                eq("gear_id", gear.gearId)
                eq("user_id", user.id)
            }
        }
    }

    suspend fun deleteGear(gearId: String) {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        db.from("user_gear").delete {
            filter {
                eq("gear_id", gearId)
                eq("user_id", user.id)
            }
        }
    }
}
