package com.example.movieapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ConcatAdapter.Config
import com.example.movieapp.BuildConfig
import com.example.movieapp.Interface.TMDBApiService
import com.example.movieapp.Models.Movie
import com.example.movieapp.Models.MovieDetail
import com.example.movieapp.Models.MovieTrailerResponse
import com.example.movieapp.Models.Trailer
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
class MovieDetailViewModel @Inject constructor(
    private val tmdb:TMDBApiService
): ViewModel() {
    private val _movieDetail = MutableStateFlow<Resource<MovieDetail>>(Resource.Unspectified())
    val movieDetail:Flow<Resource<MovieDetail>> = _movieDetail
    private val _movieTrailer = MutableStateFlow<Resource<Trailer>>(Resource.Unspectified())
    val movieTrailer:Flow<Resource<Trailer>> = _movieTrailer
    fun fetchMovieDetail(movieId:Int){
        viewModelScope.launch {
            _movieDetail.emit(Resource.Loading())
        }
        tmdb.getMovieDetails(movieId,BuildConfig.TMDB_API_KEY).enqueue(object :Callback<MovieDetail>{
            override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                if(response.isSuccessful){
                    var detail = response.body()?: MovieDetail(0,"","","","","", emptyList(), emptyList(),0, 0.0F,0)
                    viewModelScope.launch {
                        _movieDetail.emit(Resource.Success(detail))
                    }
                }else{
                    viewModelScope.launch {
                        _movieDetail.emit(Resource.Error("lay ko thanh cong, ma http: "+response.code()))
                    }
                }
            }

            override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                viewModelScope.launch {
                    _movieDetail.emit(Resource.Error("loi khi lay du lieu: "+t.message))
                }
            }

        })
    }
    fun fetchMovieTrailer(movieId: Int) {
        tmdb.getMovieTrailer(movieId, BuildConfig.TMDB_API_KEY).enqueue(object : Callback<MovieTrailerResponse> {
            override fun onResponse(
                call: Call<MovieTrailerResponse>,
                response: Response<MovieTrailerResponse>
            ) {
                if (response.isSuccessful) {
                    val videos = response.body()?.results ?: emptyList()
                    val trailer = videos.filter {
                        it.site == "YouTube" && it.type == "Trailer"
                    }
                    if (trailer.isNotEmpty()) {
                        viewModelScope.launch {
                            _movieTrailer.emit(Resource.Success(trailer[0]))
                        }
                    } else {
                        Log.e("GetMovie", "No YouTube trailers found for movieId: $movieId")
                        viewModelScope.launch {
                            _movieTrailer.emit(Resource.Error("No YouTube trailers found for movieId: $movieId\""))
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MovieTrailerResponse>, t: Throwable) {
                Log.e("GetMovie", "Loi: ${t.message}")
            }
        })
    }
}