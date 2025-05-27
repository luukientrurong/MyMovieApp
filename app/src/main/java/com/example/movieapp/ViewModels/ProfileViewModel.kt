package com.example.movieapp.ViewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.Models.User
import com.example.movieapp.Repository.CloudinaryRepository
import com.example.movieapp.Untils.Resource
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinary:CloudinaryRepository
):ViewModel() {
    private val _updateUserName = MutableStateFlow<Resource<String>>(Resource.Unspectified())
    val updaterUserName:Flow<Resource<String>> = _updateUserName
    private val _updateUserImg = MutableStateFlow<Resource<String>>(Resource.Unspectified())
    val updaterUserImg:Flow<Resource<String>> = _updateUserImg
    private val _changePassword = MutableStateFlow<Resource<String>>(Resource.Unspectified())
    val changePassword: Flow<Resource<String>> = _changePassword

    fun updateUserName(userName:String){
        viewModelScope.launch {
            _updateUserName.emit(Resource.Loading())
        }
        firestore.runTransaction{
            val documentRef = firestore.collection("User").document(auth.uid!!)
            val currentUser = it.get(documentRef).toObject(User::class.java)
            val newUser = currentUser!!.copy(userName=userName)
            it.set(documentRef,newUser)
        }.addOnSuccessListener {
            viewModelScope.launch {
                _updateUserName.emit(Resource.Success(userName))
            }
        }
            .addOnFailureListener{
                viewModelScope.launch {
                    _updateUserName.emit(Resource.Error(it.message.toString()))
                }
            }
    }
    fun updateImageUser(imageUri: Uri) {
        Log.e("UserImg","Load1")
        viewModelScope.launch {
            try {
                _updateUserImg.emit(Resource.Loading())
                Log.e("UserImg","Load2")
                val imageUrl = cloudinary.uploadFileToCloudinary(imageUri)
                firestore.runTransaction { transaction ->
                    val documentRef = firestore.collection("User").document(auth.uid!!)
                    val currentUser = transaction.get(documentRef).toObject(User::class.java)
                    val newUser = currentUser!!.copy(imgPath = imageUrl)
                    Log.e("UserImg","URL ${imageUrl}")
                    transaction.set(documentRef, newUser)
                }.addOnSuccessListener {
                    viewModelScope.launch {
                        _updateUserImg.emit(Resource.Success(imageUrl))
                    }
                    Log.e("UserImg","Xong")
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _updateUserImg.emit(Resource.Error(it.message.toString()))
                    }
                    Log.e("UserImg","Fail")
                }
            } catch (e: Exception) {
                _updateUserImg.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePassword.emit(Resource.Loading())
            try {
                val user = auth.currentUser!!
                // Xác thực lại người dùng với mật khẩu cũ
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                user.reauthenticate(credential).await()
                // Đổi mật khẩu
                user.updatePassword(newPassword).await()
                _changePassword.emit(Resource.Success("Đổi mật khẩu thành công"))
            } catch (e: Exception) {
                _changePassword.emit(Resource.Error(e.message ?: "Lỗi đổi mật khẩu"))
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

}