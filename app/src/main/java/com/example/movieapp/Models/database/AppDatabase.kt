package com.example.movieapp.Models.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadedFile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadedFileDao(): DownloadedFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            Log.e("DatabaseCheck", "Checking INSTANCE: $INSTANCE")
            return INSTANCE ?: synchronized(this) {
                Log.e("DatabaseCheck", "INSTANCE is null, creating new database")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                Log.e("DatabaseCheck", "New instance created: $instance")
                instance
            }.also {
                Log.e("DatabaseCheck", "Returning instance: $it")
            }
        }
    }
}