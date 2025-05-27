package com.example.movieapp.ViewModels

import androidx.lifecycle.ViewModel
import com.example.movieapp.Models.User
import com.example.movieapp.Untils.Constants.USER_COLLECTION
import com.example.movieapp.Untils.RegisterFieldsState
import com.example.movieapp.Untils.RegisterValidation
import com.example.movieapp.Untils.Resource
import com.example.movieapp.Untils.validateEmail
import com.example.movieapp.Untils.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db:FirebaseFirestore
) : ViewModel() {
    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspectified())
    val register: Flow<Resource<User>> = _register
    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()
    private fun checkValidatation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        val shouldRegister =
            emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success
        return shouldRegister
    }

    fun createAccoutWihtEmailAndPassowrd(user: User, password: String) {
        if (checkValidatation(user, password)) {
            runBlocking {
                _register.emit(Resource.Loading())
            }
            auth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener {
                    it.user?.let {
                        saveUserInfo(it.uid,user)
                    }
                }
                .addOnFailureListener{
                    _register.value = Resource.Error(it.message.toString())
                }
        }else{
            val registerFieldState = RegisterFieldsState(validateEmail(user.email), validatePassword(password))
            runBlocking {
                _validation.send(registerFieldState)
            }
        }
    }

    private fun saveUserInfo(uid: String, user: User) {
        db.collection(USER_COLLECTION).document(uid).set(user)
            .addOnSuccessListener {
                _register.value = (Resource.Success(user))

            }
            .addOnFailureListener{
                _register.value = (Resource.Error(it.message.toString()))
            }
    }
}