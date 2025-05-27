package com.example.movieapp.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Company(
    val id: Int,
    val logo_path:String?,
    val name:String,
    val origin_country:String
):Parcelable