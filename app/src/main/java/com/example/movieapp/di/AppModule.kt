package com.example.movieapp.di

import android.content.Context
import com.example.movieapp.Interface.TMDBApiService
import com.example.movieapp.Models.database.AppDatabase
import com.example.movieapp.Repository.CloudinaryRepository
import com.example.movieapp.utils.PlayerManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton // chỉ tồn tại duy nhất trong project
    fun provideFirebaseAuth() =FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFireStore() = Firebase.firestore

    @Provides
    @Singleton
    fun provideRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    @Provides
    @Singleton
    fun provideTMDBApiService(retrofit: Retrofit):TMDBApiService{
        return retrofit.create(TMDBApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCloudinaryRepository(@ApplicationContext contex:Context):CloudinaryRepository = CloudinaryRepository(contex)

    @Provides
    @Singleton
    fun providePlayerManager(): PlayerManager {
        return PlayerManager()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
}