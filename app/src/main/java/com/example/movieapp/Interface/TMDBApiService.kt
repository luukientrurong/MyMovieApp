package com.example.movieapp.Interface

import com.example.movieapp.Models.MovieResponse
import com.example.movieapp.Models.MovieTrailerResponse
import com.example.movieapp.Models.GenreResponse
import com.example.movieapp.Models.MovieDetail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApiService {
    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<MovieResponse>

    @GET("movie/{movie_id}/videos")
    fun getMovieTrailer(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey:String
    ):Call<MovieTrailerResponse>

    @GET("genre/movie/list")
    fun getGenre(
        @Query("api_key") apiKey: String,
    ):Call<GenreResponse>

    @GET("movie/upcoming")
    fun getUpComingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<MovieResponse>
    @GET("movie/now_playing")
    fun getNowPlaying(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<MovieResponse>

    @GET("search/movie")
    fun searchMovie(
        @Query("api_key") apiKey:String,
        @Query("page") page: Int =1,
        @Query("query") query:String
    ):Call<MovieResponse>
    @GET("discover/movie")
    fun getMovieByGenre(
        @Query("api_key") apiKey: String,
        @Query("language") language: String="en-US",
        @Query("page") page: Int=1,
        @Query("with_genres") genreId:Int
    ):Call<MovieResponse>
    @GET("movie/{movie_id}")
    fun  getMovieDetails(
        @Path("movie_id") movieId:Int,
        @Query("api_key") apiKey:String
    ):Call< MovieDetail>
}