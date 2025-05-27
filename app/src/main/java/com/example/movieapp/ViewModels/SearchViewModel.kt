package com.example.movieapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.BuildConfig
import com.example.movieapp.Interface.TMDBApiService
import com.example.movieapp.Models.Movie
import com.example.movieapp.Models.MovieResponse
import com.example.movieapp.Untils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tmdb: TMDBApiService
) : ViewModel() {
    private val _search = MutableStateFlow<Resource<List<Movie>>>(Resource.Unspectified())
    val search: Flow<Resource<List<Movie>>> = _search

    private var isLoading = false
    private var pageSearch = 1
    private var currentQuery = "" // Lưu query hiện tại để so sánh

    fun searchMovies(query: String, page: Int = 1) {
        if (isLoading) return
        isLoading = true

        // Nếu query mới và page = 1, reset danh sách
        val shouldResetList = page == 1 && query != currentQuery
        val currentList = if (shouldResetList) emptyList() else _search.value.data ?: emptyList()

        viewModelScope.launch {
            _search.emit(Resource.Loading())
        }

        tmdb.searchMovie(BuildConfig.TMDB_API_KEY, page = page, query = query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val listMovies = response.body()?.results ?: emptyList()
                    viewModelScope.launch {
                        _search.emit(Resource.Success(currentList + listMovies))
                        currentQuery = query // Cập nhật query hiện tại
                        pageSearch = page // Cập nhật trang hiện tại
                    }
                } else {
                    viewModelScope.launch {
                        _search.emit(Resource.Error("Lỗi API: ${response.code()} - ${response.message()}"))
                    }
                }
                isLoading = false
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                viewModelScope.launch {
                    _search.emit(Resource.Error("Không thể kết nối: ${t.message}"))
                }
                isLoading = false
            }
        })
    }

    fun fetchNextPage(query: String) {
        if (query == currentQuery) { // Chỉ tải tiếp nếu query không đổi
            searchMovies(query, pageSearch + 1)
        }
    }

    // Hàm reset để UI gọi nếu cần
    fun resetSearch() {
        pageSearch = 1
        currentQuery = ""
        viewModelScope.launch {
            _search.emit(Resource.Unspectified())
        }
    }
}