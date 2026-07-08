package com.V2Skydivejump.app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.V2Skydivejump.app.database.entities.DropzoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DropzoneDao {
    @Query("SELECT * FROM dropzones")
    fun getAllDropzones(): Flow<List<DropzoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDropzone(dropzone: DropzoneEntity)

    @Query("SELECT * FROM dropzones WHERE id = :id")
    suspend fun getDropzoneById(id: String): DropzoneEntity?
}
