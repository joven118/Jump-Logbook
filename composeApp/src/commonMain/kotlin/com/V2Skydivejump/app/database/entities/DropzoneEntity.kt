package com.V2Skydivejump.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@Entity(tableName = "dropzones")
data class DropzoneEntity(
    @PrimaryKey val id: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("dz_name") val dzName: String,
    val location: String,
    val city: String = "",
    val province: String = "",
    val country: String = "",
    val facilities: String,
    @SerialName("aircraft_fleet") val aircraftFleet: String,
    @SerialName("business_hour") val businessHour: String,
    val website: String,
    @SerialName("contact_numbers") val contactNumbers: String,
    @SerialName("email_address") val emailAddress: String
)

@Serializable
@Entity(tableName = "events")
data class SkydivingEvent(
    @PrimaryKey val id: String,
    @SerialName("dz_id") val dzId: String,
    val title: String,
    val date: Long,
    val description: String,
    @SerialName("registration_fee") val registrationFee: Double
)
