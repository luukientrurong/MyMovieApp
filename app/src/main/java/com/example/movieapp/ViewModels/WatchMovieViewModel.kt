package com.example.movieapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.Models.Movie
import com.example.movieapp.Untils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchMovieViewModel @Inject constructor(
    private val db:FirebaseFirestore
):ViewModel() {
    private val _movieUrl = MutableStateFlow<Resource<String>>(Resource.Unspectified())
    val movieUrl:Flow<Resource<String>> = _movieUrl
    fun fetchMovieUrl(idMovie:String){
        viewModelScope.launch {
            _movieUrl.emit(Resource.Loading())
        }
        Log.e("WacthViewModel","chuan bi lay")
        db.collection("LinkMovie").whereEqualTo("id",idMovie).get()
            .addOnSuccessListener {
                if(!it.isEmpty){
                    val document = it.documents[0]
                    val url = document.getString("url")
                    Log.e("WacthViewModel","lay thanh cong $url")
                    viewModelScope.launch {
                        _movieUrl.emit(Resource.Success(url!!))
                    }
                }else{
                    viewModelScope.launch {
                        _movieUrl.emit(Resource.Error("khong tim thay"))
                    }
                    Log.e("WacthViewModel","ko tim dc")
                }
            }.addOnFailureListener{
                viewModelScope.launch {
                    _movieUrl.emit(Resource.Error("loi: ${it.message}"))
                }
                Log.e("WacthViewModel","lay ko thanh cong ${it.message}")
            }
    }

}