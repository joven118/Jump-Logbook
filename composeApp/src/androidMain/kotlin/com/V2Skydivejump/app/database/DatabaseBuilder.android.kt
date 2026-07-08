package com.V2Skydivejump.app.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.V2Skydivejump.app.appContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = appContext.getDatabasePath(DB_FILE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
