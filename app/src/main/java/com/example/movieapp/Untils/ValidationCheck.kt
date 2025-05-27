package com.example.movieapp.Untils

import android.util.Patterns

fun validateEmail(email:String): RegisterValidation{
    if(email.isEmpty()){
        return RegisterValidation.Failed("Email cannot be empty")
    }
    if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Sai dinh dang email")
    return RegisterValidation.Success
}
fun validatePassword(password:String):RegisterValidation{
    if(password.isEmpty())
        return RegisterValidation.Failed("password ko dc de trong")
    if (password.length<6)
        return RegisterValidation.Failed("mat khau phai co it nhat 6 ky tu")
    return RegisterValidation.Success
}