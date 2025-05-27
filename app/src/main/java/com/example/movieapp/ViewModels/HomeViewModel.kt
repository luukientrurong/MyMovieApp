package com.example.movieapp.ViewModels

import android.util.Log
import androidx.lifecycle.*
import com.example.movieapp.Interface.TMDBApiService
import com.example.movieapp.Models.MovieResponse
import com.example.movieapp.Models.Movie
import com.example.movieapp.Models.MovieTrailerResponse
import com.example.movieapp.Models.Trailer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import com.example.movieapp.BuildConfig
import com.example.movieapp.Models.GenreResponse
import com.example.movieapp.Models.MyGenre
import com.example.movieapp.Untils.Resource

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tmdb: TMDBApiService
) : ViewModel() {

    private val _moviesSilde = MutableStateFlow<Resource<List<Movie>>>(Resource.Unspectified())
    val moviesSilde: Flow<Resource<List<Movie>>>  = _moviesSilde

    private val _movieTrailer = MutableStateFlow<Trailer?>(null)
    val movieTrailer:Flow<Trailer?> = _movieTrailer

    private val _moviesGenre = MutableStateFlow<Resource<List<MyGenre>>>(Resource.Unspectified())
    val moviesGenre:Flow<Resource<List<MyGenre>>> = _moviesGenre
    private val _moviesUpComing = MutableStateFlow<Resource<List<Movie>>>(Resource.Unspectified())
    val moviesUpComing:Flow<Resource<List<Movie>>> = _moviesUpComing
    private val _moviesNowPlaying = MutableStateFlow<Resource<List<Movie>>>(Resource.Unspectified())
    val moviesNowPlaying:Flow<Resource<List<Movie>>> = _moviesNowPlaying

    private var isNowPlayingLoading = false //tránh load nhiều lan`
    private var pageNowPlaying = 1 //trang hiện tai của now playing

    private var isUpComingLoading = false
    private var pageUpComing =1
    init {
        fetchMovie()
        fetchGenre()
        fetchMovieUpComing()
        fetchMovieNowPlaying()
    }
    fun fetchMovie() {
        viewModelScope.launch {
            _moviesSilde.emit(Resource.Loading())
        }
        tmdb.getPopularMovies(BuildConfig.TMDB_API_KEY).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if(response.isSuccessful){
                    val movieList = response.body()?.results?: emptyList()
                    viewModelScope.launch {
                        _moviesSilde.emit(Resource.Success(movieList))
                    }
                    Log.e("GetMovie","Get thanh cong: ${movieList.size} phim")
                }else{
                    viewModelScope.launch {
                        _moviesSilde.emit(Resource.Error("Lay khong thanh cong ma http: ${response.code()}"))
                    }
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                viewModelScope.launch {
                    _moviesSilde.emit(Resource.Error("Lay khong thanh cong ma http: ${t.message.toString()}"))
                }
            }

        })
    }

    fun fetchGenre(){
        tmdb.getGenre(BuildConfig.TMDB_API_KEY).enqueue(object :Callback<GenreResponse>{
            override fun onResponse(call: Call<GenreResponse>, response: Response<GenreResponse>) {
                val listGenre = response.body()?.genres
                viewModelScope.launch {
                    _moviesGenre.emit(Resource.Success(listGenre?: emptyList()))
                }
            }

            override fun onFailure(call: Call<GenreResponse>, t: Throwable) {
                viewModelScope.launch {
                    _moviesGenre.emit(Resource.Error(t.message.toString()))
                }
            }

        })
    }
    fun fetchMovieUpComing(page: Int = 1){
        if(isUpComingLoading) return
        isUpComingLoading = true
        var currentList = _moviesUpComing.value.data?: emptyList()
        viewModelScope.launch {
            _moviesUpComing.emit(Resource.Loading())
        }
        tmdb.getUpComingMovies(BuildConfig.TMDB_API_KEY,page=page).enqueue(object :Callback<MovieResponse>{
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                val listMove = response.body()?.results?: emptyList()
                Log.e("UpComing","page = ${page}, sl =${currentList.size}")
                viewModelScope.launch {
                    _moviesUpComing.emit(Resource.Success(currentList+listMove))
                }
                isUpComingLoading = false
                pageUpComing=page
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                viewModelScope.launch {
                    _moviesUpComing.emit(Resource.Error(t.message.toString()))
                }
                isUpComingLoading=false
            }

        })
    }
    fun fetchMovieNowPlaying(page: Int = 1){
        if (isNowPlayingLoading) return //đang trong qua` trình load thì không thể load nữa
        isNowPlayingLoading = true
        var currentList = _moviesNowPlaying.value.data?: emptyList() //ds movie hiện tại
        viewModelScope.launch {
            _moviesNowPlaying.emit(Resource.Loading())
        }
        tmdb.getNowPlaying(BuildConfig.TMDB_API_KEY,page=page).enqueue(object :Callback<MovieResponse>{
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                val listMove = response.body()?.results?: emptyList()
                val updatedList = currentList + listMove
                Log.e("HomeViewModel","tong ds hien tai: ${currentList.size}")
                viewModelScope.launch {
                    _moviesNowPlaying.emit(Resource.Success(updatedList))
                }
                pageNowPlaying = page
                isNowPlayingLoading=false
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                viewModelScope.launch {
                    _moviesNowPlaying.emit(Resource.Error(t.message.toString()))
                }
                isNowPlayingLoading = false
            }

        })
    }
    fun fetchNextMovieNowPlayingPage(){
        if (!isNowPlayingLoading){
            fetchMovieNowPlaying(pageNowPlaying+1)
        }
    }
    fun fetchNextMovieUpComing(){
        if(!isUpComingLoading){
            fetchMovieUpComing(pageUpComing+1)
        }
    }
}
