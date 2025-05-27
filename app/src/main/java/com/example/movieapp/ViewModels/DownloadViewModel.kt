package com.example.movieapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.movieapp.Models.database.AppDatabase
import com.example.movieapp.Models.database.DownloadedFile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val database: AppDatabase
): ViewModel() {
    fun getAllFiles(): LiveData<List<DownloadedFile>>{
        return database.downloadedFileDao().getAllFiles()
    }
}