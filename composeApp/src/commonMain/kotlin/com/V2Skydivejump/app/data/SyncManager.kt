package com.V2Skydivejump.app.data

import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SyncManager(
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val jumpLogRepository: JumpLogRepository,
    private val gearRepository: GearRepository,
    private val professionalRepository: ProfessionalRepository,
    private val offlineSyncRepository: OfflineSyncRepository,
    private val scope: CoroutineScope
) {

    /**
     * Restoration Phase: Fetches all user and professional data from Supabase 
     * and hydrates the local Room database. Essential for fresh installs.
     */
    fun performFullHydration() {
        scope.launch {
            val user = userRepository.currentUser.value ?: return@launch
            println("SyncManager: Starting full hydration for ${user.userId}")

            try {
                offlineSyncRepository.flushPending()

                // 1. Restore Jump Logs
                val jumps = jumpLogRepository.getJumps()
                jumps.forEach { database.jumpLogDao().insertJump(it) }

                // 2. Restore Gear
                val gears = gearRepository.getUserGear()
                gears.forEach { database.userGearDao().insertGear(it) }

                // 3. Restore Personal Career Data (Jumpers & Licensed)
                val registeredUsers = userRepository.getAllRegisteredUsers()
                registeredUsers.forEach { database.userDao().insertUser(it) }

                val ratings = professionalRepository.getUserRatings()
                ratings.forEach { database.ratingDao().insertRating(it) }

                val federations = professionalRepository.getUserFederations()
                federations.forEach { database.federationDao().insertFederation(it) }

                val ledger = userRepository.getLedger()
                ledger.forEach { database.ledgerDao().insertTransaction(it) }

                val loyalty = userRepository.getUserLoyalty()
                loyalty?.let { database.userLoyaltyDao().insertLoyalty(it) }

                val skills = userRepository.getStudentSkills()
                skills.forEach { database.studentSkillDao().insertSkill(it) }

                val follows = userRepository.getUserFollows()
                follows.forEach { database.followDao().follow(it) }

                // 4. Restore Professional Assets (if DZO)
                if (user.role == "DZ_OPERATOR") {
                    val dzId = user.userId
                    
                    val facilities = professionalRepository.getDzFacilities(dzId)
                    facilities.forEach { database.dzFacilityDao().insertFacility(it) }
                    
                    val inventory = professionalRepository.getDzInventory(dzId)
                    inventory.forEach { database.dzInventoryDao().insertInventory(it) }
                    
                    val waivers = professionalRepository.getDzWaivers(dzId)
                    waivers.forEach { database.dzWaiverDao().insertWaiver(it) }
                    
                    val schedules = professionalRepository.getFlightSchedules(dzId)
                    schedules.forEach { database.flightScheduleDao().insertSchedule(it) }

                    val staff = professionalRepository.getDzStaffMemberships(dzId)
                    staff.forEach { database.dzStaffMembershipDao().insertMembership(it) }

                    val incidents = professionalRepository.getIncidentReports(dzId)
                    incidents.forEach { database.incidentReportDao().insertReport(it) }
                }

                println("SyncManager: Full hydration complete. Database restored.")
            } catch (e: Exception) {
                println("SyncManager Error: Hydration failed: ${e.message}")
            }
        }
    }
}
