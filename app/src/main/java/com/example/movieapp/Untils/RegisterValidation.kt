package com.example.movieapp.Untils

sealed class RegisterValidation() {
    object Success:RegisterValidation()
    data class Failed(val message:String):RegisterValidation()
}
data class RegisterFieldsState(
    val email:RegisterValidation,
    val password:RegisterValidation
)