package com.example.movieapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.Models.User
import com.example.movieapp.Untils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _login = MutableSharedFlow<Resource<FirebaseUser>>()
    val login = _login.asSharedFlow()

    private val _resetPassword = MutableSharedFlow<Resource<String>>()
    val resetPassword = _resetPassword.asSharedFlow()
    fun loginWithEmailAndPassword(email: String, pass: String) {
        viewModelScope.launch {
            _login.emit(Resource.Loading())
        }
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                viewModelScope.launch {
                    it.user?.let {
                        viewModelScope.launch {
                            _login.emit(Resource.Success(it))
                        }
                    }
                }
            }
            .addOnFailureListener{
                viewModelScope.launch {
                    _login.emit(Resource.Error(getErrorMessage(it)))
                }
            }
    }
    fun resetPassword(email: String){
        viewModelScope.launch {
            _resetPassword.emit(Resource.Loading())
        }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    viewModelScope.launch {
                    _resetPassword.emit(Resource.Success(email))
                    }
                }
                .addOnFailureListener{
                    viewModelScope.launch {
                        _resetPassword.emit(Resource.Error(getErrorMessage(it)))
                    }
                }
    }
    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại!"
            is FirebaseAuthInvalidCredentialsException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Định dạng email không hợp lệ!"
                    "ERROR_WRONG_PASSWORD" -> "Sai mật khẩu, vui lòng thử lại!"
                    else -> "Thông tin đăng nhập không hợp lệ!"
                }
            }
            else -> "Lỗi không xác định: ${exception.localizedMessage}"
        }
    }

}