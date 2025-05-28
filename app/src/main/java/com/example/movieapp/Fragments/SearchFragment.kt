package com.example.movieapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.movieapp.Adapters.SearchMovieAdapter
import com.example.movieapp.R
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.SearchViewModel
import com.example.movieapp.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment: Fragment(R.layout.fragment_search) {
    lateinit var binding:FragmentSearchBinding
    private val viewModel by viewModels<SearchViewModel>()
    private val searchAdapter by lazy { SearchMovieAdapter() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRvSearch()
        //xu lý sk khi text thay đổi
        binding.edtSearch.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //trước khi text thay đổi trên màn hình
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //hiển thị icon clear khi có ít nhat 1 ký tự
                binding.iconClear.visibility = if(s.isNullOrEmpty()) View.GONE else View.VISIBLE
                //tìm kiếm
                viewModel.searchMovies(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {
                //sau khi text trên màn hình đổi
            }

        })
        lifecycleScope.launch {
            viewModel.search.collect{
                when(it){
                    is Resource.Loading ->{ binding.progressbarSearch.visibility = View.VISIBLE }
                    is Resource.Success -> {binding.progressbarSearch.visibility = View.GONE
                        searchAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error -> {binding.progressbarSearch.visibility = View.GONE
                        Toast.makeText(requireContext(),"loi: ${it.message}",Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
        //clear text
        binding.iconClear.setOnClickListener {
            binding.edtSearch.text.clear()
        }
        searchAdapter.onClick = {
            val b = Bundle().apply {
                putInt("movieId",it.id)
            }
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.frameMovieActivity) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_searchFragment_to_movieDetailFragment, b)
        }
    }
    private fun setupRvSearch(){
        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = searchAdapter
            setPadding(0,12,0,12)
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastItemVisible = layoutManager.findLastVisibleItemPosition()
                    val totalItem = layoutManager.itemCount
                    if(lastItemVisible >= totalItem -4){
                        viewModel.fetchNextPage(binding.edtSearch.text.toString())
                    }
                }
            })
        }
    }
}