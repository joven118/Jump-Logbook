package com.V2Skydivejump.app.data

import io.github.jan.supabase.postgrest.postgrest
import com.V2Skydivejump.app.database.entities.UserRatingEntity
import com.V2Skydivejump.app.database.entities.UserFederationEntity
import io.github.jan.supabase.auth.auth

class ProfessionalRepository {

    private val auth = SupabaseManager.client.auth
    private val db = SupabaseManager.client.postgrest

    suspend fun getUserRatings(): List<UserRatingEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return try {
            db.from("user_ratings").select {
                filter {
                    eq("user_id", user.id)
                }
            }.decodeList<UserRatingEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveRating(rating: UserRatingEntity) {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        db.from("user_ratings").upsert(rating.copy(userId = user.id))
    }

    suspend fun deleteRating(ratingId: Long) {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        db.from("user_ratings").delete {
            filter {
                eq("id", ratingId)
                eq("user_id", user.id)
            }
        }
    }

    suspend fun getUserFederations(): List<UserFederationEntity> {
        val user = auth.currentUserOrNull() ?: return emptyList()
        return try {
            db.from("user_federations").select {
                filter {
                    eq("user_id", user.id)
                }
            }.decodeList<UserFederationEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveFederation(federation: UserFederationEntity) {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        db.from("user_federations").upsert(federation.copy(userId = user.id))
    }

    suspend fun deleteFederation(federationId: Long) {
        val user = auth.currentUserOrNull() ?: throw IllegalStateException("User not authenticated")
        db.from("user_federations").delete {
            filter {
                eq("id", federationId)
                eq("user_id", user.id)
            }
        }
    }

    suspend fun saveIncidentReport(report: com.V2Skydivejump.app.database.entities.IncidentReportEntity) {
        db.from("incident_reports").upsert(report)
    }

    suspend fun syncStudentSkill(skill: com.V2Skydivejump.app.database.entities.StudentSkillEntity) {
        try {
            db.from("student_skills").upsert(skill)
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun updateFlightSchedule(schedule: com.V2Skydivejump.app.database.entities.FlightScheduleEntity) {
        db.from("flight_schedules").upsert(schedule)
    }

    suspend fun getDzFacilities(dzId: String): List<com.V2Skydivejump.app.database.entities.DzFacilityEntity> {
        return try {
            db.from("dz_facilities").select {
                filter { eq("dz_id", dzId) }
            }.decodeList<com.V2Skydivejump.app.database.entities.DzFacilityEntity>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getDzInventory(dzId: String): List<com.V2Skydivejump.app.database.entities.DzInventoryEntity> {
        return try {
            db.from("dz_inventory").select {
                filter { eq("dz_id", dzId) }
            }.decodeList<com.V2Skydivejump.app.database.entities.DzInventoryEntity>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getDzWaivers(dzId: String): List<com.V2Skydivejump.app.database.entities.DzWaiverEntity> {
        return try {
            db.from("dz_waivers").select {
                filter { eq("dz_id", dzId); eq("is_active", true) }
            }.decodeList<com.V2Skydivejump.app.database.entities.DzWaiverEntity>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getFlightSchedules(dzId: String): List<com.V2Skydivejump.app.database.entities.FlightScheduleEntity> {
        return try {
            db.from("flight_schedules").select {
                filter { eq("dz_id", dzId) }
            }.decodeList<com.V2Skydivejump.app.database.entities.FlightScheduleEntity>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getDzStaffMemberships(dzId: String): List<com.V2Skydivejump.app.database.entities.DzStaffMembershipEntity> {
        return try {
            db.from("dz_staff_memberships").select {
                filter { eq("dz_id", dzId) }
            }.decodeList<com.V2Skydivejump.app.database.entities.DzStaffMembershipEntity>()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getIncidentReports(dzId: String): List<com.V2Skydivejump.app.database.entities.IncidentReportEntity> {
        return try {
            db.from("incident_reports").select {
                filter { eq("dz_id", dzId) }
            }.decodeList<com.V2Skydivejump.app.database.entities.IncidentReportEntity>()
        } catch (e: Exception) { emptyList() }
    }
}
