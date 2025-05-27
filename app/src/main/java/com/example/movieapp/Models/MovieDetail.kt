package com.example.movieapp.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val backdrop_path:String,
    val poster_path: String,
    val original_title:String,
    val genres: List<MyGenre>,
    val production_companies: List<Company>,
    val runtime: Int,
    val vote_average:Float,
    val vote_count:Int
):Parcelable{
    constructor():this(0,"","","","","", emptyList(), emptyList(),0, 0.0F,0)
}