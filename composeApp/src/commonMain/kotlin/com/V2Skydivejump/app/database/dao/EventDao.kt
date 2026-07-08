package com.V2Skydivejump.app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.V2Skydivejump.app.database.entities.SkydivingEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<SkydivingEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SkydivingEvent)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)
}
