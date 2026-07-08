package com.V2Skydivejump.app.ui.dzo

import com.V2Skydivejump.app.database.entities.*

enum class JumpType {
    LICENSED, TANDEM_STUDENT, AFF_STUDENT, INSTRUCTOR, VIDEOGRAPHER, COACH
}

enum class ReadinessStatus {
    READY, PENDING, MISSING, NOT_ELIGIBLE
}

data class StagingRequirementStatus(
    val profileComplete: Boolean = false,
    val equipmentVerified: Boolean = false,
    val currencyCurrent: Boolean = false,
    val waiversComplete: Boolean = false,
    val overallStatus: ReadinessStatus = ReadinessStatus.NOT_ELIGIBLE
)

data class ManifestJumper(
    val id: String,
    val name: String,
    val license: String,
    val membershipNumber: String = "",
    val ratings: String = "",
    val totalJumps: Int = 0,
    val currentDiscipline: String = "Unknown",
    val role: String,
    val balance: Double,
    val jumpType: JumpType,
    val isCheckedIn: Boolean = false,
    val isWaiverSigned: Boolean = false,
    val signedWaiverId: Long? = null,
    val assignedInstructorId: String? = null,
    val exitWeight: Double? = null,
    val profilePhotoUrl: String? = null,
    val requirements: StagingRequirementStatus = StagingRequirementStatus(),
    val rentalGear: List<DzInventoryEntity> = emptyList()
)


enum class LoadStatus {
    PLANNING, BOARDING, TAXIING, IN_FLIGHT, LANDED
}

data class FlightLoad(
    val id: Int,
    val aircraft: DzInventoryEntity?,
    val status: LoadStatus,
    val jumpers: List<ManifestJumper> = emptyList(),
    val takeoffTime: Long? = null,
    val turnaroundTimeMinutes: Int = 20,
    val maxJumpers: Int = 0,
    val pilotName: String = ""
)

enum class DzOperationalStatus {
    OPEN, STUDENT_HOLD, WEATHER_HOLD, CLOSED
}

enum class SafetyLevel {
    NORMAL, CAUTION, DANGER
}

data class SafetyAlert(
    val level: SafetyLevel,
    val message: String,
    val timestamp: Long
)

data class StaffMember(
    val id: String,
    val name: String,
    val role: String,
    val status: String, // "Active", "On Break", "Off"
    val assignedTo: String? = null
)

data class Booking(
    val id: String,
    val customerName: String,
    val jumpType: JumpType,
    val preferredDate: Long,
    val status: String,
    val paymentStatus: String,
    val rentalGearIds: List<Long> = emptyList(),
    val rentalTotal: Double = 0.0
)

enum class WeatherSource {
    AUTO_DEFAULT, METAR_NOAA, OPEN_WEATHER, DZ_LOCAL_SENSOR, WINDY
}

data class WeatherData(
    val temperature: Double = 0.0,
    val windSpeedKts: Double = 0.0,
    val windDirectionDeg: Int = 0,
    val gustKts: Double = 0.0,
    val visibilityMiles: Double = 10.0,
    val cloudBaseFt: Int = 10000,
    val condition: String = "Clear",
    val pressureInHg: Double = 29.92,
    val humidity: Int = 50,
    val timestamp: Long = com.V2Skydivejump.app.TimeUtils.nowEpochMillis(),
    val source: WeatherSource = WeatherSource.AUTO_DEFAULT
)

data class DzOperationalMetrics(
    val totalLoadsToday: Int = 0,
    val jumpersOnSite: Int = 0,
    val tandemReservations: Int = 0,
    val activeAircraftCount: Int = 0,
    val weatherSnapshot: String = "Clear, 15kts SE",
    val dailyJumpCount: Int = 0,
    val avgTurnaroundMinutes: Int = 22,
    val currentOperationalRisk: SafetyLevel = SafetyLevel.NORMAL,
    
    // Staging specific summary
    val stagingReady: Int = 0,
    val stagingPending: Int = 0,
    val stagingIncomplete: Int = 0,
    val stagingStudents: Int = 0,
    val stagingTandems: Int = 0,
    
    // Promo specific summary
    val activePromos: Int = 0,
    val promoRedemptionsToday: Int = 0,
    val promoRevenue: Double = 0.0
)


data class FinanceReport(
    val revenueItems: List<FinanceItem>,
    val expenseItems: List<FinanceItem>,
    val totalRevenue: Double,
    val totalExpenses: Double,
    val netProfit: Double
)

data class FinanceItem(
    val category: String,
    val amount: Double,
    val percentage: Double = 0.0
)

data class CashFlowReport(
    val beginningCash: Double,
    val cashIn: Double,
    val cashOut: Double,
    val endingCash: Double
)

data class BalanceSheetReport(
    val currentAssets: List<FinanceItem>,
    val fixedAssets: List<FinanceItem>,
    val liabilities: List<FinanceItem>,
    val equity: List<FinanceItem>,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val totalEquity: Double
)

data class AircraftProfitability(
    val aircraftName: String,
    val revenue: Double,
    val fuel: Double,
    val maintenance: Double,
    val profit: Double
)

data class LoadProfitability(
    val loadId: Int,
    val jumperCount: Int,
    val revenue: Double,
    val fuel: Double,
    val pilotCost: Double,
    val profit: Double
)

data class AiRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // OPERATIONAL, FINANCIAL, SAFETY, MARKETING
    val impactScore: Int, // 1-10
    val actionLabel: String = "Execute",
    val timestamp: Long = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
)

data class CustomerClvMetric(
    val userId: String,
    val userName: String,
    val totalSpend: Double,
    val jumpCount: Int,
    val lastJumpDate: Long,
    val clvScore: Int // 1-100
)

data class GearRoiMetric(
    val gearId: Long,
    val gearName: String,
    val purchasePrice: Double,
    val totalRevenue: Double,
    val maintenanceCost: Double,
    val breakEvenProgress: Float // 0.0 - 1.0
)

data class PricingSuggestion(
    val activityType: String,
    val currentPrice: Double,
    val suggestedPrice: Double,
    val reason: String,
    val confidence: Int // 1-100
)

