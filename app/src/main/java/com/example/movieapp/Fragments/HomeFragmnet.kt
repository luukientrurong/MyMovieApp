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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.movieapp.Adapters.Film2ColAdapter
import com.example.movieapp.Adapters.GenreAdapter
import com.example.movieapp.Adapters.SildeAdapter
import com.example.movieapp.R
import com.example.movieapp.Untils.ItemSpacingDerco
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.HomeViewModel
import com.example.movieapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragmnet:Fragment(R.layout.fragment_home) {
    lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()
    private val adapterSilde by lazy { SildeAdapter() }
    private val adapterGenre by lazy { GenreAdapter() }
    private val adapterUpcoming by lazy { Film2ColAdapter() }
    private val adapterNowPlaying by lazy { Film2ColAdapter() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager2()
        lifecycleScope.launch {
            viewModel.moviesSilde.collect {
                when (it) {
                    is Resource.Success -> {
                        binding.progressBarSilde.visibility = View.GONE
                        adapterSilde.differ.submitList((it.data?.filter { !it.backdrop_path.isNullOrEmpty() }))
                    }

                    is Resource.Loading -> {
                        binding.progressBarSilde.visibility = View.VISIBLE
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                            .show()
                        binding.progressBarSilde.visibility = View.VISIBLE
                    }

                    else -> binding.progressBarSilde.visibility = View.VISIBLE
                }
//                list.forEach{
//                    viewModel.fetchMovieTrailer(it.id)
//
//                    viewModel.movieTrailer
//                    Log.e("GetMovie",it.title + " id: "+ it.id + " link anh:" + it.poster_path)
//                }
//                adapterSilde.differ.submitList(list.filter { !it.backdrop_path.isNullOrEmpty() })

            }
        }
        adapterSilde.onClick = {
            val b = Bundle().apply {
                putInt("movieId",it.id)
            }
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.frameMovieActivity) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_homeFragmnet_to_movieDetailFragment, b)
        }
        setupRvGenre()
        lifecycleScope.launch {
            viewModel.moviesGenre.collect {
                when (it) {
                    is Resource.Success -> {
                        adapterGenre.differ.submitList(it.data)
                    }

                    is Resource.Error -> Toast.makeText(
                        requireContext(),
                        it.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> Unit
                }
            }
        }
        adapterGenre.onClick = {
            val b = Bundle().apply {
                putParcelable("genre",it)
            }
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.frameMovieActivity) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_homeFragmnet_to_movieByGenreFragment, b)
        }
        lifecycleScope.launch {
            viewModel.movieTrailer.collect {
                if (it == null) {
                    Log.e("daw", "nullhfgh")
                } else {
                    Log.e("GetMovie", "trailer id: ${it.id}, trailer key: ${it.key}")
                }

            }
        }
        setupRvUpComing()
        lifecycleScope.launch {
            viewModel.moviesUpComing.collect {
                when (it) {
                    is Resource.Success -> {
                        adapterUpcoming.diff.submitList(it.data)
                        binding.progressBarUpComing.visibility = View.GONE
                    }

                    is Resource.Loading -> {
                        binding.progressBarUpComing.visibility = View.VISIBLE
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
        adapterUpcoming.onClick = {
            val b = Bundle().apply {
                putInt("movieId",it.id)
            }
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.frameMovieActivity) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_homeFragmnet_to_movieDetailFragment, b)
        }
        setupRvNowPlaying()
        lifecycleScope.launch {
            viewModel.moviesNowPlaying.collect {
                when (it) {
                    is Resource.Success -> {
                        adapterNowPlaying.diff.submitList(it.data)
                        binding.progressBarNowPlaying.visibility = View.GONE
                        Log.e("GetMovie","SL phim trong ds: ${it.data?.size}")
                    }

                    is Resource.Loading -> {
                        binding.progressBarNowPlaying.visibility = View.VISIBLE
                        delay(200)
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
        adapterNowPlaying.onClick = {
            val b = Bundle().apply {
                putInt("movieId",it.id)
            }
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.frameMovieActivity) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_homeFragmnet_to_movieDetailFragment, b)
        }
    }


    fun setupViewPager2() {
        binding.apply {
            viewpagerSilde.adapter = adapterSilde
            viewpagerSilde.orientation = ViewPager2.ORIENTATION_HORIZONTAL //huong`
            viewpagerSilde.clipToPadding = false
            viewpagerSilde.clipChildren = false
            viewpagerSilde.offscreenPageLimit = 3 // số lượng xuất hiện trên màn hình
            viewpagerSilde.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            val composite: CompositePageTransformer = CompositePageTransformer()
            composite.addTransformer(MarginPageTransformer(40))
            composite.addTransformer(ViewPager2.PageTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
                page.alpha = 0.4f + r * 0.6f

            })
            viewpagerSilde.setPageTransformer(composite)
            viewpagerSilde.setCurrentItem(1)
        }
    }

    fun setupRvGenre() {
        binding.rvGenre.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterGenre
            addItemDecoration(ItemSpacingDerco(16))
        }
    }

    fun setupRvUpComing() {
        binding.rvUpComing.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterUpcoming
            addItemDecoration(ItemSpacingDerco(16))
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItem = layoutManager.itemCount
                    val lastItemVisible = layoutManager.findLastVisibleItemPosition()
                    if(totalItem>0 && lastItemVisible >= totalItem-4){
                        viewModel.fetchNextMovieUpComing()
                    }
                }
            })
        }
    }

    fun setupRvNowPlaying() {
        binding.rvNowPlaying.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = adapterNowPlaying
            addItemDecoration(ItemSpacingDerco(0))
            //sự kiện khi cuộn
            binding.nestedScrollView.setOnScrollChangeListener { view, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) { // Cuộn xuống

                    if (!view.canScrollVertically(1)) {
                        viewModel.fetchNextMovieNowPlayingPage()
                        Toast.makeText(requireContext(), "da den cuoi chuan bi tai", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
}
