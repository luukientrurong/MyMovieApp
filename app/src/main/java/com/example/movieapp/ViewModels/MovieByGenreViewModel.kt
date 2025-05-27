package com.example.movieapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.BuildConfig
import com.example.movieapp.Interface.TMDBApiService
import com.example.movieapp.Models.Movie
import com.example.movieapp.Models.MovieResponse
import com.example.movieapp.Untils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MovieByGenreViewModel @Inject constructor(
    private val tmdb:TMDBApiService
):ViewModel() {
    private val _movieByGenre = MutableStateFlow<Resource<List<Movie>>>(Resource.Unspectified())
    val movieByGenre:Flow<Resource<List<Movie>>> = _movieByGenre
    private var isLoading = false
    private var currentPage =1
    private var currentGenreId =0
    fun fetchMovieByGenre(genreId:Int,page:Int = 1){
        if(isLoading) return
        isLoading = true
        currentGenreId =genreId
        var currentList = _movieByGenre.value.data?: emptyList()
        Log.e("MovieByGenre","ds hien tai: "+currentList.size)
        viewModelScope.launch {
            _movieByGenre.emit(Resource.Loading())
        }
        tmdb.getMovieByGenre(apiKey = BuildConfig.TMDB_API_KEY, genreId = genreId, page = page).enqueue(object : Callback<MovieResponse>{
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if(response.isSuccessful){
                    val movieList = response.body()?.results?: emptyList()
                    viewModelScope.launch {
                        _movieByGenre.emit(Resource.Success(currentList+movieList))
                    }
                    isLoading =false
                    currentPage = page
                }
                else{
                    viewModelScope.launch {
                        _movieByGenre.emit(Resource.Error("lay ko thanh cong, ma http: "+response.code()))
                    }
                    isLoading = false
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                viewModelScope.launch {
                    _movieByGenre.emit(Resource.Error("loi khi lay du lieu: "+t.message))
                }
            }

        })
    }
    fun fetchNextPage(){
        if (!isLoading){
            fetchMovieByGenre(currentGenreId,currentPage+1)
        }
    }
}