package com.V2Skydivejump.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey @SerialName("user_id") val userId: String,
    val name: String = "",
    @SerialName("screen_name") val screenName: String? = null,
    @SerialName("license_number") val licenseNumber: String = "",
    val role: String = "JUMPER", // "JUMPER", "DZ_OPERATOR", "INSTRUCTOR"
    @SerialName("base_jump_count") val baseJumpCount: Int = 0,
    @SerialName("exit_weight") val exitWeight: Double? = null,
    @SerialName("selected_gear_ids_for_weight") val selectedGearIdsForWeight: String? = null, // Comma separated IDs of gear included in weight calc
    @SerialName("wallet_balance") val walletBalance: Double = 0.0,
    @SerialName("profile_picture_url") val profilePictureUrl: String? = null,
    @SerialName("background_picture_url") val backgroundPictureUrl: String? = null,
    val birthdate: Long? = null,
    val nationality: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val weight: Double? = null,
    @SerialName("weight_unit") val weightUnit: String? = "kg",
    val gender: String? = null,
    @SerialName("mobile_number") val mobileNumber: String? = null,
    val email: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("emergency_contact_name") val emergencyContactName: String? = null,
    @SerialName("emergency_contact_number") val emergencyContactNumber: String? = null,
    @SerialName("dz_name") val dzName: String? = null,
    @SerialName("dz_location") val dzLocation: String? = null,
    @SerialName("dz_street") val dzStreet: String? = null,
    @SerialName("dz_city") val dzCity: String? = null,
    @SerialName("dz_province") val dzProvince: String? = null,
    @SerialName("dz_country") val dzCountry: String? = null,
    @SerialName("dz_mobile_number") val dzMobileNumber: String? = null,
    @SerialName("dz_email") val dzEmail: String? = null,
    @SerialName("operating_days") val operatingDays: String? = null,
    @SerialName("operating_hours") val operatingHours: String? = null,
    @SerialName("dz_website") val dzWebsite: String? = null,
    @SerialName("referral_code") val referralCode: String? = null,
    @SerialName("nfc_tag_id") val nfcTagId: String? = null,
    @SerialName("wind_limit_kts") val windLimitKts: Double = 25.0,
    @SerialName("student_wind_limit_kts") val studentWindLimitKts: Double = 15.0,
    @SerialName("rigger_seal_symbol") val riggerSealSymbol: String? = null,
    @SerialName("rigger_license") val riggerLicense: String? = null,
    @SerialName("membership_level") val membershipLevel: String = "Standard", // Standard, Silver, Gold, Platinum
    @SerialName("membership_tier") val membershipTier: String = "STANDARD", // STANDARD, PRO
    @SerialName("subscription_expiry") val subscriptionExpiry: Long? = null
)

@Serializable
@Entity(tableName = "dz_staff_memberships", primaryKeys = ["dzId", "userId"])
data class DzStaffMembershipEntity(
    @SerialName("dz_id") val dzId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("staff_role") val staffRole: String, // e.g., "Pilot", "Instructor", "Manifest"
    @SerialName("is_active") val isActive: Boolean = true
)



@Serializable
@Entity(tableName = "jump_logs")
data class JumpLogEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("jump_id") val jumpId: Long = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("jump_number") val jumpNumber: Int,
    val date: Long, // Timestamp
    @SerialName("dz_id") val dzId: String? = null,
    @SerialName("dz_name") val dzName: String? = "",
    @SerialName("dz_location") val dzLocation: String? = "",
    val country: String? = "",
    val location: String? = "", // Kept for legacy/combined name if needed
    val aircraft: String? = "", // Kept for backward compatibility or as generic name
    @SerialName("aircraft_type") val aircraftType: String? = "",
    @SerialName("aircraft_tail_number") val aircraftTailNumber: String? = null,
    
    // Telemetry
    @SerialName("exit_altitude_agl") val exitAltitudeAgl: Int = 0,
    @SerialName("deployment_altitude_agl") val deploymentAltitudeAgl: Int = 0,
    @SerialName("freefall_time_seconds") val freefallTimeSeconds: Int = 0,
    @SerialName("max_speed_mph") val maxSpeedMph: Int = 0,
    @SerialName("average_speed_mph") val averageSpeedMph: Int = 0,
    @SerialName("canopy_time_seconds") val canopyTimeSeconds: Int = 0,
    
    // Details
    @SerialName("jump_type") val jumpType: String? = "Fun Jump",
    val disciplines: String? = "", // Comma separated list
    @SerialName("landing_styles") val landingStyles: String? = "", // Comma separated list
    @SerialName("weather_condition") val weatherCondition: String? = "",
    
    // Equipment
    @SerialName("gear_id") val gearId: String? = null,
    @SerialName("main_canopy_id") val mainCanopyId: String? = null,
    @SerialName("reserve_canopy_id") val reserveCanopyId: String? = null,
    @SerialName("container_id") val containerId: String? = null,
    @SerialName("helmet_id") val helmetId: String? = null,
    @SerialName("altimeter_id") val altimeterId: String? = null,
    @SerialName("parts_id") val partsId: String? = null,
    @SerialName("camera_id") val cameraId: String? = null,
    @SerialName("suit_id") val suitId: String? = null,
    @SerialName("other_gear_id") val otherGearId: String? = null,
    
    // Media
    @SerialName("photo_urls") val photoUrls: String? = "", // Comma separated Full-Res URLs
    @SerialName("thumbnail_urls") val thumbnailUrls: String? = "", // Comma separated Thumbnail URLs
    @SerialName("video_urls") val videoUrls: String? = "", // Comma separated Video URLs
    
    // Verification
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("verifier_name") val verifierName: String? = null,
    @SerialName("verifier_license") val verifierLicense: String? = null,
    @SerialName("verification_method") val verificationMethod: String? = null, // "SIGNATURE", "QR_CODE", "USER_LOOKUP"
    @SerialName("electronic_signature") val electronicSignature: String? = null, // Base64 or path to signature image
    
    // Team & Notes
    val teammates: String? = "",
    val coach: String? = "",
    val instructor: String? = "",
    val organizer: String? = "",
    @SerialName("jump_notes") val jumpNotes: String? = "",
    @SerialName("self_packed") val selfPacked: Boolean = false
)

@Serializable
@Entity(tableName = "aircrafts")
data class AircraftEntity(
    @PrimaryKey @SerialName("aircraft_id") val aircraftId: String,
    @SerialName("dz_id") val dzId: String,
    @SerialName("aircraft_name") val aircraftName: String,
    @SerialName("tail_number") val tailNumber: String,
    val model: String,
    @SerialName("max_capacity") val maxCapacity: Int,
    @SerialName("max_payload") val maxPayload: Double? = null
)

@Serializable
@Entity(tableName = "flight_schedules")
data class FlightScheduleEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("schedule_id") val scheduleId: Long = 0,
    @SerialName("dz_id") val dzId: String,
    @SerialName("date_of_flight") val dateOfFlight: Long,
    @SerialName("frequency") val frequency: String = "Specific Date", // Monday – Sunday, Weekdays, Weekend, Specific Date
    @SerialName("load_number") val loadNumber: Int,
    @SerialName("aircraft_id") val aircraftId: Long? = null, // Link to DzInventoryEntity id
    @SerialName("aircraft_name") val aircraftName: String,
    @SerialName("aircraft_type") val aircraftType: String,
    @SerialName("aircraft_tail_number") val aircraftTailNumber: String,
    @SerialName("load_capacity") val loadCapacity: Int,
    @SerialName("creation_source") val creationSource: String = "SCHEDULE", // "SCHEDULE" or "MANIFEST"
    @SerialName("booked_jumper_ids") val bookedJumperIds: String = "" // Comma-separated user IDs
)

@Serializable
@Entity(tableName = "ledger")
data class LedgerEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("transaction_id") val transactionId: Long = 0,
    @SerialName("user_id") val userId: String,
    val category: String,
    val amount: Double,
    val timestamp: Long
)

@Serializable
@Entity(tableName = "user_gear")
data class UserGearEntity(
    @PrimaryKey @SerialName("gear_id") val gearId: String, // "me_main", "me_reserve", etc.
    @SerialName("user_id") val userId: String,
    val category: String, // "Main Canopy", "Reserve Canopy", "Container", "Helmet", "Altimeter", "Parts"
    val make: String = "",
    val model: String = "",
    @SerialName("size_sq_ft") val sizeSqFt: String = "",
    @SerialName("size_mlw") val sizeMlw: String = "", // Specifically for Containers
    @SerialName("serial_number") val serialNumber: String = "",
    @SerialName("date_of_manufacture") val dateOfManufacture: String = "",
    val weight: Double? = null,
    @SerialName("weight_unit") val weightUnit: String = "lbs", // "lbs" or "kg"
    @SerialName("is_primary") val isPrimary: Boolean = false,
    @SerialName("repack_card_photo_url") val repackCardPhotoUrl: String? = null,
    @SerialName("seal_symbol_photo_url") val sealSymbolPhotoUrl: String? = null,
    @SerialName("last_repack_date") val lastRepackDate: Long? = null,
    @SerialName("repack_due_date") val repackDueDate: Long? = null
)

@Serializable
@Entity(tableName = "incident_reports")
data class IncidentReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("dz_id") val dzId: String,
    @SerialName("user_id") val userId: String, // Reporter
    @SerialName("jumper_id") val jumperId: String? = null,
    val date: Long,
    val type: String, // MALFUNCTION, OFF_FIELD, GEAR_ISSUE, INJURY
    val description: String,
    @SerialName("gear_id") val gearId: String? = null,
    @SerialName("is_investigated") val isInvestigated: Boolean = false,
    @SerialName("investigation_notes") val investigationNotes: String? = ""
)

@Serializable
@Entity(tableName = "user_ratings")
data class UserRatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("rating_name") val ratingName: String,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("verifier_name") val verifierName: String? = null,
    @SerialName("verifier_license") val verifierLicense: String? = null,
    @SerialName("verification_date") val verificationDate: Long? = null
)

@Serializable
@Entity(tableName = "user_federations")
data class UserFederationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("federation_name") val federationName: String, // e.g., "USPA", "BPA", "APF"
    @SerialName("membership_number") val membershipNumber: String? = null
)

@Serializable
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("sender_id") val senderId: String,
    @SerialName("receiver_id") val receiverId: String,
    val message: String,
    val timestamp: Long,
    @SerialName("is_read") val isRead: Boolean = false
)

@Serializable
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("user_id") val userId: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    @SerialName("is_read") val isRead: Boolean = false,
    val type: String // "JUMP_VERIFICATION", "SYSTEM", "CHAT", "FOLLOW"
)

@Serializable
@Entity(tableName = "feed_posts")
data class FeedPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    @SerialName("user_role") val userRole: String, // "JUMPER" or "DZ_OPERATOR"
    val content: String,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val timestamp: Long,
    val type: String, // "JUMP", "EVENT", "ACTIVITY"
    val likes: Int = 0
)

@Serializable
@Entity(tableName = "feed_comments")
data class FeedCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("post_id") val postId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    val content: String,
    val timestamp: Long
)

@Serializable
@Entity(tableName = "user_follows", primaryKeys = ["followerId", "followedId"])
data class UserFollowEntity(
    @SerialName("follower_id") val followerId: String,
    @SerialName("followed_id") val followedId: String,
    val timestamp: Long
)

@Serializable
@Entity(tableName = "dz_facilities")
data class DzFacilityEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("id") val id: Long = 0,
    @SerialName("dz_id") val dzId: String,
    val name: String,
    val description: String = "",
    @SerialName("photo_url") val photoUrl: String? = null
)

@Serializable
@Entity(tableName = "dz_inventory")
data class DzInventoryEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("id") val id: Long = 0,
    @SerialName("dz_id") val dzId: String,
    val name: String = "",
    val category: String, // Parachute System, AAD, Parts, etc.
    @SerialName("sub_category") val subCategory: String? = null, // Tandem, Student, Rental Sport
    @SerialName("make_model") val makeModel: String = "",
    @SerialName("size_sqft") val sizeSqft: String = "",
    @SerialName("serial_number") val serialNumber: String = "",
    @SerialName("manufacture_date") val manufactureDate: Long? = null,
    @SerialName("dom") val dom: String = "", // Date of Manufacture as String for flexibility
    @SerialName("last_service_date") val lastServiceDate: Long? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("linked_aad_id") val linkedAadId: Long? = null,
    @SerialName("linked_main_canopy_id") val linkedMainCanopyId: Long? = null,
    @SerialName("linked_reserve_canopy_id") val linkedReserveCanopyId: Long? = null,
    @SerialName("linked_container_id") val linkedContainerId: Long? = null,
    @SerialName("linked_pilot_chute_id") val linkedPilotChuteId: Long? = null,
    @SerialName("linked_other_part_id") val linkedOtherPartId: Long? = null,
    @SerialName("linked_part_ids") val linkedPartIds: String? = null, // Kept for legacy/extra

    // Aircraft Specific Fields
    @SerialName("aircraft_type") val aircraftType: String = "",
    @SerialName("aircraft_manufacture") val aircraftManufacture: String = "",
    @SerialName("aircraft_model") val aircraftModel: String = "",
    @SerialName("aircraft_category") val aircraftCategory: String = "",
    @SerialName("max_jumpers") val maxJumpers: Int = 0,
    @SerialName("max_payload") val maxPayload: String = "",
    @SerialName("mtow") val mtow: String = "",
    @SerialName("cruise_speed") val cruiseSpeed: String = "",
    @SerialName("climb_rate") val climbRate: String = "",
    @SerialName("service_ceiling") val serviceCeiling: String = "",
    @SerialName("engine_type") val engineType: String = "",
    @SerialName("engine_manufacturer") val engineManufacturer: String = "",
    @SerialName("engine_serial_number") val engineSerialNumber: String = "",
    @SerialName("engine_tso") val engineTso: String = "",
    @SerialName("engine_next_overhaul_due") val engineNextOverhaulDue: String = "",
    @SerialName("fuel_type") val fuelType: String = "",
    @SerialName("fuel_capacity") val fuelCapacity: String = "",
    @SerialName("normal_jump_altitude") val normalJumpAltitude: String = "",
    @SerialName("maximum_jump_altitude") val maximumJumpAltitude: String = "",
    @SerialName("typical_jump_run_speed") val typicalJumpRunSpeed: String = "",
    @SerialName("door_configuration") val doorConfiguration: String = "",
    @SerialName("last_inspection_date") val lastInspectionDate: Long? = null,
    @SerialName("next_inspection_date") val nextInspectionDate: Long? = null,
    @SerialName("is_for_sale") val isForSale: Boolean = false,
    @SerialName("is_for_rent") val isForRent: Boolean = false,
    @SerialName("rental_fee") val rentalFee: Double = 0.0,
    @SerialName("rental_status") val rentalStatus: String = "Available", // Available, Reserved, In Use, Maintenance
    @SerialName("current_fuel") val currentFuel: Double = 0.0,
    @SerialName("fuel_burn_per_load") val fuelBurnPerLoad: Double = 0.0,
    @SerialName("total_flight_hours") val totalFlightHours: Double = 0.0,
    @SerialName("maintenance_interval_hours") val maintenanceIntervalHours: Int = 100,
    @SerialName("is_grounded") val isGround: Boolean = false
)

@Serializable
@Entity(tableName = "dz_ratings")
data class DzRatingEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("id") val id: Long = 0,
    @SerialName("dz_id") val dzId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    val stars: Int, // 1-5
    val comment: String = "",
    val timestamp: Long = 0
)

@Serializable
@Entity(tableName = "dz_waivers")
data class DzWaiverEntity(
    @PrimaryKey(autoGenerate = true) @SerialName("id") val id: Long = 0,
    @SerialName("dz_id") val dzId: String,
    val title: String,
    val content: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("last_updated") val lastUpdated: Long = 0
)

@Serializable
@Entity(tableName = "jumper_waiver_signatures")
data class JumperWaiverSignatureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("dz_id") val dzId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("waiver_id") val waiverId: Long,
    @SerialName("signature_base64") val signatureBase64: String, // Or URL to storage
    @SerialName("signed_at") val signedAt: Long
)

@Serializable
data class JumpMediaEntity(
    @SerialName("id") val id: Long = 0,
    @SerialName("jump_id") val jumpId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("media_url") val mediaUrl: String,
    @SerialName("media_type") val mediaType: String, // "IMAGE", "VIDEO", "TELEMETRY"
)

@Serializable
@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("dz_id") val dzId: String,
    val name: String,
    val code: String,
    val description: String,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("promo_type") val promoType: String, // PERCENTAGE, FIXED, FREE_SLOT, BUNDLE, etc.
    val value: Double,
    @SerialName("start_date") val startDate: Long,
    @SerialName("end_date") val endDate: Long,
    @SerialName("is_unlimited") val isUnlimited: Boolean = true,
    @SerialName("max_redemptions") val maxRedemptions: Int = 0,
    @SerialName("current_redemptions") val currentRedemptions: Int = 0,
    @SerialName("eligibility_rules") val eligibilityRules: String = "ALL", // ALL, FIRST_TIME, LICENSED, STUDENT, etc.
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
@Entity(tableName = "user_loyalty")
data class UserLoyaltyEntity(
    @PrimaryKey @SerialName("user_id") val userId: String,
    @SerialName("total_points") val totalPoints: Int = 0,
    @SerialName("lifetime_points") val lifetimePoints: Int = 0,
    @SerialName("last_updated") val lastUpdated: Long = 0
)

@Serializable
@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("referrer_id") val referrerId: String,
    @SerialName("referee_id") val refereeId: String,
    @SerialName("referee_role") val refereeRole: String = "JUMPER", // JUMPER, LICENSED, INSTRUCTOR, RIGGER, DZO, COACH
    @SerialName("reward_points") val rewardPoints: Int = 0,
    @SerialName("status") val status: String = "PENDING", // PENDING, VERIFIED, REJECTED
    @SerialName("is_dzo_verified") val isDzoVerified: Boolean = false,
    @SerialName("date_referred") val dateReferred: Long = 0,
    @SerialName("verification_date") val verificationDate: Long? = null
)

@Serializable
@Entity(tableName = "student_skills", primaryKeys = ["userId", "skillId"])
data class StudentSkillEntity(
    @SerialName("user_id") val userId: String,
    @SerialName("skill_id") val skillId: String, // e.g., "AFF_L1_STABLE_EXIT"
    @SerialName("category") val category: String, // "AFF", "A_LICENSE", "B_LICENSE"
    @SerialName("skill_name") val skillName: String,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("instructor_id") val instructorId: String? = null,
    @SerialName("instructor_name") val instructorName: String? = null,
    @SerialName("completion_date") val completionDate: Long? = null,
    @SerialName("notes") val notes: String? = ""
)

@Serializable
@Entity(tableName = "season_leaderboards", primaryKeys = ["seasonId", "userId", "category"])
data class SeasonLeaderboardEntry(
    @SerialName("season_id") val seasonId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    val category: String,
    val value: Double,
    @SerialName("last_updated") val lastUpdated: Long = 0
)
