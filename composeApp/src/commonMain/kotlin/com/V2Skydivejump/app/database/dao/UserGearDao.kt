package com.V2Skydivejump.app.database.dao

import androidx.room.*
import com.V2Skydivejump.app.database.entities.UserGearEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGearDao {
    @Query("SELECT * FROM user_gear WHERE userId = :userId")
    fun getUserGear(userId: String): Flow<List<UserGearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGear(gear: UserGearEntity)

    @Delete
    suspend fun deleteGear(gear: UserGearEntity)

    @Query("SELECT * FROM user_gear WHERE gearId = :gearId")
    suspend fun getGearById(gearId: String): UserGearEntity?
}
