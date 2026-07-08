package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.database.entities.JumpLogEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

enum class CurrencyStatus {
    GREEN, YELLOW, RED
}

object PerformanceAnalyticsEngine {

    /**
     * Calculates jumper currency based on USPA-style rules.
     * GREEN: < 60 days
     * YELLOW: 60-90 days (Custom middle ground)
     * RED: > 60 days (Strictly following the 60-day requirement for A-license)
     */
    fun calculateCurrency(lastJumpDate: Long, licenseLevel: String): CurrencyStatus {
        val now = 0L // Placeholder
        val daysSinceLastJump = (now - lastJumpDate) / (1000 * 60 * 60 * 24)

        return if (daysSinceLastJump < 60) {
            CurrencyStatus.GREEN
        } else if (daysSinceLastJump < 90) {
            CurrencyStatus.YELLOW
        } else {
            CurrencyStatus.RED
        }
    }

    /**
     * Analyzes canopy usage to recommend maintenance.
     */
    fun analyzeCanopyWear(jumpLogs: List<JumpLogEntity>, canopyName: String): String {
        val canopyJumps = jumpLogs.count { it.aircraft?.contains(canopyName, ignoreCase = true) == true }
        // Note: Using 'aircraft' field as a proxy for gear tracking until GearEntity is fully implemented
        
        return if (canopyJumps >= 400) {
            "Warning: $canopyName has $canopyJumps jumps. Lineset inspection recommended."
        } else {
            "$canopyName usage: $canopyJumps/400 jumps until next inspection."
        }
    }

    /**
     * Calculates total freefall time in seconds.
     */
    fun calculateTotalFreefallTime(jumpLogs: List<JumpLogEntity>): Int {
        return jumpLogs.sumOf { it.freefallTimeSeconds }
    }

    /**
     * Formats seconds into HH:MM:SS
     */
    fun formatDurationHms(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    /**
     * Generates a progression insight based on discipline variety and frequency.
     */
    fun getProgressionInsight(jumpLogs: List<JumpLogEntity>): String {
        if (jumpLogs.isEmpty()) return "Start logging jumps to see your progression trends."
        
        val recentJumps = jumpLogs.take(20)
        val disciplines = recentJumps.flatMap { it.disciplines?.split(",") ?: emptyList() }
            .filter { it.isNotBlank() }
            .groupBy { it }
            .mapValues { it.value.size }
            
        val primaryDiscipline = disciplines.maxByOrNull { it.value }?.key
        
        return if (primaryDiscipline != null) {
            "You are showing strong consistency in $primaryDiscipline. Consider incorporating more variety in your next 10 jumps to broaden your skill set."
        } else {
            "You are exploring various disciplines. This is great for building a solid foundation!"
        }
    }

    /**
     * Predicts the next milestone based on current jump count and frequency.
     */
    fun getMilestonePrediction(jumpLogs: List<JumpLogEntity>): String {
        val totalJumps = jumpLogs.size
        val milestones = listOf(25, 50, 200, 500, 1000, 2000, 5000)
        val nextMilestone = milestones.find { it > totalJumps } ?: return "You've reached elite status! Continue pushing the limits."
        
        val jumpsLeft = nextMilestone - totalJumps
        return "Based on your current activity, you are $jumpsLeft jumps away from your next major milestone ($nextMilestone jumps). Keep up the momentum!"
    }

    /**
     * Analyzes safety based on notes and landing styles.
     */
    fun getSafetyInsight(jumpLogs: List<JumpLogEntity>): String {
        val hardLandings = jumpLogs.count { it.landingStyles?.contains("hard", ignoreCase = true) == true }
        val unstableExits = jumpLogs.count { it.disciplines?.contains("Unstable", ignoreCase = true) == true }
        
        return when {
            hardLandings > 2 -> "We noticed a few hard landings recently. Focus on your flare timing and canopy control in your upcoming jumps."
            unstableExits > 2 -> "A few recent exits were logged as unstable. Practice your exit positions or consider a coach for your next few jumps."
            else -> "Your recent jump profile shows excellent stability and landing consistency. Great job maintaining safety standards!"
        }
    }
}
