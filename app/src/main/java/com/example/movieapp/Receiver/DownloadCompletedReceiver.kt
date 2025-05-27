package com.example.movieapp.Receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.movieapp.Models.database.AppDatabase
import com.example.movieapp.Models.database.DownloadedFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadCompleteReceiver : BroadcastReceiver() {
    @Inject
    lateinit var database: AppDatabase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            Log.e("DownloadCheck", "Download completed")
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Log.e("DownloadCheck", "Download ID: $downloadId")
            if (downloadId != -1L) {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    Log.e("DownloadCheck", "Download status: $status, reason: $reason")
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val fileUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                        val fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
                        val downloadTime = System.currentTimeMillis()
                        Log.e("DownloadCheck", "File data: name=$fileName, path=$fileUri, time=$downloadTime")

                        val file = DownloadedFile(
                            fileName = fileName.removePrefix("Đang tải: ").trim(),
                            filePath = fileUri,
                            downloadTime = downloadTime
                        )
                        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                            try {
                                database.downloadedFileDao().insert(file)
                                Log.e("DownloadCheck", "Successfully saved to database")
                                val files = database.downloadedFileDao().getAllFilesSync()
                                Log.e("DownloadCheck", "Files in database after insert: ${files.size}")
                                files.forEach { f ->
                                    Log.e("DownloadCheck", "File: ${f.fileName}, ${f.filePath}")
                                }
                            } catch (e: Exception) {
                                Log.e("DownloadCheck", "Error saving to database: ${e.message}")
                            }
                        }
                    } else {
                        Log.e("DownloadCheck", "Download not successful, status: $status, reason: $reason")
                    }
                } else {
                    Log.e("DownloadCheck", "Cursor is empty")
                }
                cursor.close()
            } else {
                Log.e("DownloadCheck", "Invalid download ID")
            }
        }
    }
}