package com.example.movieapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Adapters.Film2ColAdapter
import com.example.movieapp.R
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.MovieByGenreViewModel
import com.example.movieapp.databinding.FragmentMovieByGenreBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieByGenreFragment:Fragment(R.layout.fragment_movie_by_genre) {
    lateinit var binding: FragmentMovieByGenreBinding
    private val args by navArgs<MovieByGenreFragmentArgs>()
    private val film2ColApdater by lazy { Film2ColAdapter ()}
    private val viewModel by viewModels<MovieByGenreViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieByGenreBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val genre = args.genre
        setupFilm2ColAdapter()
        binding.imgBackMovieByGenre.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvGenre.text = genre.name
        viewModel.fetchMovieByGenre(genre.id.toInt())
        lifecycleScope.launch {
            viewModel.movieByGenre.collect{
                when(it){
                    is Resource.Success->{film2ColApdater.diff.submitList(it.data)
                    Toast.makeText(requireContext(),"tai thanh cong: "+genre.name+", id: "+genre.id,Toast.LENGTH_SHORT).show()
                        binding.progressBarMovieByGenre.visibility=View.GONE}
                    is Resource.Error->{Toast.makeText(requireContext(),"loi: "+it.message,Toast.LENGTH_SHORT).show()
                        binding.progressBarMovieByGenre.visibility=View.GONE}
                    is Resource.Loading ->{binding.progressBarMovieByGenre.visibility=View.VISIBLE}
                    else->Unit
                }
            }
        }
        film2ColApdater.onClick = {
            val b = Bundle().apply {
                putInt("movieId",it.id)
            }
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.frameMovieActivity) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_movieByGenreFragment_to_movieDetailFragment, b)
        }

    }

    private fun setupFilm2ColAdapter() {
        binding.rvMovieByGenre.apply {
            adapter = film2ColApdater
            layoutManager = GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false)
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItem = layoutManager.itemCount
                    val lastItemVisible = layoutManager.findLastVisibleItemPosition()
                    if(totalItem>0 && lastItemVisible >= totalItem-4){
                        viewModel.fetchNextPage()
                    }
                }
            })
        }
    }
}