package com.V2Skydivejump.app.database.dao

import androidx.room.*
import com.V2Skydivejump.app.database.entities.UserPrivacySettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacyDao {
    @Query("SELECT * FROM user_privacy_settings WHERE userId = :userId")
    fun getPrivacySettings(userId: String): Flow<UserPrivacySettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePrivacySettings(settings: UserPrivacySettingsEntity)
}
