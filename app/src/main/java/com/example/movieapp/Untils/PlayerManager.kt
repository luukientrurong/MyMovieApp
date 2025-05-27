package com.example.movieapp.utils

import com.google.android.exoplayer2.ExoPlayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor() {
    private var currentPlayer: ExoPlayer? = null

    fun setCurrentPlayer(player: ExoPlayer) {
        currentPlayer?.release() // Giải phóng player cũ
        currentPlayer = player
    }

    fun releasePlayer() {
        currentPlayer?.release()
        currentPlayer = null
    }

    fun getCurrentPlayer(): ExoPlayer? = currentPlayer
}