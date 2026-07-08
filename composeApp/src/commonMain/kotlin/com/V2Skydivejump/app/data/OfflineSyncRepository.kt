package com.V2Skydivejump.app.data

import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.database.entities.*
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SyncOperationType {
    const val USER_PROFILE_UPSERT = "USER_PROFILE_UPSERT"
    const val PRIVACY_UPSERT = "PRIVACY_UPSERT"
    const val DZ_STAFF_UPSERT = "DZ_STAFF_UPSERT"
    const val DZ_FACILITY_UPSERT = "DZ_FACILITY_UPSERT"
    const val DZ_FACILITY_DELETE = "DZ_FACILITY_DELETE"
    const val DZ_INVENTORY_UPSERT = "DZ_INVENTORY_UPSERT"
    const val DZ_INVENTORY_DELETE = "DZ_INVENTORY_DELETE"
    const val DZ_RATING_UPSERT = "DZ_RATING_UPSERT"
    const val DZ_WAIVER_UPSERT = "DZ_WAIVER_UPSERT"
    const val INCIDENT_REPORT_UPSERT = "INCIDENT_REPORT_UPSERT"
    const val FLIGHT_SCHEDULE_UPSERT = "FLIGHT_SCHEDULE_UPSERT"
    const val USER_GEAR_UPSERT = "USER_GEAR_UPSERT"
    const val USER_GEAR_DELETE = "USER_GEAR_DELETE"
    const val USER_RATING_UPSERT = "USER_RATING_UPSERT"
    const val USER_RATING_DELETE = "USER_RATING_DELETE"
    const val USER_FEDERATION_UPSERT = "USER_FEDERATION_UPSERT"
    const val USER_FEDERATION_DELETE = "USER_FEDERATION_DELETE"
}

@Serializable
data class PendingSyncOperation(
    val id: String,
    val type: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val lastError: String? = null
)

class OfflineSyncRepository(
    private val userRepository: UserRepository,
    private val gearRepository: GearRepository,
    private val professionalRepository: ProfessionalRepository
) {
    private val settings = Settings()
    private val queueKey = "offline_sync_queue_v1"
    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun pendingCount(): Int = readQueue().size

    inline fun <reified T> enqueue(type: String, payload: T) {
        enqueueRaw(type, json.encodeToString(payload))
    }

    fun enqueueRaw(type: String, payload: String) {
        val queue = readQueue().toMutableList()
        val dedupeKey = dedupeKey(type, payload)
        val compacted = queue.filterNot { dedupeKey(it.type, it.payload) == dedupeKey }.toMutableList()
        compacted.add(
            PendingSyncOperation(
                id = "${TimeUtils.nowEpochMillis()}_${compacted.size}",
                type = type,
                payload = payload,
                createdAt = TimeUtils.nowEpochMillis()
            )
        )
        writeQueue(compacted)
    }

    suspend fun flushPending() {
        val queue = readQueue().sortedBy { it.createdAt }.toMutableList()
        if (queue.isEmpty()) return

        val remaining = mutableListOf<PendingSyncOperation>()
        for (operation in queue) {
            try {
                process(operation)
            } catch (e: Exception) {
                remaining.add(
                    operation.copy(
                        retryCount = operation.retryCount + 1,
                        lastError = e.message ?: e::class.simpleName
                    )
                )
            }
        }
        writeQueue(remaining)
    }

    private suspend fun process(operation: PendingSyncOperation) {
        when (operation.type) {
            SyncOperationType.USER_PROFILE_UPSERT -> userRepository.updateProfile(json.decodeFromString<UserEntity>(operation.payload))
            SyncOperationType.PRIVACY_UPSERT -> userRepository.updatePrivacySettings(json.decodeFromString<UserPrivacySettingsEntity>(operation.payload))
            SyncOperationType.DZ_STAFF_UPSERT -> userRepository.addDzStaffMembership(json.decodeFromString<DzStaffMembershipEntity>(operation.payload))
            SyncOperationType.DZ_FACILITY_UPSERT -> userRepository.addDzFacility(json.decodeFromString<DzFacilityEntity>(operation.payload))
            SyncOperationType.DZ_FACILITY_DELETE -> userRepository.deleteDzFacility(operation.payload.toLong())
            SyncOperationType.DZ_INVENTORY_UPSERT -> userRepository.addDzInventory(json.decodeFromString<DzInventoryEntity>(operation.payload))
            SyncOperationType.DZ_INVENTORY_DELETE -> userRepository.deleteDzInventory(operation.payload.toLong())
            SyncOperationType.DZ_RATING_UPSERT -> userRepository.addDzRating(json.decodeFromString<DzRatingEntity>(operation.payload))
            SyncOperationType.DZ_WAIVER_UPSERT -> userRepository.upsertWaiver(json.decodeFromString<DzWaiverEntity>(operation.payload))
            SyncOperationType.INCIDENT_REPORT_UPSERT -> professionalRepository.saveIncidentReport(json.decodeFromString<IncidentReportEntity>(operation.payload))
            SyncOperationType.FLIGHT_SCHEDULE_UPSERT -> professionalRepository.updateFlightSchedule(json.decodeFromString<FlightScheduleEntity>(operation.payload))
            SyncOperationType.USER_GEAR_UPSERT -> gearRepository.saveGear(json.decodeFromString<UserGearEntity>(operation.payload))
            SyncOperationType.USER_GEAR_DELETE -> gearRepository.deleteGear(operation.payload)
            SyncOperationType.USER_RATING_UPSERT -> professionalRepository.saveRating(json.decodeFromString<UserRatingEntity>(operation.payload))
            SyncOperationType.USER_RATING_DELETE -> professionalRepository.deleteRating(operation.payload.toLong())
            SyncOperationType.USER_FEDERATION_UPSERT -> professionalRepository.saveFederation(json.decodeFromString<UserFederationEntity>(operation.payload))
            SyncOperationType.USER_FEDERATION_DELETE -> professionalRepository.deleteFederation(operation.payload.toLong())
            else -> error("Unknown offline sync operation: ${operation.type}")
        }
    }

    private fun readQueue(): List<PendingSyncOperation> {
        val stored = settings.getString(queueKey, "[]")
        return try {
            json.decodeFromString<List<PendingSyncOperation>>(stored)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeQueue(queue: List<PendingSyncOperation>) {
        settings.putString(queueKey, json.encodeToString(queue))
    }

    private fun dedupeKey(type: String, payload: String): String {
        return when (type) {
            SyncOperationType.USER_PROFILE_UPSERT -> "$type:${json.decodeFromString<UserEntity>(payload).userId}"
            SyncOperationType.PRIVACY_UPSERT -> "$type:${json.decodeFromString<UserPrivacySettingsEntity>(payload).userId}"
            SyncOperationType.DZ_STAFF_UPSERT -> {
                val value = json.decodeFromString<DzStaffMembershipEntity>(payload)
                "$type:${value.dzId}:${value.userId}"
            }
            SyncOperationType.DZ_FACILITY_UPSERT -> "$type:${json.decodeFromString<DzFacilityEntity>(payload).id}"
            SyncOperationType.DZ_FACILITY_DELETE -> "$type:$payload"
            SyncOperationType.DZ_INVENTORY_UPSERT -> "$type:${json.decodeFromString<DzInventoryEntity>(payload).id}"
            SyncOperationType.DZ_INVENTORY_DELETE -> "$type:$payload"
            SyncOperationType.DZ_RATING_UPSERT -> "$type:${json.decodeFromString<DzRatingEntity>(payload).id}"
            SyncOperationType.DZ_WAIVER_UPSERT -> "$type:${json.decodeFromString<DzWaiverEntity>(payload).id}"
            SyncOperationType.INCIDENT_REPORT_UPSERT -> "$type:${json.decodeFromString<IncidentReportEntity>(payload).id}"
            SyncOperationType.FLIGHT_SCHEDULE_UPSERT -> "$type:${json.decodeFromString<FlightScheduleEntity>(payload).scheduleId}"
            SyncOperationType.USER_GEAR_UPSERT -> "$type:${json.decodeFromString<UserGearEntity>(payload).gearId}"
            SyncOperationType.USER_GEAR_DELETE -> "$type:$payload"
            SyncOperationType.USER_RATING_UPSERT -> "$type:${json.decodeFromString<UserRatingEntity>(payload).id}"
            SyncOperationType.USER_RATING_DELETE -> "$type:$payload"
            SyncOperationType.USER_FEDERATION_UPSERT -> "$type:${json.decodeFromString<UserFederationEntity>(payload).id}"
            SyncOperationType.USER_FEDERATION_DELETE -> "$type:$payload"
            else -> "$type:$payload"
        }
    }
}
