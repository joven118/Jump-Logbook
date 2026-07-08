package com.V2Skydivejump.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@Entity(tableName = "gear_listings")
data class GearListingEntity(
    @PrimaryKey val id: String,
    @SerialName("seller_id") val sellerId: String,
    val title: String,
    val category: String, // Canopy, Container, Altimeter, AAD
    val condition: String,
    val price: Double,
    val description: String,
    @SerialName("date_posted") val datePosted: Long,
    val location: String? = null,
    @SerialName("contact_details") val contactDetails: String? = null,
    @SerialName("photo_urls") val photoUrls: String? = null, // Comma-separated
    @SerialName("video_urls") val videoUrls: String? = null // Comma-separated
)
