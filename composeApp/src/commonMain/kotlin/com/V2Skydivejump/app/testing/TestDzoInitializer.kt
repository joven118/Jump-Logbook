package com.V2Skydivejump.app.testing

import com.V2Skydivejump.app.SessionViewModel
import com.V2Skydivejump.app.UserRole
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.*
import com.V2Skydivejump.app.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class TestDzoInitializer(
    private val scope: CoroutineScope,
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val professionalRepository: ProfessionalRepository
) {
    /**
     * Phase 1: Create Real Accounts via Supabase Auth
     */
    suspend fun createTestAccounts() {
        println("TEST: Initiating Account Creation...")
        try {
            // Sign Up DZO
            userRepository.signUp(
                email = "test_dzo@v2.com",
                password = "Password123!",
                name = "Test DZO Admin",
                role = "DZ_OPERATOR",
                license = "D-12345"
            )
            println("TEST: DZO Account Registered.")
            
            delay(2000) // Avoid rate limits

            // Sign Up Jumper
            userRepository.signUp(
                email = "test_jumper@v2.com",
                password = "Password123!",
                name = "Test Jumper Pro",
                role = "JUMPER",
                license = "B-54321"
            )
            println("TEST: Jumper Account Registered.")
        } catch (e: Exception) {
            println("TEST ERROR: Account creation failed: ${e.message}")
        }
    }

    /**
     * Phase 2: Setup DZO Infrastructure
     * (Simulates a DZO encoding their data)
     */
    suspend fun setupDzoAssets(dzId: String) {
        println("TEST: Setting up DZ Assets for $dzId")
        
        // 1. Add Aircraft
        val aircraftId = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
        val aircraft = DzInventoryEntity(
            id = aircraftId,
            dzId = dzId,
            name = "Test Caravan",
            category = "Aircraft",
            serialNumber = "N12345",
            aircraftModel = "C208",
            maxJumpers = 14,
            maxPayload = "3500",
            normalJumpAltitude = "13500",
            isGround = false
        )
        userRepository.addDzInventory(aircraft)
        database.dzInventoryDao().insertInventory(aircraft)

        // 2. Add Facility
        val hangar = DzFacilityEntity(
            id = aircraftId + 1,
            dzId = dzId,
            name = "Main Hangar",
            description = "Climate controlled packing and lounge area."
        )
        userRepository.addDzFacility(hangar)
        database.dzFacilityDao().insertFacility(hangar)

        // 3. Add Waiver
        val waiver = DzWaiverEntity(
            id = aircraftId + 2,
            dzId = dzId,
            title = "Test Liability Release",
            content = "This is a test waiver for automated verification of PDF features."
        )
        userRepository.upsertWaiver(waiver)
        database.dzWaiverDao().insertWaiver(waiver)

        println("TEST: DZO Infrastructure Sync Complete.")
    }

    /**
     * Phase 3: Simulate Full Operational Loop
     */
    suspend fun simulateFlightLoop(dzId: String, jumperId: String) {
        println("TEST: Simulating Flight Loop for Jumper $jumperId at DZ $dzId")
        
        // 1. DZO Adds a Manifest Load
        val inventory = database.dzInventoryDao().getInventoryForDz(dzId).first()
        val aircraft = inventory.firstOrNull() ?: return
        
        // 2. Jumper "Completes" a jump via Manifest Landing
        val now = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
        val jumpLog = JumpLogEntity(
            userId = jumperId,
            jumpNumber = 1, 
            date = now,
            dzId = dzId,
            dzName = "V2 Automated DZ",
            aircraftType = aircraft.aircraftModel,
            aircraftTailNumber = aircraft.serialNumber,
            isVerified = true,
            verifierName = "V2 Automated DZ",
            verificationMethod = "TEST_SUITE"
        )
        
        database.jumpLogDao().insertJump(jumpLog)
        
        println("TEST: Operational Loop Complete. Verified jump logged.")
    }
}
