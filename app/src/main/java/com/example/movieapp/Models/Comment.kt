package com.example.movieapp.Models

data class Comment(
    val userId: String = "",
    val username: String = "",
    val userPhotoUrl: String = "",
    val commentText: String = "",
    val timestamp: Long = 0
)