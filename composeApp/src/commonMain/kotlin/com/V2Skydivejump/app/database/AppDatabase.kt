package com.V2Skydivejump.app.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.V2Skydivejump.app.database.dao.*
import com.V2Skydivejump.app.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        JumpLogEntity::class,
        AircraftEntity::class,
        LedgerEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class,
        DropzoneEntity::class,
        SkydivingEvent::class,
        UserGearEntity::class,
        UserPrivacySettingsEntity::class,
        GearListingEntity::class,
        UserRatingEntity::class,
        UserFederationEntity::class,
        ChatMessageEntity::class,
        NotificationEntity::class,
        UserFollowEntity::class,
        FeedPostEntity::class,
        FeedCommentEntity::class,
        FlightScheduleEntity::class,
        DzStaffMembershipEntity::class,
        DzFacilityEntity::class,
        DzInventoryEntity::class,
        DzRatingEntity::class,
        DzWaiverEntity::class,
        JumperWaiverSignatureEntity::class,
        PromotionEntity::class,
        UserLoyaltyEntity::class,
        ReferralEntity::class,
        SeasonLeaderboardEntry::class,
        IncidentReportEntity::class,
        StudentSkillEntity::class
    ],
    version = 59
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun jumpLogDao(): JumpLogDao
    abstract fun aircraftDao(): AircraftDao
    abstract fun flightScheduleDao(): FlightScheduleDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun badgeDao(): BadgeDao
    abstract fun dropzoneDao(): DropzoneDao
    abstract fun eventDao(): EventDao
    abstract fun userGearDao(): UserGearDao
    abstract fun privacyDao(): PrivacyDao
    abstract fun gearListingDao(): GearListingDao
    abstract fun ratingDao(): UserRatingDao
    abstract fun federationDao(): UserFederationDao
    abstract fun chatDao(): ChatDao
    abstract fun notificationDao(): NotificationDao
    abstract fun followDao(): UserFollowDao
    abstract fun feedPostDao(): FeedPostDao
    abstract fun dzStaffMembershipDao(): DzStaffMembershipDao
    abstract fun dzFacilityDao(): DzFacilityDao
    abstract fun dzInventoryDao(): DzInventoryDao
    abstract fun dzRatingDao(): DzRatingDao
    abstract fun dzWaiverDao(): DzWaiverDao
    abstract fun promotionDao(): PromotionDao
    abstract fun userLoyaltyDao(): UserLoyaltyDao
    abstract fun studentSkillDao(): StudentSkillDao
    abstract fun incidentReportDao(): IncidentReportDao
}






// The Room compiler generates the implementation of this constructor.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

internal const val DB_FILE_NAME = "skydive_jump.db"
