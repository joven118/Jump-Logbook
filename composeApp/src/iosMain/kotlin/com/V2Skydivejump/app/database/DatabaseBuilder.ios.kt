package com.V2Skydivejump.app.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/" + DB_FILE_NAME
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory =  { AppDatabase::class.instantiateImpl() }
    )
}
