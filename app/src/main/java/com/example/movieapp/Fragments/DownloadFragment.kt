package com.example.movieapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Activities.WatchMovieActivity
import com.example.movieapp.Adapters.DownloadedMoviesAdapter
import com.example.movieapp.R
import com.example.movieapp.ViewModels.DownloadViewModel
import com.example.movieapp.databinding.FragmentDownloadBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadFragment : Fragment(R.layout.fragment_download){
    private val viewModel by viewModels<DownloadViewModel>()
    private  val adapter by lazy { DownloadedMoviesAdapter() }
    private lateinit var binding: FragmentDownloadBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDownloadBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Khởi tạo RecyclerView
//        adapter = DownloadedMoviesAdapter { file ->
//            // Xử lý khi click vào item
//            Toast.makeText(requireContext(),"a", Toast.LENGTH_SHORT).show()
//        }
        binding.recyclerViewDownloads.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            this.adapter = this@DownloadFragment.adapter
        }
        viewModel.getAllFiles().observe(viewLifecycleOwner) { files ->
            Log.e("DatabaseCheck", "Files in database: ${files.size}")
            files.forEach { file ->
                Log.e("DatabaseCheck", "File: ${file.fileName}, ${file.filePath}, ${file.downloadTime}")
            }
           adapter.diff.submitList(files)
        }
        adapter.onClick ={
            val intent = Intent(requireContext(), WatchMovieActivity::class.java).apply {
                putExtra("filePath", it.filePath)
                putExtra("fileName", it.fileName)
                putExtra("isLocalFile", true)
            }
            startActivity(intent)
        }
    }
}