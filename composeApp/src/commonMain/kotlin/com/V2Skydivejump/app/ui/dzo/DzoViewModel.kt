package com.V2Skydivejump.app.ui.dzo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.*
import com.V2Skydivejump.app.data.UserRepository
import com.V2Skydivejump.app.data.ProfessionalRepository
import com.V2Skydivejump.app.data.JumpLogRepository
import com.V2Skydivejump.app.data.OfflineSyncRepository
import com.V2Skydivejump.app.data.SyncOperationType
import com.V2Skydivejump.app.utils.MediaUploadService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DzoUiState(
    val user: UserEntity? = null,
    val dzName: String = "",
    val dzLocation: String = "",
    val operationalStatus: DzOperationalStatus = DzOperationalStatus.OPEN,
    val metrics: DzOperationalMetrics = DzOperationalMetrics(),
    val aircrafts: List<AircraftEntity> = emptyList(),
    val flightSchedules: List<FlightScheduleEntity> = emptyList(),
    val unmanifestedJumpers: List<ManifestJumper> = emptyList(),
    val activeLoads: List<FlightLoad> = emptyList(),
    val safetyAlerts: List<SafetyAlert> = emptyList(),
    val staff: List<StaffMember> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val feedPosts: List<FeedPostEntity> = emptyList(),
    val totalRevenue: String = "$0.00",
    val jumperVolume: Int = 0,
    val aircraftHours: String = "0.0",
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val dzStaff: List<UserEntity> = emptyList(),
    val dzFacilities: List<DzFacilityEntity> = emptyList(),
    val dzInventory: List<DzInventoryEntity> = emptyList(),
    val dzRatings: List<DzRatingEntity> = emptyList(),
    val dzWaivers: List<DzWaiverEntity> = emptyList(),
    val promotions: List<PromotionEntity> = emptyList(),
    val incidentReports: List<IncidentReportEntity> = emptyList(),
    val averageRating: Double = 0.0,
    val financeReport: FinanceReport = FinanceReport(emptyList(), emptyList(), 0.0, 0.0, 0.0),
    val cashFlowReport: CashFlowReport = CashFlowReport(0.0, 0.0, 0.0, 0.0),
    val balanceSheet: BalanceSheetReport = BalanceSheetReport(emptyList(), emptyList(), emptyList(), emptyList(), 0.0, 0.0, 0.0),
    val aircraftProfitability: List<AircraftProfitability> = emptyList(),
    val loadProfitability: List<LoadProfitability> = emptyList(),
    val aiRecommendations: List<AiRecommendation> = emptyList(),
    val customerClvMetrics: List<CustomerClvMetric> = emptyList(),
    val gearRoiMetrics: List<GearRoiMetric> = emptyList(),
    val pricingSuggestions: List<PricingSuggestion> = emptyList(),
    val weatherData: WeatherData = WeatherData(),
    val selectedWeatherSource: WeatherSource = WeatherSource.AUTO_DEFAULT,
    val privacySettings: UserPrivacySettingsEntity? = null,
    val isFollowing: Boolean = false,
    val isLoading: Boolean = false
)

class DzoViewModel(
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val professionalRepository: ProfessionalRepository,
    private val jumpLogRepository: JumpLogRepository,
    private val offlineSyncRepository: OfflineSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DzoUiState())
    val uiState: StateFlow<DzoUiState> = _uiState.asStateFlow()

    private val targetDzId = MutableStateFlow<String?>(null)
    private var lastGeneratedId = 0L

    init {
        observeProfile()
        observeTargetDz()
        loadDzFacilities()
        loadDzInventory()
        loadDzRatings()
        loadDzWaivers()
        loadPromotions()
        loadFinancialData()
        loadAiInsights()
        loadWeatherData()
        loadCfoAnalytics()
        observeJumpers()
    }

    fun setTargetDzId(dzId: String) {
        targetDzId.value = dzId
    }

    private fun nextRemoteId(): Long {
        val now = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
        lastGeneratedId = maxOf(now, lastGeneratedId + 1)
        return lastGeneratedId
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepository.currentUser.collectLatest { user ->
                if (user == null) return@collectLatest
                _uiState.update { it.copy(user = user) }
                if (user.role == "DZ_OPERATOR" && targetDzId.value == null) {
                    targetDzId.value = user.userId
                }
            }
        }
    }

    private fun observeTargetDz() {
        viewModelScope.launch {
            targetDzId.collectLatest { dzId ->
                if (dzId == null) return@collectLatest
                
                // Load additional DZO specific data
                loadDzStaff(dzId)
                loadFlightSchedules(dzId)
                loadAircrafts(dzId)
                loadPrivacySettings(dzId)
                loadBookings(dzId)
                loadIncidentReports(dzId)
                checkFollowingStatus(dzId)
                
                // Load followers/following count
                database.followDao().getFollowerCount(dzId).collect { count ->
                    _uiState.update { it.copy(followerCount = count) }
                }
                database.followDao().getFollowingCount(dzId).collect { count ->
                    _uiState.update { it.copy(followingCount = count) }
                }
            }
        }
    }

    private fun loadBookings(dzId: String) {
        viewModelScope.launch {
            // Derived from flight schedules
            database.flightScheduleDao().getSchedulesForDz(dzId).collect { schedules ->
                val allBookings = mutableListOf<Booking>()
                schedules.forEach { schedule ->
                    val jumperIds = schedule.bookedJumperIds.split(",").filter { it.isNotBlank() }
                    jumperIds.forEach { jumperId ->
                        allBookings.add(
                            Booking(
                                id = "${schedule.scheduleId}_$jumperId",
                                customerName = "Jumper $jumperId", 
                                jumpType = JumpType.LICENSED,
                                preferredDate = schedule.dateOfFlight,
                                status = "Confirmed",
                                paymentStatus = "Paid"
                            )
                        )
                    }
                }
                val sortedBookings = allBookings.sortedBy { it.preferredDate }
                _uiState.update { it.copy(bookings = sortedBookings) }
            }
        }
    }

    private fun loadPrivacySettings(userId: String) {
        viewModelScope.launch {
            database.privacyDao().getPrivacySettings(userId).collect { settings ->
                _uiState.update { it.copy(privacySettings = settings) }
            }
        }
    }

    private fun loadDzStaff(dzId: String) {
        viewModelScope.launch {
            database.dzStaffMembershipDao().getStaffForDz(dzId).collect { memberships ->
                val staffUserIds = memberships.map { it.userId }
                val staffUsers = mutableListOf<UserEntity>()
                staffUserIds.forEach { id ->
                    database.userDao().getUserById(id)?.let { staffUsers.add(it) }
                }
                _uiState.update { it.copy(dzStaff = staffUsers) }
            }
        }
    }

    private fun loadIncidentReports(dzId: String) {
        viewModelScope.launch {
            database.incidentReportDao().getReportsForDz(dzId).collect { list ->
                _uiState.update { it.copy(incidentReports = list) }
            }
        }
    }

    private fun loadFlightSchedules(dzId: String) {
        viewModelScope.launch {
            database.flightScheduleDao().getSchedulesForDz(dzId).collect { schedules ->
                _uiState.update { it.copy(flightSchedules = schedules) }
            }
        }
    }

    private fun loadAircrafts(dzId: String) {
        viewModelScope.launch {
            database.aircraftDao().getAircraftsForDz(dzId).collect { list ->
                _uiState.update { it.copy(aircrafts = list) }
            }
        }
    }

    private fun loadDzFacilities() {
        viewModelScope.launch {
            targetDzId.collectLatest { dzId ->
                if (dzId == null) return@collectLatest
                database.dzFacilityDao().getFacilitiesForDz(dzId).collect { list ->
                    _uiState.update { it.copy(dzFacilities = list) }
                }
            }
        }
    }

    private fun loadDzInventory() {
        viewModelScope.launch {
            targetDzId.collectLatest { dzId ->
                if (dzId == null) return@collectLatest
                database.dzInventoryDao().getInventoryForDz(dzId).collect { list ->
                    _uiState.update { it.copy(dzInventory = list) }
                }
            }
        }
    }

    private fun loadDzRatings() {
        viewModelScope.launch {
            targetDzId.collectLatest { dzId ->
                if (dzId == null) return@collectLatest
                database.dzRatingDao().getRatingsForDz(dzId).collect { ratings ->
                    _uiState.update { it.copy(dzRatings = ratings) }
                }
                database.dzRatingDao().getAverageRatingForDz(dzId).collect { avg ->
                    _uiState.update { it.copy(averageRating = avg ?: 0.0) }
                }
            }
        }
    }

    private fun loadDzWaivers() {
        viewModelScope.launch {
            targetDzId.collectLatest { dzId ->
                if (dzId == null) return@collectLatest
                database.dzWaiverDao().getActiveWaiversForDz(dzId).collect { waivers ->
                    _uiState.update { it.copy(dzWaivers = waivers) }
                }
            }
        }
    }

    private fun loadPromotions() {
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            // No mock promos for clean start
            _uiState.update { it.copy(promotions = emptyList()) }
        }
    }

    fun upsertPromotion(promo: PromotionEntity) {
        viewModelScope.launch {
            // In a real app, save to DAO and sync
            val updatedPromos = uiState.value.promotions.toMutableList()
            val index = updatedPromos.indexOfFirst { it.id == promo.id }
            if (index != -1) updatedPromos[index] = promo else updatedPromos.add(promo)
            _uiState.update { it.copy(promotions = updatedPromos) }
        }
    }

    private fun loadFinancialData() {
        // Zero values for clean start
        _uiState.update { state ->
            state.copy(
                financeReport = FinanceReport(emptyList(), emptyList(), 0.0, 0.0, 0.0),
                cashFlowReport = CashFlowReport(0.0, 0.0, 0.0, 0.0),
                balanceSheet = BalanceSheetReport(emptyList(), emptyList(), emptyList(), emptyList(), 0.0, 0.0, 0.0),
                aircraftProfitability = emptyList(),
                loadProfitability = emptyList(),
                totalRevenue = "$0"
            )
        }
    }

    fun updateFinanceReport(report: FinanceReport) {
        _uiState.update { it.copy(financeReport = report) }
    }

    fun updateCashFlowReport(report: CashFlowReport) {
        _uiState.update { it.copy(cashFlowReport = report) }
    }

    private fun loadAiInsights() {
        viewModelScope.launch {
            // Insights only generated if there is operational data
            _uiState.update { it.copy(aiRecommendations = emptyList()) }
        }
    }

    fun updateBalanceSheetReport(report: BalanceSheetReport) {
        _uiState.update { it.copy(balanceSheet = report) }
    }

    private fun observeJumpers() {
        // Empty jumpers list for clean start
        _uiState.update { it.copy(
            unmanifestedJumpers = emptyList(),
            metrics = it.metrics.copy(
                stagingReady = 0,
                stagingPending = 0,
                stagingIncomplete = 0,
                stagingStudents = 0,
                stagingTandems = 0
            )
        ) }
    }

    fun updateProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            // Instant UI feedback
            _uiState.update { it.copy(user = updatedUser) }
            
            database.userDao().updateUser(updatedUser)
            try {
                userRepository.updateProfile(updatedUser)
            } catch (e: Exception) { 
                offlineSyncRepository.enqueue(SyncOperationType.USER_PROFILE_UPSERT, updatedUser)
                println("DzoViewModel Error: Profile sync failed: ${e.message}")
            }
        }
    }

    fun updatePrivacySettings(settings: UserPrivacySettingsEntity) {
        viewModelScope.launch {
            database.privacyDao().updatePrivacySettings(settings)
            try {
                userRepository.updatePrivacySettings(settings)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.PRIVACY_UPSERT, settings)
            }
        }
    }

    fun uploadProfileMedia(type: String, uri: String) {
        viewModelScope.launch {
            try {
                userRepository.uploadProfileMedia(type, uri)
            } catch (e: Exception) { 
                println("DzoViewModel Error: Media upload failed: ${e.message}")
            }
        }
    }

    fun addDzFacility(name: String, description: String, photoUri: String?) {
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            val uploadedPhotoUrl = try {
                uploadOptionalPhoto(photoUri, "facility", dzId)
            } catch (e: Exception) {
                println("DzoViewModel Error: Facility photo upload failed: ${e.message}")
                return@launch
            }
            val facility = DzFacilityEntity(
                id = nextRemoteId(),
                dzId = dzId,
                name = name,
                description = description,
                photoUrl = uploadedPhotoUrl
            )
            database.dzFacilityDao().insertFacility(facility)
            try {
                userRepository.addDzFacility(facility)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_FACILITY_UPSERT, facility)
                println("DzoViewModel Error: Facility cloud save failed: ${e.message}")
            }
        }
    }

    fun updateDzFacility(facility: DzFacilityEntity, newPhotoUri: String?) {
        viewModelScope.launch {
            val uploadedPhotoUrl = try {
                uploadOptionalPhoto(newPhotoUri, "facility", facility.id.toString())
            } catch (e: Exception) {
                println("DzoViewModel Error: Facility photo upload failed: ${e.message}")
                return@launch
            }
            val updated = if (uploadedPhotoUrl != null) facility.copy(photoUrl = uploadedPhotoUrl) else facility
            database.dzFacilityDao().updateFacility(updated)
            try {
                userRepository.updateDzFacility(updated)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_FACILITY_UPSERT, updated)
                println("DzoViewModel Error: Facility cloud update failed: ${e.message}")
            }
        }
    }

    fun deleteDzFacility(facility: DzFacilityEntity) {
        viewModelScope.launch {
            database.dzFacilityDao().deleteFacility(facility)
            try {
                userRepository.deleteDzFacility(facility.id)
            } catch (e: Exception) {
                offlineSyncRepository.enqueueRaw(SyncOperationType.DZ_FACILITY_DELETE, facility.id.toString())
                println("DzoViewModel Error: Facility cloud delete failed: ${e.message}")
            }
        }
    }

    fun addDzInventory(item: DzInventoryEntity) {
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            val uploadedPhotoUrl = try {
                uploadOptionalPhoto(item.photoUrl, "inventory", dzId)
            } catch (e: Exception) {
                println("DzoViewModel Error: Inventory photo upload failed: ${e.message}")
                return@launch
            }
            val newItem = item.copy(id = if (item.id == 0L) nextRemoteId() else item.id, dzId = dzId, photoUrl = uploadedPhotoUrl)
            database.dzInventoryDao().insertInventory(newItem)
            try {
                userRepository.addDzInventory(newItem)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_INVENTORY_UPSERT, newItem)
                println("DzoViewModel Error: Inventory cloud save failed: ${e.message}")
            }
        }
    }

    fun updateDzInventory(item: DzInventoryEntity, newPhotoUri: String?) {
        viewModelScope.launch {
            val uploadedPhotoUrl = try {
                uploadOptionalPhoto(newPhotoUri, "inventory", item.id.toString())
            } catch (e: Exception) {
                println("DzoViewModel Error: Inventory photo upload failed: ${e.message}")
                return@launch
            }
            val updated = if (uploadedPhotoUrl != null) item.copy(photoUrl = uploadedPhotoUrl) else item
            database.dzInventoryDao().updateInventory(updated)
            try {
                userRepository.updateDzInventory(updated)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_INVENTORY_UPSERT, updated)
                println("DzoViewModel Error: Inventory cloud update failed: ${e.message}")
            }
        }
    }

    fun deleteDzInventory(item: DzInventoryEntity) {
        viewModelScope.launch {
            database.dzInventoryDao().deleteInventory(item)
            try {
                userRepository.deleteDzInventory(item.id)
            } catch (e: Exception) {
                offlineSyncRepository.enqueueRaw(SyncOperationType.DZ_INVENTORY_DELETE, item.id.toString())
                println("DzoViewModel Error: Inventory cloud delete failed: ${e.message}")
            }
        }
    }

    private suspend fun uploadOptionalPhoto(uri: String?, entityType: String, entityId: String): String? {
        if (uri.isNullOrBlank()) return null
        if (uri.startsWith("http://") || uri.startsWith("https://")) return uri
        return MediaUploadService.uploadLocalUri(
            localUri = uri,
            bucketName = "inventory-media",
            entityType = entityType,
            entityId = entityId,
            mediaKind = "image"
        ).publicUrl
    }

    fun upsertIncidentReport(report: IncidentReportEntity) {
        viewModelScope.launch {
            val persistedReport = report.copy(id = if (report.id == 0L) nextRemoteId() else report.id)
            database.incidentReportDao().insertReport(persistedReport)
            try {
                professionalRepository.saveIncidentReport(persistedReport)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.INCIDENT_REPORT_UPSERT, persistedReport)
                println("DzoViewModel Error: Incident report cloud save failed: ${e.message}")
            }

            val alert = SafetyAlert(
                level = SafetyLevel.CAUTION,
                message = "New Incident Logged: ${persistedReport.type}",
                timestamp = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            _uiState.update { it.copy(safetyAlerts = listOf(alert) + it.safetyAlerts) }
        }
    }

    fun updateRiggerSeal(symbol: String, license: String) {
        viewModelScope.launch {
            val currentUser = uiState.value.user ?: return@launch
            val updated = currentUser.copy(riggerSealSymbol = symbol, riggerLicense = license)
            database.userDao().updateUser(updated)
            _uiState.update { it.copy(user = updated) }
            try {
                userRepository.updateProfile(updated)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.USER_PROFILE_UPSERT, updated)
                println("DzoViewModel Error: Rigger seal cloud save failed: ${e.message}")
            }
        }
    }

    fun addDzRating(stars: Int, comment: String) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value ?: return@launch
            val dzId = targetDzId.value ?: return@launch
            
            val rating = DzRatingEntity(
                id = nextRemoteId(),
                dzId = dzId,
                userId = currentUser.userId,
                userName = currentUser.name,
                stars = stars,
                comment = comment,
                timestamp = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            
            database.dzRatingDao().insertRating(rating)
            try {
                userRepository.addDzRating(rating)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_RATING_UPSERT, rating)
                println("DzoViewModel Error: Rating cloud save failed: ${e.message}")
            }
        }
    }

    fun upsertWaiver(title: String, content: String, id: Long = 0) {
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            val waiver = DzWaiverEntity(
                id = if (id == 0L) nextRemoteId() else id,
                dzId = dzId,
                title = title,
                content = content,
                lastUpdated = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            if (id == 0L) {
                database.dzWaiverDao().insertWaiver(waiver)
            } else {
                database.dzWaiverDao().updateWaiver(waiver)
            }
            try {
                userRepository.upsertWaiver(waiver)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_WAIVER_UPSERT, waiver)
                println("DzoViewModel Error: Waiver cloud save failed: ${e.message}")
            }
        }
    }

    fun deactivateWaiver(waiver: DzWaiverEntity) {
        viewModelScope.launch {
            val updated = waiver.copy(isActive = false)
            database.dzWaiverDao().updateWaiver(updated)
            try {
                userRepository.upsertWaiver(updated)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_WAIVER_UPSERT, updated)
                println("DzoViewModel Error: Waiver cloud update failed: ${e.message}")
            }
        }
    }

    fun checkInJumper(jumper: ManifestJumper, loadId: Int) {
        viewModelScope.launch {
            val updatedLoads = uiState.value.activeLoads.map { load ->
                if (load.id == loadId) {
                    val updatedJumpers = load.jumpers.map { j ->
                        if (j.id == jumper.id) j.copy(isCheckedIn = true) else j
                    }
                    load.copy(jumpers = updatedJumpers)
                } else load
            }
            _uiState.update { it.copy(activeLoads = updatedLoads) }
            
            val dzId = targetDzId.value ?: return@launch
            val signature = JumperWaiverSignatureEntity(
                dzId = dzId,
                userId = jumper.id,
                waiverId = 0,
                signatureBase64 = "SIGNED_ELECTRONICALLY", 
                signedAt = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            database.dzWaiverDao().insertSignature(signature)
            try {
                userRepository.signWaiver(signature)
            } catch (e: Exception) {
                println("DzoViewModel Error: Waiver signature cloud save failed: ${e.message}")
            }
        }
    }

    fun addFlightSchedule(date: Long, frequency: String, loadNum: Int, aircraft: DzInventoryEntity, maxJumpers: Int) {
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            val schedule = FlightScheduleEntity(
                scheduleId = nextRemoteId(),
                dzId = dzId,
                dateOfFlight = date,
                frequency = frequency,
                loadNumber = loadNum,
                aircraftId = aircraft.id,
                aircraftName = aircraft.name,
                aircraftType = aircraft.aircraftType,
                aircraftTailNumber = aircraft.serialNumber,
                loadCapacity = maxJumpers,
                creationSource = "SCHEDULE"
            )
            database.flightScheduleDao().insertSchedule(schedule)
            try {
                professionalRepository.updateFlightSchedule(schedule)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.FLIGHT_SCHEDULE_UPSERT, schedule)
                println("DzoViewModel Error: Flight schedule cloud save failed: ${e.message}")
            }
        }
    }

    fun bookFlight(scheduleId: Long) {
        viewModelScope.launch {
            val user = uiState.value.user ?: return@launch
            val schedule = database.flightScheduleDao().getScheduleById(scheduleId) ?: return@launch
            val jumperIds = schedule.bookedJumperIds.split(",").filter { it.isNotBlank() }.toMutableList()
            if (!jumperIds.contains(user.userId)) {
                jumperIds.add(user.userId)
                val updatedSchedule = schedule.copy(bookedJumperIds = jumperIds.joinToString(","))
                database.flightScheduleDao().updateSchedule(updatedSchedule)
                try {
                    professionalRepository.updateFlightSchedule(updatedSchedule)
                } catch (e: Exception) {
                    offlineSyncRepository.enqueue(SyncOperationType.FLIGHT_SCHEDULE_UPSERT, updatedSchedule)
                    println("DzoViewModel Error: Flight booking cloud save failed: ${e.message}")
                }
                
                // Add notification for DZO
                val notification = SafetyAlert(
                    level = SafetyLevel.NORMAL,
                    message = "New Booking: ${user.name} booked Load #${schedule.loadNumber}",
                    timestamp = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
                )
                _uiState.update { it.copy(safetyAlerts = listOf(notification) + it.safetyAlerts) }
            }
        }
    }

    fun addAircraft(name: String, tail: String, model: String, capacity: Int) {
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            val ac = AircraftEntity(
                aircraftId = "ac_${com.V2Skydivejump.app.TimeUtils.nowEpochMillis()}",
                dzId = dzId,
                aircraftName = name,
                tailNumber = tail,
                model = model,
                maxCapacity = capacity,
                maxPayload = (capacity * 220.0) // Heuristic default
            )
            database.aircraftDao().insertAircraft(ac)
            
            // Also add as inventory
            val inventoryAc = DzInventoryEntity(
                id = nextRemoteId(),
                dzId = dzId,
                name = name,
                category = "Aircraft",
                serialNumber = tail,
                aircraftModel = model,
                maxJumpers = capacity,
                maxPayload = (capacity * 220.0).toString()
            )
            database.dzInventoryDao().insertInventory(inventoryAc)
            try {
                userRepository.addDzInventory(inventoryAc)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_INVENTORY_UPSERT, inventoryAc)
                println("DzoViewModel Error: Aircraft inventory cloud save failed: ${e.message}")
            }
        }
    }

    fun addDzStaff(name: String, email: String, role: String) {
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            
            // 1. Check if user exists, otherwise create a placeholder staff user
            var user = database.userDao().getUserByEmail(email)
            if (user == null) {
                val newUserId = "staff_${com.V2Skydivejump.app.TimeUtils.nowEpochMillis()}"
                user = UserEntity(
                    userId = newUserId,
                    name = name,
                    email = email,
                    licenseNumber = "STAFF",
                    role = role
                )
                database.userDao().insertUser(user)
            }

            // 2. Add membership linking this user to the DZ
            val membership = DzStaffMembershipEntity(
                dzId = dzId,
                userId = user.userId,
                staffRole = role
            )
            database.dzStaffMembershipDao().insertMembership(membership)
            
            // 3. Refresh staff list in UI
            loadDzStaff(dzId)

            try {
                userRepository.addDzStaffMembership(membership)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.DZ_STAFF_UPSERT, membership)
            }
        }
    }

    fun manifestJumper(jumper: ManifestJumper, loadId: Int) {
        // AI Weather-Safety Interlock Check
        if (uiState.value.operationalStatus == DzOperationalStatus.STUDENT_HOLD && 
            (jumper.jumpType == JumpType.AFF_STUDENT || jumper.jumpType == JumpType.TANDEM_STUDENT)) {
            val alert = SafetyAlert(
                level = SafetyLevel.DANGER,
                message = "MANIFEST REJECTED: Jumper ${jumper.name} is a student and DZ is on STUDENT HOLD.",
                timestamp = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            _uiState.update { it.copy(safetyAlerts = listOf(alert) + it.safetyAlerts) }
            return
        }

        if (uiState.value.operationalStatus == DzOperationalStatus.WEATHER_HOLD) {
            val alert = SafetyAlert(
                level = SafetyLevel.DANGER,
                message = "MANIFEST REJECTED: All operations suspended due to WEATHER HOLD.",
                timestamp = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            _uiState.update { it.copy(safetyAlerts = listOf(alert) + it.safetyAlerts) }
            return
        }

        val load = uiState.value.activeLoads.find { it.id == loadId } ?: return
        val totalWeight = (load.jumpers + jumper).sumOf { it.exitWeight ?: 200.0 }
        val maxPayload = load.aircraft?.maxPayload?.toDoubleOrNull() ?: (load.aircraft?.maxJumpers?.toDouble() ?: 10.0) * 220.0

        if (totalWeight > maxPayload) {
            val alert = SafetyAlert(
                level = SafetyLevel.DANGER,
                message = "Load #$loadId exceeds maximum payload limit!",
                timestamp = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            _uiState.update { it.copy(safetyAlerts = it.safetyAlerts + alert) }
        }

        val updatedLoads = uiState.value.activeLoads.map { l ->
            if (l.id == loadId) l.copy(jumpers = l.jumpers + jumper) else l
        }
        val updatedUnmanifested = uiState.value.unmanifestedJumpers.filter { it.id != jumper.id }
        _uiState.update { it.copy(activeLoads = updatedLoads, unmanifestedJumpers = updatedUnmanifested) }
    }

    fun unmanifestJumper(jumper: ManifestJumper, loadId: Int) {
        val updatedLoads = uiState.value.activeLoads.map { load ->
            if (load.id == loadId) load.copy(jumpers = load.jumpers.filter { it.id != jumper.id }) else load
        }
        _uiState.update { it.copy(activeLoads = updatedLoads, unmanifestedJumpers = uiState.value.unmanifestedJumpers + jumper) }
    }

    fun addLoad(loadNumber: Int, aircraft: DzInventoryEntity, maxJumpers: Int, pilotName: String) {
        if (aircraft.isGround) {
            val alert = SafetyAlert(
                level = SafetyLevel.DANGER,
                message = "MANIFEST REJECTED: ${aircraft.name} is GROUNDED for maintenance.",
                timestamp = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            )
            _uiState.update { it.copy(safetyAlerts = listOf(alert) + it.safetyAlerts) }
            return
        }

        val newLoad = FlightLoad(
            id = loadNumber,
            aircraft = aircraft,
            status = LoadStatus.PLANNING,
            jumpers = emptyList(),
            maxJumpers = maxJumpers,
            pilotName = pilotName
        )
        _uiState.update { it.copy(activeLoads = it.activeLoads + newLoad) }

        // Automatically add to Flight Schedules for DZO Profile visibility
        viewModelScope.launch {
            val dzId = targetDzId.value ?: return@launch
            val schedule = FlightScheduleEntity(
                scheduleId = nextRemoteId(),
                dzId = dzId,
                dateOfFlight = com.V2Skydivejump.app.TimeUtils.nowEpochMillis(),
                frequency = "Specific Date",
                loadNumber = loadNumber,
                aircraftId = aircraft.id,
                aircraftName = aircraft.name,
                aircraftType = aircraft.aircraftType,
                aircraftTailNumber = aircraft.serialNumber,
                loadCapacity = maxJumpers,
                creationSource = "MANIFEST"
            )
            database.flightScheduleDao().insertSchedule(schedule)
            try {
                professionalRepository.updateFlightSchedule(schedule)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.FLIGHT_SCHEDULE_UPSERT, schedule)
                println("DzoViewModel Error: Manifest schedule cloud save failed: ${e.message}")
            }
        }
    }

    fun updateLoadStatus(loadId: Int, newStatus: LoadStatus) {
        val currentLoad = uiState.value.activeLoads.find { it.id == loadId } ?: return
        
        if (newStatus == LoadStatus.LANDED) {
            completeLoad(currentLoad)
        } else {
            val updatedLoads = uiState.value.activeLoads.map { 
                if (it.id == loadId) it.copy(status = newStatus) else it 
            }
            _uiState.update { it.copy(activeLoads = updatedLoads) }
        }
    }

    private fun completeLoad(load: FlightLoad) {
        viewModelScope.launch {
            val now = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()
            val dz = targetDzId.value ?: ""
            val dzName = uiState.value.user?.dzName ?: "Verified DZ"
            
            load.jumpers.forEach { jumper ->
                // 1. Create Verified Jump Log
                val jumpLog = JumpLogEntity(
                    userId = jumper.id,
                    jumpNumber = jumper.totalJumps + 1,
                    date = now,
                    dzId = dz,
                    dzName = dzName,
                    aircraftType = load.aircraft?.aircraftType ?: "",
                    aircraftTailNumber = load.aircraft?.serialNumber ?: "",
                    isVerified = true,
                    verifierName = dzName,
                    verificationMethod = "DZO_MANIFEST_PUSH"
                )
                database.jumpLogDao().insertJump(jumpLog)
                
                // CRITICAL FIX: Push verified jump to Jumper's Cloud Logbook
                try {
                    jumpLogRepository.insertJumpLog(jumpLog)
                } catch (e: Exception) { }
                
                // 2. Update User Stats (Verified Jumps)
                val user = database.userDao().getUserById(jumper.id)
                user?.let {
                    val updated = it.copy(baseJumpCount = it.baseJumpCount + 1)
                    database.userDao().updateUser(updated)
                    try {
                        userRepository.updateProfile(updated)
                    } catch (e: Exception) {
                        offlineSyncRepository.enqueue(SyncOperationType.USER_PROFILE_UPSERT, updated)
                    }
                }
            }
            
            // 3. Deduct Fuel & Track Flight Hours
            load.aircraft?.let { aircraft ->
                val currentAc = database.dzInventoryDao().getAllInventory().first().find { it.id == aircraft.id }
                currentAc?.let { ac ->
                    val fuelUsed = ac.fuelBurnPerLoad
                    val newFuel = (ac.currentFuel - fuelUsed).coerceAtLeast(0.0)
                    
                    val loadHours = load.turnaroundTimeMinutes / 60.0
                    val newTotalHours = ac.totalFlightHours + loadHours
                    val isDueForMaintenance = newTotalHours >= ac.maintenanceIntervalHours
                    
                    val updatedAircraft = ac.copy(
                        currentFuel = newFuel,
                        totalFlightHours = newTotalHours,
                        isGround = isDueForMaintenance
                    )
                    database.dzInventoryDao().updateInventory(updatedAircraft)
                    try {
                        userRepository.updateDzInventory(updatedAircraft)
                    } catch (e: Exception) {
                        offlineSyncRepository.enqueue(SyncOperationType.DZ_INVENTORY_UPSERT, updatedAircraft)
                    }
                    
                    // Safety Alert if fuel is low (less than 3 loads remaining)
                    if (newFuel < (fuelUsed * 3)) {
                        val alert = SafetyAlert(
                            level = SafetyLevel.CAUTION,
                            message = "Low Fuel: ${ac.name} has only ${"%.1f".format(newFuel)} gal remaining.",
                            timestamp = now
                        )
                        _uiState.update { it.copy(safetyAlerts = it.safetyAlerts + alert) }
                    }

                    // Critical Alert if Grounded
                    if (isDueForMaintenance) {
                        val groundAlert = SafetyAlert(
                            level = SafetyLevel.DANGER,
                            message = "AIRCRAFT GROUNDED: ${ac.name} has reached its ${ac.maintenanceIntervalHours}h maintenance limit (${"%.1f".format(newTotalHours)}h total).",
                            timestamp = now
                        )
                        _uiState.update { it.copy(safetyAlerts = it.safetyAlerts + groundAlert) }
                    }
                }
            }
            
            // 4. Clear load from active list
            val updatedLoads = uiState.value.activeLoads.filter { it.id != load.id }
            _uiState.update { it.copy(activeLoads = updatedLoads) }
        }
    }

    private fun loadWeatherData() {
        viewModelScope.launch {
            // Mock weather data fetch
            val data = WeatherData(
                temperature = 78.0,
                windSpeedKts = 18.0, // High wind for demo
                windDirectionDeg = 180,
                gustKts = 22.0,
                visibilityMiles = 15.0,
                cloudBaseFt = 12500,
                condition = "Scattered Clouds",
                pressureInHg = 29.98,
                humidity = 45,
                source = uiState.value.selectedWeatherSource
            )
            _uiState.update { it.copy(weatherData = data) }
            checkWeatherSafety(data)
        }
    }

    private fun checkWeatherSafety(weather: WeatherData) {
        val user = uiState.value.user ?: return
        val studentLimit = user.studentWindLimitKts
        val tandemLimit = user.windLimitKts
        val now = com.V2Skydivejump.app.TimeUtils.nowEpochMillis()

        val newAlerts = mutableListOf<SafetyAlert>()

        if (weather.windSpeedKts > studentLimit || weather.gustKts > studentLimit) {
            newAlerts.add(SafetyAlert(
                level = SafetyLevel.CAUTION,
                message = "STUDENT HOLD: Winds (${weather.windSpeedKts.toInt()} kts) exceed student limit of ${studentLimit.toInt()} kts.",
                timestamp = now
            ))
            _uiState.update { it.copy(operationalStatus = DzOperationalStatus.STUDENT_HOLD) }
        }

        if (weather.windSpeedKts > tandemLimit || weather.gustKts > tandemLimit) {
            newAlerts.add(SafetyAlert(
                level = SafetyLevel.DANGER,
                message = "WEATHER HOLD: Winds (${weather.windSpeedKts.toInt()} kts) exceed operational safety limits.",
                timestamp = now
            ))
            _uiState.update { it.copy(operationalStatus = DzOperationalStatus.WEATHER_HOLD) }
        }

        if (newAlerts.isNotEmpty()) {
            _uiState.update { it.copy(safetyAlerts = newAlerts + it.safetyAlerts) }
        } else {
            _uiState.update { it.copy(operationalStatus = DzOperationalStatus.OPEN) }
        }
    }

    fun setWeatherSource(source: WeatherSource) {
        _uiState.update { it.copy(selectedWeatherSource = source) }
        loadWeatherData()
    }

    private fun loadCfoAnalytics() {
        viewModelScope.launch {
            // No mock analytics for clean start
            _uiState.update { it.copy(
                customerClvMetrics = emptyList(),
                gearRoiMetrics = emptyList(),
                pricingSuggestions = emptyList()
            ) }
        }
    }

    private fun checkFollowingStatus(targetId: String) {
        viewModelScope.launch {
            val status = userRepository.isFollowing(targetId)
            _uiState.update { it.copy(isFollowing = status) }
        }
    }

    fun toggleFollow() {
        val dzId = targetDzId.value ?: return
        viewModelScope.launch {
            userRepository.toggleFollow(dzId)
            checkFollowingStatus(dzId)
        }
    }

    fun toggleStudentSkill(skill: StudentSkillEntity) {
        viewModelScope.launch {
            database.studentSkillDao().insertSkill(skill)
            professionalRepository.syncStudentSkill(skill)
        }
    }

    fun shareEvent(title: String, desc: String) { /* TODO */ }
    fun likePost(post: FeedPostEntity) { /* TODO */ }
    fun addComment(postId: Long, text: String) { /* TODO */ }
    fun getCommentsForPost(postId: Long): Flow<List<FeedCommentEntity>> = flowOf(emptyList())
}
