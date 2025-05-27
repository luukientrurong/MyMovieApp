package com.example.movieapp.ViewModels.SharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.Untils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MovieFavouritesSharedViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _favourite = MutableLiveData<String>()
    val favourtie: LiveData<String> get() = _favourite
    private val _isFavourite = MutableLiveData<Boolean>()
    val isFavourite: LiveData<Boolean> get() = _isFavourite
    fun addFavourite(movieId: String) {
        val userid = auth.currentUser!!.uid
        firestore.collection("User").document(userid).collection("favourites").document(movieId)
            .set(hashMapOf("movieId" to movieId))
            .addOnSuccessListener {
                _favourite.value = "Thêm thành công"
                _isFavourite.value = true
            }
            .addOnFailureListener {
                _favourite.value = "Lỗi: ${it.message}"
            }
    }

    fun deleteFavourite(movieId: String) {
        val userId = auth.currentUser!!.uid
        firestore.collection("User").document(userId).collection("favourites").document(movieId)
            .delete()
            .addOnSuccessListener {
                _favourite.value = "Xóa thành công"
                _isFavourite.value = false
            }
            .addOnFailureListener {
                _favourite.value = "Lỗi: ${it.message}"
            }
    }

    fun isFavourited(movieId: String) {
        val userId = auth.currentUser!!.uid
        viewModelScope.launch {
            try {
                val document = firestore.collection("User")
                    .document(userId)
                    .collection("favourites")
                    .document(movieId)
                    .get()
                    .await()

                _isFavourite.value = document.exists()
            } catch (e: Exception) {
                _isFavourite.value = false
                _favourite.value = e.message
            }
        }
    }
}