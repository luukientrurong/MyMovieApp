package com.example.movieapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.BuildConfig
import com.example.movieapp.Interface.TMDBApiService
import com.example.movieapp.Models.MovieDetail
import com.example.movieapp.Untils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val tmdb: TMDBApiService
) : ViewModel() {
    private val _favouriteMovies = MutableStateFlow<Resource<List<MovieDetail>>>(Resource.Unspectified())
    val favouriteMovies: Flow<Resource<List<MovieDetail>>> = _favouriteMovies

    init {
        getFavouriteMovies()
    }
    fun getFavouriteMovies() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("User").document(userId).collection("favourites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    viewModelScope.launch {
                        _favouriteMovies.emit(Resource.Error("Lỗi: ${error.message}"))
                    }
                    return@addSnapshotListener
                }

        viewModelScope.launch {
            _favouriteMovies.emit(Resource.Loading())
            try {
                // Lấy danh sách movieId từ Firestore
                val movieIds = snapshot?.documents?.mapNotNull { it.getString("movieId")?.toIntOrNull() } ?: emptyList()
                Log.e("FavViewModel","sl movieId: ${movieIds.size}")
                if (movieIds.isNotEmpty()) {
                    // Lấy chi tiết phim từ TMDB API
                    val movieDetails = movieIds.map { movieId ->
                        async { fetchMovieDetail(movieId) }
                    }.awaitAll().filterNotNull()
                    Log.e("FavViewModel","movieDetails: ${movieDetails.size}")

                    _favouriteMovies.emit(Resource.Success(movieDetails))
                } else {
                    _favouriteMovies.emit(Resource.Success(emptyList()))
                }
            } catch (e: Exception) {
                _favouriteMovies.emit(Resource.Error("Lỗi: ${e.message}"))
            }
        }
    }
}
    suspend fun fetchMovieDetail(movieId: Int): MovieDetail? = suspendCoroutine { continuation ->
        tmdb.getMovieDetails(movieId, BuildConfig.TMDB_API_KEY)
            .enqueue(object : Callback<MovieDetail> {
                override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                    if (response.isSuccessful) {
                        continuation.resume(response.body())
                    } else {
                        continuation.resume(null)
                    }
                }

                override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                    continuation.resume(null)
                }
            })
    }

}