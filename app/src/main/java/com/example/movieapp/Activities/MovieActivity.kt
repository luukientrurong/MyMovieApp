package com.example.movieapp.Activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.movieapp.Fragments.DownloadFragment
import com.example.movieapp.Fragments.FavoriteFragment
import com.example.movieapp.Fragments.HomeFragmnet
import com.example.movieapp.Fragments.MovieDetailFragment
import com.example.movieapp.Fragments.ProfileFragment
import com.example.movieapp.Fragments.SearchFragment
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityMovieBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieActivity : AppCompatActivity() {
    lateinit var binding: ActivityMovieBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navController = findNavController(R.id.frameMovieActivity)
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.movieByGenreFragment, R.id.movieDetailFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.homeFragmnet, R.id.downloadFragment, R.id.favoriteFragment,
                R.id.searchFragment, R.id.profileFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }

}