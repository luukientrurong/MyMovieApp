package com.example.movieapp.Models

data class User(
    val userName:String,
    val email:String,
    val imgPath:String =""
){
    constructor():this("","","")
}