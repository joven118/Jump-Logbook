package com.V2Skydivejump.app.ui.jumper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.database.AppDatabase
import com.V2Skydivejump.app.database.entities.*
import com.V2Skydivejump.app.ui.social.ShareSelection
import com.V2Skydivejump.app.utils.BadgeEvaluationEngine
import com.V2Skydivejump.app.utils.CurrencyStatus
import com.V2Skydivejump.app.utils.MediaUploadService
import com.V2Skydivejump.app.utils.PerformanceAnalyticsEngine
import com.V2Skydivejump.app.utils.CountryUtils
import io.github.jan.supabase.postgrest.postgrest
import com.V2Skydivejump.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DzBookingGroup(
    val dzId: String,
    val dzName: String,
    val country: String,
    val flagEmoji: String,
    val detailedLocation: String,
    val schedules: List<FlightScheduleBookingInfo>
)

data class FlightScheduleBookingInfo(
    val scheduleId: Long,
    val aircraftType: String,
    val loadNumber: Int,
    val maxJumpers: Int,
    val exitAltitude: String,
    val availableSlots: Int,
    val isBooked: Boolean
)

data class JumperUiState(
    val user: UserEntity? = null,
    val jumps: List<JumpLogEntity> = emptyList(),
    val currencyStatus: CurrencyStatus = CurrencyStatus.RED,
    val canopyAlert: String = "",
    val earnedBadgeIds: Set<String> = emptySet(),
    val dropzones: List<DropzoneEntity> = emptyList(),
    val events: List<SkydivingEvent> = emptyList(),
    val userGear: List<UserGearEntity> = emptyList(),
    val marketplaceListings: List<GearListingEntity> = emptyList(),
    val privacySettings: UserPrivacySettingsEntity = UserPrivacySettingsEntity("me"),
    val userRatings: List<UserRatingEntity> = emptyList(),
    val userFederations: List<UserFederationEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadNotifications: Int = 0,
    val chatMessages: Map<String, List<ChatMessageEntity>> = emptyMap(),
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val transactions: List<LedgerEntity> = emptyList(),
    val feedPosts: List<FeedPostEntity> = emptyList(),
    val registeredUsers: List<UserEntity> = emptyList(),
    val allBadges: List<BadgeEntity> = emptyList(),
    val userBadges: List<UserBadgeEntity> = emptyList(),
    val flightSchedules: List<FlightScheduleEntity> = emptyList(),
    val aircrafts: List<AircraftEntity> = emptyList(),
    val isPro: Boolean = false,
    val convenienceFeeType: String = "FIXED",
    val convenienceFeeRate: Double = 0.0,
    val dzInventory: List<DzInventoryEntity> = emptyList(),
    val promotions: List<PromotionEntity> = emptyList(),
    val bookingGroups: List<DzBookingGroup> = emptyList(),
    val selectedScheduleId: Long? = null,
    val currentRentalSelection: List<DzInventoryEntity> = emptyList(),
    val currentRentalTotal: Double = 0.0,
    val wingLoad: Double? = null,
    val totalFreefallTimeHms: String = "00:00:00",
    val leaderboardEntries: List<SeasonLeaderboardEntry> = emptyList(),
    val studentSkills: List<StudentSkillEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class JumperViewModel(
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val jumpLogRepository: JumpLogRepository,
    private val gearRepository: GearRepository,
    private val badgeRepository: BadgeRepository,
    private val professionalRepository: ProfessionalRepository,
    private val offlineSyncRepository: OfflineSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JumperUiState())
    val uiState: StateFlow<JumperUiState> = _uiState.asStateFlow()

    init {
        observeData()
        observeSchedules()
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.currentUser.collectLatest { user ->
                if (user == null) {
                    _uiState.update { it.copy(user = null, isLoading = false) }
                    return@collectLatest
                }
                
                val userId = user.userId
                
                launch {
                    try {
                        val remoteJumps: List<JumpLogEntity> = jumpLogRepository.getJumps()
                        remoteJumps.forEach { jump -> database.jumpLogDao().insertJump(jump) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Could not refresh jump logs from cloud.") }
                    }
                }

                launch {
                    try {
                        val remoteUsers = userRepository.getAllRegisteredUsers()
                        remoteUsers.forEach { registeredUser -> database.userDao().insertUser(registeredUser) }

                        val remoteInventory = userRepository.getAllDzInventory()
                        remoteInventory.forEach { inventoryItem -> database.dzInventoryDao().insertInventory(inventoryItem) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Could not refresh registered users and DZ inventory.") }
                    }
                }
                
                combine(
                    database.jumpLogDao().getJumpsForUser(userId),
                    database.badgeDao().getUserBadges(userId),
                    database.dropzoneDao().getAllDropzones(),
                    database.eventDao().getAllEvents(),
                    database.userGearDao().getUserGear(userId),
                    database.gearListingDao().getAllListings(),
                    database.ratingDao().getRatingsForUser(userId),
                    database.federationDao().getFederationsForUser(userId),
                    database.notificationDao().getNotifications(userId),
                    database.followDao().getFollowerCount(userId),
                    database.followDao().getFollowingCount(userId),
                    database.ledgerDao().getTransactionsByUserId(userId),
                    database.feedPostDao().getAllPosts(),
                    database.userDao().getAllUsers(),
                    database.flightScheduleDao().getAllSchedules(),
                    database.userDao().getDzos(),
                    database.dzInventoryDao().getAllInventory(),
                    database.promotionDao().getActivePromotions("") // Mock: empty string for 'global' or handle per DZ
                ) { array ->
                    try {
                        val jumps = (array[0] as? List<JumpLogEntity>) ?: emptyList()
                        val userBadges = (array[1] as? List<UserBadgeEntity>) ?: emptyList()
                        val dropzones = (array[2] as? List<DropzoneEntity>) ?: emptyList()
                        val events = (array[3] as? List<SkydivingEvent>) ?: emptyList()
                        val gear = (array[4] as? List<UserGearEntity>) ?: emptyList()
                        val listings = (array[5] as? List<GearListingEntity>) ?: emptyList()
                        val ratings = (array[6] as? List<UserRatingEntity>) ?: emptyList()
                        val federations = (array[7] as? List<UserFederationEntity>) ?: emptyList()
                        val notifications = (array[8] as? List<NotificationEntity>) ?: emptyList()
                        val followers = (array[9] as? Int) ?: 0
                        val following = (array[10] as? Int) ?: 0
                        val transactions = (array[11] as? List<LedgerEntity>) ?: emptyList()
                        val feedPosts = (array[12] as? List<FeedPostEntity>) ?: emptyList()
                        val registeredUsers = (array[13] as? List<UserEntity>) ?: emptyList()
                        val allSchedules = (array[14] as? List<FlightScheduleEntity>) ?: emptyList()
                        val dzos = (array[15] as? List<UserEntity>) ?: emptyList()
                        val allInventory = (array[16] as? List<DzInventoryEntity>) ?: emptyList()
                        val promos = (array[17] as? List<PromotionEntity>) ?: emptyList()

                        val userCountry = user.country ?: ""
                        val userRegion = CountryUtils.getRegion(userCountry)

                        val bookingGroups = dzos.mapNotNull { dzo ->
                            val dzSchedules = allSchedules.filter { it.dzId == dzo.userId }
                            if (dzSchedules.isEmpty()) return@mapNotNull null
                            
                            val dzCountry = dzo.dzCountry ?: dzo.country ?: ""
                            val detailedLocation = listOfNotNull(
                                dzo.dzStreet, dzo.dzCity, dzo.dzProvince, dzCountry
                            ).joinToString(", ")

                            DzBookingGroup(
                                dzId = dzo.userId,
                                dzName = dzo.dzName ?: dzo.name,
                                country = dzCountry,
                                flagEmoji = CountryUtils.getFlagEmoji(dzCountry),
                                detailedLocation = detailedLocation,
                                schedules = dzSchedules.map { sched ->
                                    val aircraft = allInventory.find { it.id == sched.aircraftId }
                                    val bookedCount = sched.bookedJumperIds.split(",").filter { it.isNotBlank() }.size
                                    FlightScheduleBookingInfo(
                                        scheduleId = sched.scheduleId,
                                        aircraftType = sched.aircraftType,
                                        loadNumber = sched.loadNumber,
                                        maxJumpers = sched.loadCapacity,
                                        exitAltitude = aircraft?.normalJumpAltitude ?: "N/A",
                                        availableSlots = sched.loadCapacity - bookedCount,
                                        isBooked = user.userId in sched.bookedJumperIds.split(",")
                                    )
                                }
                            )
                        }.sortedWith(
                            compareBy<DzBookingGroup> { it.country != userCountry }
                            .thenBy { CountryUtils.getRegionProximityScore(userRegion, CountryUtils.getRegion(it.country)) }
                            .thenBy { it.country }
                        )

                        val consolidatedBadges = (BadgeEvaluationEngine.MILSTONE_BADGES + 
                            BadgeEvaluationEngine.FS_BADGES + 
                            BadgeEvaluationEngine.TRAVEL_BADGES + 
                            BadgeEvaluationEngine.SPECIAL_TRAVEL_ACHIEVEMENTS +
                            BadgeEvaluationEngine.MILITARY_BADGES +
                            BadgeEvaluationEngine.SPECIAL_MILITARY_RECORDS +
                            BadgeEvaluationEngine.WINGSUIT_BADGES +
                            BadgeEvaluationEngine.FREEFLY_BADGES +
                            BadgeEvaluationEngine.SAFETY_BADGES +
                            BadgeEvaluationEngine.CANOPY_BADGES +
                            BadgeEvaluationEngine.SPECIAL_ACHIEVEMENTS +
                            BadgeEvaluationEngine.AMBASSADOR_BADGES +
                            BadgeEvaluationEngine.DZO_RECRUITER_BADGES +
                            BadgeEvaluationEngine.COMMUNITY_BUILDER_BADGES
                        ).distinctBy { it.id }

                        val lastJumpDate = jumps.firstOrNull()?.date ?: 0L
                        val currency = PerformanceAnalyticsEngine.calculateCurrency(lastJumpDate, user.licenseNumber)
                        val totalFreefallSeconds = PerformanceAnalyticsEngine.calculateTotalFreefallTime(jumps)
                        val totalHms = PerformanceAnalyticsEngine.formatDurationHms(totalFreefallSeconds)
                        
                        // Empty competitive data for clean start
                        val leaderboard = emptyList<SeasonLeaderboardEntry>()

                        // Empty Skills for clean start
                        val skills = emptyList<StudentSkillEntity>()

                        // Update local user with mock NFC only if we want to force it
                        val userWithNfc = user

                        val currentExitWeight = user.exitWeight
                        val selectedGearIds = user.selectedGearIdsForWeight?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                        val mainCanopy = gear.find { it.gearId in selectedGearIds && it.category == "Main Canopy" }
                        val mainSize = mainCanopy?.sizeSqFt?.toDoubleOrNull()
                        
                        val calculatedWingLoad = if (currentExitWeight != null && mainSize != null && mainSize > 0) {
                            currentExitWeight / mainSize
                        } else null

                        val isProUser = user.membershipTier == "PRO"

                        // Load Global Convenience Fee from Admin Layer (Simulated or via AdminRepository)
                        // In real app, AdminRepository would fetch this from Supabase 'app_config' table
                        val feeType = "FIXED"
                        val feeRate = 1.0 // Default $1.00 per jump

                        JumperUiState(
                            user = userWithNfc,
                            jumps = jumps,
                            isPro = isProUser,
                            convenienceFeeType = feeType,
                            convenienceFeeRate = feeRate,
                            currencyStatus = currency,
                            earnedBadgeIds = userBadges.map { it.badgeId }.toSet(),
                            dropzones = dropzones,
                            events = events,
                            userGear = gear,
                            marketplaceListings = listings,
                            privacySettings = UserPrivacySettingsEntity(user.userId),
                            userRatings = ratings,
                            userFederations = federations,
                            notifications = notifications,
                            unreadNotifications = notifications.count { !it.isRead },
                            followerCount = followers,
                            followingCount = following,
                            transactions = transactions,
                            feedPosts = feedPosts,
                            registeredUsers = registeredUsers,
                            allBadges = consolidatedBadges,
                            userBadges = userBadges,
                            flightSchedules = allSchedules,
                            dzInventory = allInventory,
                            promotions = promos,
                            bookingGroups = bookingGroups,
                            wingLoad = calculatedWingLoad,
                            totalFreefallTimeHms = totalHms,
                            leaderboardEntries = leaderboard,
                            studentSkills = skills,
                            isLoading = false
                        )
                    } catch (e: Exception) {
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Could not load jumper dashboard data."
                        )
                    }
                }.collect { newState ->
                    _uiState.value = newState.copy(errorMessage = _uiState.value.errorMessage)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun addListing(listing: GearListingEntity) {
        viewModelScope.launch {
            try {
                val uploadedPhotoUrls = uploadListingMedia(listing.photoUrls, listing.id, "image")
                val uploadedVideoUrls = uploadListingMedia(listing.videoUrls, listing.id, "video")
                database.gearListingDao().insertListing(
                    listing.copy(
                        photoUrls = uploadedPhotoUrls,
                        videoUrls = uploadedVideoUrls
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Could not upload listing media.") }
            }
        }
    }

    private suspend fun uploadListingMedia(urls: String?, listingId: String, mediaKind: String): String? {
        val mediaUris = urls
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        if (mediaUris.isEmpty()) return null

        val bucket = if (mediaKind == "video") "marketplace-media" else "marketplace-media"
        return mediaUris.mapIndexed { index, uri ->
            MediaUploadService.uploadLocalUri(
                localUri = uri,
                bucketName = bucket,
                entityType = "marketplace_$mediaKind",
                entityId = "${listingId}_$index",
                mediaKind = mediaKind
            ).publicUrl
        }.joinToString(",")
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

    fun updateUserProfile(user: UserEntity) {
        viewModelScope.launch {
            database.userDao().updateUser(user)
            try {
                userRepository.updateProfile(user)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.USER_PROFILE_UPSERT, user)
                _uiState.update { it.copy(errorMessage = "Profile saved offline. It will sync when online.") }
            }
        }
    }

    fun updateProfilePicture(url: String) {
        val user = uiState.value.user ?: return
        _uiState.update { it.copy(user = user.copy(profilePictureUrl = url)) }
    }

    fun updateBackgroundPicture(url: String) {
        val user = uiState.value.user ?: return
        _uiState.update { it.copy(user = user.copy(backgroundPictureUrl = url)) }
    }

    fun uploadProfileMedia(type: String, uri: String) {
        viewModelScope.launch {
            try {
                val publicUrl = userRepository.uploadProfileMedia(type, uri)
                if (type == "profile") {
                    updateProfilePicture(publicUrl)
                } else if (type == "background") {
                    updateBackgroundPicture(publicUrl)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Could not upload profile media.") }
            }
        }
    }

    fun saveGearItem(gear: UserGearEntity) {
        viewModelScope.launch {
            database.userGearDao().insertGear(gear)
            try {
                gearRepository.saveGear(gear)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.USER_GEAR_UPSERT, gear)
            }
        }
    }

    fun deleteGearItem(gear: UserGearEntity) {
        viewModelScope.launch {
            database.userGearDao().deleteGear(gear)
            try {
                gearRepository.deleteGear(gear.gearId)
            } catch (e: Exception) {
                offlineSyncRepository.enqueueRaw(SyncOperationType.USER_GEAR_DELETE, gear.gearId)
            }
        }
    }

    fun addRating(rating: UserRatingEntity) {
        viewModelScope.launch {
            val userId = uiState.value.user?.userId ?: return@launch
            val persistedRating = rating.copy(userId = userId)
            database.ratingDao().insertRating(persistedRating)
            try {
                professionalRepository.saveRating(persistedRating)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.USER_RATING_UPSERT, persistedRating)
            }
        }
    }

    fun deleteRating(rating: UserRatingEntity) {
        viewModelScope.launch {
            database.ratingDao().deleteRating(rating)
            try {
                professionalRepository.deleteRating(rating.id)
            } catch (e: Exception) {
                offlineSyncRepository.enqueueRaw(SyncOperationType.USER_RATING_DELETE, rating.id.toString())
            }
        }
    }

    fun verifyRating(rating: UserRatingEntity, verifierName: String, verifierLicense: String) {
        viewModelScope.launch {
            val updated = rating.copy(
                isVerified = true,
                verifierName = verifierName,
                verifierLicense = verifierLicense,
                verificationDate = TimeUtils.nowEpochMillis()
            )
            database.ratingDao().updateRating(updated)
            try {
                professionalRepository.saveRating(updated)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.USER_RATING_UPSERT, updated)
            }
        }
    }

    fun addFederation(federation: UserFederationEntity) {
        viewModelScope.launch {
            val userId = uiState.value.user?.userId ?: return@launch
            val persistedFederation = federation.copy(userId = userId)
            database.federationDao().insertFederation(persistedFederation)
            try {
                professionalRepository.saveFederation(persistedFederation)
            } catch (e: Exception) {
                offlineSyncRepository.enqueue(SyncOperationType.USER_FEDERATION_UPSERT, persistedFederation)
            }
        }
    }

    fun deleteFederation(federation: UserFederationEntity) {
        viewModelScope.launch {
            database.federationDao().deleteFederation(federation)
            try {
                professionalRepository.deleteFederation(federation.id)
            } catch (e: Exception) {
                offlineSyncRepository.enqueueRaw(SyncOperationType.USER_FEDERATION_DELETE, federation.id.toString())
            }
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            database.notificationDao().markAsRead(id)
        }
    }

    fun sendMessage(recipientId: String, text: String) {
        viewModelScope.launch {
            val currentUserId = uiState.value.user?.userId ?: return@launch
            database.chatDao().sendMessage(
                ChatMessageEntity(
                    senderId = currentUserId,
                    receiverId = recipientId,
                    message = text,
                    timestamp = TimeUtils.nowEpochMillis()
                )
            )
        }
    }

    fun getChatMessages(otherId: String): Flow<List<ChatMessageEntity>> {
        val currentUserId = uiState.value.user?.userId ?: return flowOf(emptyList())
        return database.chatDao().getMessages(currentUserId, otherId)
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = uiState.value.user?.userId ?: return@launch
            if (currentUserId == targetUserId) return@launch

            val isFollowingLocal = database.followDao().isFollowingOnce(currentUserId, targetUserId)
            if (isFollowingLocal) {
                database.followDao().unfollow(UserFollowEntity(currentUserId, targetUserId, 0L))
            } else {
                database.followDao().follow(UserFollowEntity(currentUserId, targetUserId, TimeUtils.nowEpochMillis()))
            }
            try {
                userRepository.toggleFollow(targetUserId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Follow change was saved locally but did not sync yet.") }
            }
        }
    }

    fun isFollowing(targetUserId: String): Flow<Boolean> {
        val currentUserId = uiState.value.user?.userId ?: return flowOf(false)
        return database.followDao().isFollowing(currentUserId, targetUserId)
    }

    fun addFunds(amount: Double, method: String) {
        viewModelScope.launch {
            val user = uiState.value.user ?: return@launch
            val newBalance = user.walletBalance + amount
            database.userDao().updateWalletBalance(user.userId, newBalance)
            database.ledgerDao().insertTransaction(
                LedgerEntity(
                    userId = user.userId,
                    category = "Funds Added ($method)",
                    amount = amount,
                    timestamp = TimeUtils.nowEpochMillis()
                )
            )
        }
    }

    fun addJump(jump: JumpLogEntity) {
        viewModelScope.launch {
            try {
                database.jumpLogDao().insertJump(jump)
                jumpLogRepository.insertJumpLog(jump)
            } catch (e: Exception) { }
            resequenceJumps(jump.userId)
        }
    }

    fun deleteJump(jump: JumpLogEntity) {
        viewModelScope.launch {
            database.jumpLogDao().deleteJump(jump)
            try {
                jumpLogRepository.deleteJumpLog(jump.jumpId)
            } catch (e: Exception) { }
            resequenceJumps(jump.userId)
        }
    }

    fun updateJump(jump: JumpLogEntity) {
        viewModelScope.launch {
            database.jumpLogDao().updateJump(jump)
            try {
                jumpLogRepository.updateJumpLog(jump)
            } catch (e: Exception) { }
            resequenceJumps(jump.userId)
        }
    }

    fun shareJump(jump: JumpLogEntity, selection: ShareSelection = ShareSelection()) {
        viewModelScope.launch {
            val user = uiState.value.user ?: return@launch
            val contentParts = mutableListOf<String>()
            contentParts.add("Just logged Jump #${jump.jumpNumber}!")
            val post = FeedPostEntity(
                userId = user.userId,
                userName = user.name ?: "Unknown Jumper",
                userRole = "JUMPER",
                content = contentParts.joinToString(" "),
                timestamp = TimeUtils.nowEpochMillis(),
                type = "JUMP"
            )
            try {
                database.feedPostDao().insertPost(post)
                SupabaseManager.client.postgrest.from("feed_posts").insert(post)
            } catch (e: Exception) { }
        }
    }

    fun likePost(post: FeedPostEntity) {
        viewModelScope.launch {
            database.feedPostDao().updatePost(post.copy(likes = post.likes + 1))
        }
    }

    fun addComment(postId: Long, text: String) {
        viewModelScope.launch {
            val user = uiState.value.user ?: return@launch
            database.feedPostDao().insertComment(
                FeedCommentEntity(
                    postId = postId,
                    userId = user.userId,
                    userName = user.name,
                    content = text,
                    timestamp = TimeUtils.nowEpochMillis()
                )
            )
        }
    }

    fun getCommentsForPost(postId: Long): Flow<List<FeedCommentEntity>> {
        return database.feedPostDao().getCommentsForPost(postId)
    }

    fun claimMilitaryBadge(badgeId: String, documentUrl: String) {
        viewModelScope.launch {
            val user = uiState.value.user ?: return@launch
            database.badgeDao().awardBadge(
                UserBadgeEntity(
                    userId = user.userId,
                    badgeId = badgeId,
                    dateEarned = TimeUtils.nowEpochMillis(),
                    verificationStatus = BadgeVerificationStatus.PENDING,
                    supportingDocumentUrl = documentUrl
                )
            )
            try {
                badgeRepository.claimBadge(badgeId, documentUrl)
            } catch (e: Exception) { }
        }
    }

    private suspend fun resequenceJumps(userId: String) {
        val allJumps = database.jumpLogDao().getJumpsForUserOnce(userId)
        val sortedJumps = allJumps.sortedWith(compareBy<JumpLogEntity> { it.date }.thenBy { it.jumpId })
        sortedJumps.forEachIndexed { index, jump ->
            val newNumber = index + 1
            if (jump.jumpNumber != newNumber) {
                database.jumpLogDao().updateJump(jump.copy(jumpNumber = newNumber))
            }
        }
    }

    private fun observeSchedules() {
        viewModelScope.launch {
            database.flightScheduleDao().getAllSchedules().collect { schedules ->
                _uiState.update { it.copy(flightSchedules = schedules) }
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
                
                // CRITICAL FIX: Sync booking to Supabase so DZO sees it
                try {
                    professionalRepository.updateFlightSchedule(updatedSchedule)
                } catch (e: Exception) {
                    println("JumperViewModel Error: Booking sync failed: ${e.message}")
                }
            }
        }
    }

    fun selectScheduleForRental(scheduleId: Long) {
        _uiState.update { it.copy(selectedScheduleId = scheduleId) }
    }

    fun finalizeBookingWithRental(items: List<DzInventoryEntity>, total: Double) {
        _uiState.update { it.copy(currentRentalSelection = items, currentRentalTotal = total) }
    }

    fun toggleStudentSkill(skill: StudentSkillEntity) {
        viewModelScope.launch {
            database.studentSkillDao().insertSkill(skill)
            // Local update for immediate UI feedback
            _uiState.update { state ->
                val updatedSkills = state.studentSkills.toMutableList()
                val index = updatedSkills.indexOfFirst { it.skillId == skill.skillId }
                if (index != -1) updatedSkills[index] = skill else updatedSkills.add(skill)
                state.copy(studentSkills = updatedSkills)
            }
        }
    }

    fun confirmBooking(appliedPromo: PromotionEntity?, finalTotal: Double, pointsToEarn: Int) {
        val scheduleId = uiState.value.selectedScheduleId ?: return
        val user = uiState.value.user ?: return
        val rentalItems = uiState.value.currentRentalSelection

        viewModelScope.launch {
            // Actual booking logic
            bookFlight(scheduleId)

            // Update loyalty points
            val currentLoyalty = database.userLoyaltyDao().getUserLoyalty(user.userId).firstOrNull()
            val updatedPoints = (currentLoyalty?.totalPoints ?: 0) + pointsToEarn
            val updatedLifetime = (currentLoyalty?.lifetimePoints ?: 0) + pointsToEarn
            database.userLoyaltyDao().insertLoyalty(
                UserLoyaltyEntity(
                    userId = user.userId,
                    totalPoints = updatedPoints,
                    lifetimePoints = updatedLifetime,
                    lastUpdated = TimeUtils.nowEpochMillis()
                )
            )

            // Update promo redemptions if applicable
            if (appliedPromo != null) {
                database.promotionDao().updatePromotion(appliedPromo.copy(currentRedemptions = appliedPromo.currentRedemptions + 1))
            }

            // Reserve gear
            val updatedInventory = uiState.value.dzInventory.map { gear ->
                if (rentalItems.any { it.id == gear.id }) {
                    gear.copy(rentalStatus = "Reserved")
                } else gear
            }
            _uiState.update { it.copy(dzInventory = updatedInventory, selectedScheduleId = null, currentRentalSelection = emptyList()) }
        }
    }
}
