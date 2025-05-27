package com.example.movieapp.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

class GenreResponse(
    val genres: List<MyGenre>
)
@Parcelize
data class MyGenre(
    val id:String,
    val name:String
) : Parcelable{
    constructor():this("","")
}
