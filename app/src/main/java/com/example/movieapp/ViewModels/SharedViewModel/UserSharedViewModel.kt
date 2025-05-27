package com.example.movieapp.ViewModels.SharedViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.Models.User
import com.example.movieapp.Untils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSharedViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
): ViewModel() {
    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspectified())
    val user: StateFlow<Resource<User>> = _user

    init {
        fetchUser()
    }
    private fun fetchUser() {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
            firestore.collection("User").document(auth.uid!!).get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        viewModelScope.launch {
                            _user.emit(Resource.Success(user))
                        }
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _user.emit(Resource.Error(it.message ?: "Lỗi không xác định"))
                    }
                }
        }
    }
}