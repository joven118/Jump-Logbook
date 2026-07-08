package com.V2Skydivejump.app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.V2Skydivejump.app.database.entities.GearListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GearListingDao {
    @Query("SELECT * FROM gear_listings ORDER BY datePosted DESC")
    fun getAllListings(): Flow<List<GearListingEntity>>

    @Query("SELECT * FROM gear_listings WHERE category = :category ORDER BY datePosted DESC")
    fun getListingsByCategory(category: String): Flow<List<GearListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: GearListingEntity)

    @Query("DELETE FROM gear_listings WHERE id = :id")
    suspend fun deleteListing(id: String)
}
