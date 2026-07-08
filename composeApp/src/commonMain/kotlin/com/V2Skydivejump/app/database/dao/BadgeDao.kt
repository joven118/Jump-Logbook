package com.V2Skydivejump.app.database.dao

import androidx.room.*
import com.V2Skydivejump.app.database.entities.BadgeEntity
import com.V2Skydivejump.app.database.entities.UserBadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM user_badges WHERE userId = :userId")
    fun getUserBadges(userId: String): Flow<List<UserBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun awardBadge(userBadge: UserBadgeEntity)

    @Query("SELECT * FROM user_badges WHERE userId = :userId AND badgeId = :badgeId")
    suspend fun hasBadge(userId: String, badgeId: String): UserBadgeEntity?
}
