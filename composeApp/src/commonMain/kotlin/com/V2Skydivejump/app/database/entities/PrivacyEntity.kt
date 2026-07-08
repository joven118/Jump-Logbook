package com.V2Skydivejump.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.V2Skydivejump.app.UserRole
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed interface PrivacyLevel {
    @Serializable
    data object PUBLIC : PrivacyLevel
    @Serializable
    data object ONLY_ME : PrivacyLevel
    @Serializable
    data class GROUP(val allowedRoles: List<UserRole>) : PrivacyLevel
}

@Serializable
@Entity(tableName = "user_privacy_settings")
data class UserPrivacySettingsEntity(
    @PrimaryKey @SerialName("user_id") val userId: String,
    @SerialName("name_privacy") val namePrivacy: String = "PUBLIC",
    @SerialName("screen_name_privacy") val screenNamePrivacy: String = "PUBLIC",
    @SerialName("license_privacy") val licensePrivacy: String = "PUBLIC",
    @SerialName("birthdate_privacy") val birthdatePrivacy: String = "ONLY_ME",
    @SerialName("gender_privacy") val genderPrivacy: String = "ONLY_ME",
    @SerialName("nationality_privacy") val nationalityPrivacy: String = "PUBLIC",
    @SerialName("city_privacy") val cityPrivacy: String = "PUBLIC",
    @SerialName("province_privacy") val provincePrivacy: String = "PUBLIC",
    @SerialName("country_privacy") val countryPrivacy: String = "PUBLIC",
    @SerialName("weight_load_privacy") val weightLoadPrivacy: String = "ONLY_ME",
    @SerialName("mobile_privacy") val mobilePrivacy: String = "ONLY_ME",
    @SerialName("email_privacy") val emailPrivacy: String = "ONLY_ME",
    @SerialName("emergency_contact_name_privacy") val emergencyContactNamePrivacy: String = "ONLY_ME",
    @SerialName("emergency_contact_number_privacy") val emergencyContactNumberPrivacy: String = "ONLY_ME",
    
    // Kept for backward compatibility or other areas if needed
    @SerialName("gear_privacy") val gearPrivacy: String = "PUBLIC",
    @SerialName("statistics_privacy") val statisticsPrivacy: String = "PUBLIC"
)
