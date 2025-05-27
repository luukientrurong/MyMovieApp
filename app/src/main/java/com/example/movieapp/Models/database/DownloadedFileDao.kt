package com.example.movieapp.Models.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DownloadedFileDao {
    @Insert
    suspend fun insert(file: DownloadedFile)

    @Query("SELECT * FROM downloaded_files")
    suspend fun getAllFilesSync(): List<DownloadedFile>

    @Query("SELECT * FROM downloaded_files")
    fun getAllFiles(): LiveData<List<DownloadedFile>>
}