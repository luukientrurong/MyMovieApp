package com.example.movieapp.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.FavouriteAdapter
import com.example.movieapp.R
import com.example.movieapp.Untils.ItemSpacingDerco
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.FavouritesViewModel
import com.example.movieapp.databinding.FragmentFavoriteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class FavoriteFragment : Fragment(R.layout.fragment_favorite){
    lateinit var binding: FragmentFavoriteBinding
    private val adaterFav by lazy { FavouriteAdapter() }
    private val viewModel by viewModels<FavouritesViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRvFav()
        lifecycleScope.launch {
            viewModel.favouriteMovies.collect {
                when(it){
                    is Resource.Loading ->{
                        binding.progressBarFav.visibility = View.VISIBLE
                        Log.e("FavFagment","Loading")

                    }
                    is Resource.Success ->{
                        Log.e("FavFagment","success")
                        binding.progressBarFav.visibility = View.GONE
                        if(!it.data.isNullOrEmpty()){
                            binding.tvNoFav.visibility = View.GONE
                            adaterFav.diff.submitList(it.data)
                            Log.e("FavFagment","success co du lieu")

                        }else{
                            binding.tvNoFav.visibility = View.VISIBLE
                            Log.e("FavFagment","success ko du lieu")


                        }
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(),"Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                        binding.progressBarFav.visibility = View.GONE

                    }
                    else ->Log.e("FavFagment","else")

                }
            }
        }
        adaterFav.onClick = {
            val b = Bundle().apply {
                putInt("movieId",it.id)
            }
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.frameMovieActivity) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_favoriteFragment_to_movieDetailFragment, b)
        }
    }
    fun setupRvFav() {
        binding.rvFav.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = adaterFav
            addItemDecoration(ItemSpacingDerco(16))
        }
    }
}