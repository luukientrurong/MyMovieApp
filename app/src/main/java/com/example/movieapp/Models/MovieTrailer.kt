package com.example.movieapp.Models

class MovieTrailerResponse(
    val id:Int,
    val results:List<Trailer>
)
data class Trailer(
    val id:String,
    val key:String,
    val name:String,
    val site:String,
    val type:String
)