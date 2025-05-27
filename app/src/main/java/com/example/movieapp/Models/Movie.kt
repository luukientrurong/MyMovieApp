package com.example.movieapp.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class MovieResponse(
    val results: List<Movie> //tmdb trả ve` 1 biến results là list chứa cac` phần tử là thông tin của 1 bộ phim
)

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val backdrop_path:String,
    val poster_path: String,
    val original_title:String
)