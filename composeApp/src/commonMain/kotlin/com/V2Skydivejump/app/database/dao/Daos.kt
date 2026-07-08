package com.V2Skydivejump.app.database.dao

import androidx.room.*
import com.V2Skydivejump.app.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET walletBalance = :newBalance WHERE userId = :userId")
    suspend fun updateWalletBalance(userId: String, newBalance: Double)

    @Query("SELECT * FROM users WHERE role = 'DZ_OPERATOR'")
    fun getDzos(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE role != 'DZ_OPERATOR'")
    suspend fun deleteAllExceptDzos()
}


@Dao
interface JumpLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJump(jump: JumpLogEntity)

    @Update
    suspend fun updateJump(jump: JumpLogEntity)

    @Delete
    suspend fun deleteJump(jump: JumpLogEntity)

    @Query("SELECT * FROM jump_logs ORDER BY date DESC")
    fun getAllJumps(): Flow<List<JumpLogEntity>>

    @Query("SELECT * FROM jump_logs WHERE userId = :userId ORDER BY date DESC")
    fun getJumpsForUser(userId: String): Flow<List<JumpLogEntity>>

    @Query("SELECT * FROM jump_logs WHERE userId = :userId")
    suspend fun getJumpsForUserOnce(userId: String): List<JumpLogEntity>

    @Query("DELETE FROM jump_logs")
    suspend fun deleteAllJumps()
}

@Dao
interface AircraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAircraft(aircraft: AircraftEntity)

    @Update
    suspend fun updateAircraft(aircraft: AircraftEntity)

    @Delete
    suspend fun deleteAircraft(aircraft: AircraftEntity)

    @Query("SELECT * FROM aircrafts")
    fun getAllAircrafts(): Flow<List<AircraftEntity>>

    @Query("SELECT * FROM aircrafts WHERE dzId = :dzId")
    fun getAircraftsForDz(dzId: String): Flow<List<AircraftEntity>>

    @Query("DELETE FROM aircrafts")
    suspend fun deleteAllAircrafts()
}


@Dao
interface FlightScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: FlightScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: FlightScheduleEntity)

    @Query("SELECT * FROM flight_schedules WHERE dzId = :dzId ORDER BY dateOfFlight ASC, loadNumber ASC")
    fun getSchedulesForDz(dzId: String): Flow<List<FlightScheduleEntity>>

    @Query("SELECT * FROM flight_schedules WHERE scheduleId = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): FlightScheduleEntity?

    @Query("SELECT * FROM flight_schedules")
    fun getAllSchedules(): Flow<List<FlightScheduleEntity>>

    @Update
    suspend fun updateSchedule(schedule: FlightScheduleEntity)

    @Query("DELETE FROM flight_schedules")
    suspend fun deleteAllSchedules()
}


@Dao
interface LedgerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerEntity)

    @Update
    suspend fun updateTransaction(transaction: LedgerEntity)

    @Delete
    suspend fun deleteTransaction(transaction: LedgerEntity)

    @Query("SELECT * FROM ledger ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledger WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<LedgerEntity>>

    @Query("DELETE FROM ledger")
    suspend fun deleteAllTransactions()
}

@Dao
interface UserRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: UserRatingEntity)

    @Update
    suspend fun updateRating(rating: UserRatingEntity)

    @Delete
    suspend fun deleteRating(rating: UserRatingEntity)

    @Query("SELECT * FROM user_ratings WHERE userId = :userId")
    fun getRatingsForUser(userId: String): Flow<List<UserRatingEntity>>
}

@Dao
interface UserFederationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFederation(federation: UserFederationEntity)

    @Update
    suspend fun updateFederation(federation: UserFederationEntity)

    @Delete
    suspend fun deleteFederation(federation: UserFederationEntity)

    @Query("SELECT * FROM user_federations WHERE userId = :userId")
    fun getFederationsForUser(userId: String): Flow<List<UserFederationEntity>>
}

@Dao
interface ChatDao {
    @Insert
    suspend fun sendMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getMessages(userId: String, otherId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
}

@Dao
interface NotificationDao {
    @Insert
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}

@Dao
interface UserFollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun follow(follow: UserFollowEntity)

    @Delete
    suspend fun unfollow(follow: UserFollowEntity)

    @Query("SELECT COUNT(*) FROM user_follows WHERE followedId = :userId")
    fun getFollowerCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_follows WHERE followerId = :userId")
    fun getFollowingCount(userId: String): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM user_follows WHERE followerId = :followerId AND followedId = :followedId)")
    fun isFollowing(followerId: String, followedId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM user_follows WHERE followerId = :followerId AND followedId = :followedId)")
    suspend fun isFollowingOnce(followerId: String, followedId: String): Boolean
}

@Dao
interface FeedPostDao {
    @Insert
    suspend fun insertPost(post: FeedPostEntity)

    @Query("SELECT * FROM feed_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<FeedPostEntity>>

    @Update
    suspend fun updatePost(post: FeedPostEntity)

    @Insert
    suspend fun insertComment(comment: FeedCommentEntity)

    @Query("SELECT * FROM feed_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<FeedCommentEntity>>
}

@Dao
interface DzStaffMembershipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: DzStaffMembershipEntity)

    @Update
    suspend fun updateMembership(membership: DzStaffMembershipEntity)

    @Delete
    suspend fun deleteMembership(membership: DzStaffMembershipEntity)

    @Query("SELECT * FROM dz_staff_memberships WHERE dzId = :dzId")
    fun getStaffForDz(dzId: String): Flow<List<DzStaffMembershipEntity>>

    @Query("SELECT * FROM dz_staff_memberships WHERE userId = :userId")
    fun getMembershipsForUser(userId: String): Flow<List<DzStaffMembershipEntity>>

    @Query("DELETE FROM dz_staff_memberships")
    suspend fun deleteAllMemberships()
}

@Dao
interface DzFacilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacility(facility: DzFacilityEntity)

    @Update
    suspend fun updateFacility(facility: DzFacilityEntity)

    @Delete
    suspend fun deleteFacility(facility: DzFacilityEntity)

    @Query("SELECT * FROM dz_facilities WHERE dzId = :dzId")
    fun getFacilitiesForDz(dzId: String): Flow<List<DzFacilityEntity>>

    @Query("DELETE FROM dz_facilities")
    suspend fun deleteAllFacilities()
}

@Dao
interface DzInventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: DzInventoryEntity)

    @Update
    suspend fun updateInventory(inventory: DzInventoryEntity)

    @Delete
    suspend fun deleteInventory(inventory: DzInventoryEntity)

    @Query("SELECT * FROM dz_inventory WHERE dzId = :dzId")
    fun getInventoryForDz(dzId: String): Flow<List<DzInventoryEntity>>

    @Query("SELECT * FROM dz_inventory")
    fun getAllInventory(): Flow<List<DzInventoryEntity>>

    @Query("DELETE FROM dz_inventory")
    suspend fun deleteAllInventory()
}


@Dao
interface DzRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: DzRatingEntity)

    @Query("SELECT * FROM dz_ratings WHERE dzId = :dzId ORDER BY timestamp DESC")
    fun getRatingsForDz(dzId: String): Flow<List<DzRatingEntity>>

    @Query("SELECT AVG(stars) FROM dz_ratings WHERE dzId = :dzId")
    fun getAverageRatingForDz(dzId: String): Flow<Double?>

    @Query("DELETE FROM dz_ratings")
    suspend fun deleteAllRatings()
}

@Dao
interface DzWaiverDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaiver(waiver: DzWaiverEntity)

    @Update
    suspend fun updateWaiver(waiver: DzWaiverEntity)

    @Query("SELECT * FROM dz_waivers WHERE dzId = :dzId AND isActive = 1")
    fun getActiveWaiversForDz(dzId: String): Flow<List<DzWaiverEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignature(signature: JumperWaiverSignatureEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM jumper_waiver_signatures WHERE userId = :userId AND dzId = :dzId AND waiverId = :waiverId)")
    suspend fun hasSignedWaiver(userId: String, dzId: String, waiverId: Long): Boolean

    @Query("DELETE FROM dz_waivers")
    suspend fun deleteAllWaivers()

    @Query("DELETE FROM jumper_waiver_signatures")
    suspend fun deleteAllSignatures()
}

@Dao
interface StudentSkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: StudentSkillEntity)

    @Query("SELECT * FROM student_skills WHERE userId = :userId")
    fun getSkillsForUser(userId: String): Flow<List<StudentSkillEntity>>

    @Query("SELECT * FROM student_skills WHERE userId = :userId AND category = :category")
    fun getSkillsForUserByCategory(userId: String, category: String): Flow<List<StudentSkillEntity>>

    @Query("DELETE FROM student_skills")
    suspend fun deleteAllSkills()
}

@Dao
interface UserLoyaltyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyalty(loyalty: UserLoyaltyEntity)

    @Query("SELECT * FROM user_loyalty WHERE userId = :userId")
    fun getUserLoyalty(userId: String): Flow<UserLoyaltyEntity?>
}

@Dao
interface IncidentReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: IncidentReportEntity)

    @Query("SELECT * FROM incident_reports WHERE dzId = :dzId ORDER BY date DESC")
    fun getReportsForDz(dzId: String): Flow<List<IncidentReportEntity>>

    @Query("DELETE FROM incident_reports")
    suspend fun deleteAllReports()
}

@Dao
interface PromotionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: PromotionEntity)

    @Update
    suspend fun updatePromotion(promotion: PromotionEntity)

    @Query("SELECT * FROM promotions WHERE dzId = :dzId AND isActive = 1")
    fun getActivePromotions(dzId: String): Flow<List<PromotionEntity>>

    @Query("SELECT * FROM promotions WHERE code = :code AND isActive = 1")
    suspend fun getPromotionByCode(code: String): PromotionEntity?

    @Query("DELETE FROM promotions")
    suspend fun deleteAllPromotions()
}






