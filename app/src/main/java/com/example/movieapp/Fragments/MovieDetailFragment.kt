package com.example.movieapp.Fragments

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieapp.Activities.WatchMovieActivity
import com.example.movieapp.Adapters.CompanyAdapter
import com.example.movieapp.Adapters.GenreAdapter
import com.example.movieapp.Models.MovieDetail
import com.example.movieapp.R
import com.example.movieapp.Untils.Resource
import com.example.movieapp.Untils.ItemSpacingDerco
import com.example.movieapp.ViewModels.MovieDetailViewModel
import com.example.movieapp.ViewModels.SharedViewModel.MovieFavouritesSharedViewModel
import com.example.movieapp.databinding.FragmentMovieDetailBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {
    private lateinit var binding: FragmentMovieDetailBinding
    private val genreAdapter by lazy { GenreAdapter() }
    private val companyAdapter by lazy { CompanyAdapter() }
    private val viewModel by viewModels<MovieDetailViewModel>()
    private val arg by navArgs<MovieDetailFragmentArgs>()
    private var trailerUrl: String? = null
    private lateinit var youtubePlayer: YouTubePlayer
    private lateinit var openYouTubeLauncher: ActivityResultLauncher<Intent>
    private lateinit var movieDetail: MovieDetail
    private val favSharedViewModel by activityViewModels<MovieFavouritesSharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openYouTubeLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        initializeYouTubePlayer()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MovieDetailFragment", "movieId: ${arg.movieId}")
        setupRvGenre()
        setupRvCompany()
        viewModel.fetchMovieDetail(arg.movieId)
        viewModel.fetchMovieTrailer(arg.movieId)

        // Thêm YouTubePlayerView vào vòng đời
        lifecycle.addObserver(binding.youtubePlayerView)

        // Lắng nghe chi tiết phim
        lifecycleScope.launch {
            viewModel.movieDetail.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { detail ->
                            setupAll(detail)
                            movieDetail = detail
                            genreAdapter.differ.submitList(detail.genres)
                            companyAdapter.diff.submitList(detail.production_companies)
                            favSharedViewModel.isFavourited(movieDetail.id.toString())
                        }
                    }

                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Lỗi: ${resource.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> Unit
                }
            }
        }

        // Lắng nghe trailer
        lifecycleScope.launch {
            viewModel.movieTrailer.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        trailerUrl = resource.data?.key
                        Log.d("MovieDetailFragment", "trailerUrl: $trailerUrl")
                    }

                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Lỗi tải trailer: ${resource.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> Unit
                }
            }
        }

        // Xử lý nút Back
        binding.imgBackDetail.setOnClickListener {
            findNavController().navigateUp()
        }


        // Xử lý nút Trailer
        binding.btnTrailer.setOnClickListener {
            if (!isNetworkAvailable()) {
                Toast.makeText(requireContext(), "Không có kết nối mạng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!trailerUrl.isNullOrEmpty() && trailerUrl!!.matches(Regex("[a-zA-Z0-9_-]{11}"))) {
                binding.imgMovieDetail.visibility = View.GONE
                binding.youtubePlayerView.visibility = View.VISIBLE
                binding.btnCloseVideo?.visibility = View.VISIBLE
                youtubePlayer?.loadVideo(trailerUrl!!, 0f)
            } else {
                Log.e("MovieDetailFragment", "Invalid trailerUrl: $trailerUrl")
                Toast.makeText(
                    requireContext(),
                    "Không tìm thấy trailer hợp lệ. Mở YouTube?",
                    Toast.LENGTH_SHORT
                ).show()
                openInYouTubeApp()
            }
        }

        // Xử lý nút đóng video
        binding.btnCloseVideo?.setOnClickListener {
            resetPlayerView()
        }
        //Xử lí nút xem phim
        binding.btnPlayFilmDetail.setOnClickListener {
            val intent = Intent(requireContext(), WatchMovieActivity::class.java)
            intent.putExtra("movie", movieDetail)
            startActivity(intent)
        }
        // Quan sát LiveData để cập nhật UI
        favSharedViewModel.isFavourite.observe(viewLifecycleOwner) { isFavourite ->
            binding.imgFavoriteDetail.setImageResource(
                if (isFavourite) {
                    R.drawable.icon_fav

                }else{
                    R.drawable.icon_non_fav
                }
            )
        }

        // Xử lý nút yêu thích
        binding.imgFavoriteDetail.setOnClickListener {
            if (::movieDetail.isInitialized) { // Kiểm tra movieDetail đã khởi tạo
                if (favSharedViewModel.isFavourite.value == true) {
                    favSharedViewModel.deleteFavourite(movieDetail.id.toString())
                    Toast.makeText(requireContext(), "Xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show()
                } else {
                    favSharedViewModel.addFavourite(movieDetail.id.toString())
                    Toast.makeText(requireContext(), "Thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Đang tải thông tin phim, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupRvGenre() {
        binding.rvGenreDetail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = genreAdapter
            addItemDecoration(ItemSpacingDerco(16))
        }
    }

    private fun setupRvCompany() {
        binding.rvListCompanyMovieDetail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = companyAdapter
            addItemDecoration(ItemSpacingDerco(20))
        }
    }

    private fun setupAll(movieDetail: MovieDetail) {
        binding.apply {
            Glide.with(requireContext())
                .load("https://image.tmdb.org/t/p/w780${movieDetail.backdrop_path}")
                .into(imgMovieDetail)
            tvTenPhimDetail.text = movieDetail.title
            tvVote.text = "${movieDetail.vote_average} (${movieDetail.vote_count})"
            tvOverview.text = movieDetail.overview
            tvTime.text = "${movieDetail.runtime} phút"
        }
    }

    private fun initializeYouTubePlayer() {
        lifecycle.addObserver(binding.youtubePlayerView)
        binding.youtubePlayerView.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youtubePlayer = player
            }

            override fun onError(player: YouTubePlayer, error: PlayerConstants.PlayerError) {
                Log.e("MovieDetailFragment", "YouTube Player Error: $error")
                val errorMessage = when (error) {
                    PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> "Yêu cầu không hợp lệ, có thể do cấu hình API"
                    PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> "Video không hỗ trợ phát trong ứng dụng"
                    PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> "Không tìm thấy video"
                    else -> "Lỗi phát video: $error"
                }
                Toast.makeText(
                    requireContext(),
                    "$errorMessage. Mở trong YouTube?",
                    Toast.LENGTH_LONG
                ).show()
                openInYouTubeApp()
                resetPlayerView()
            }
        })
    }

    private fun openInYouTubeApp() {
        if (!trailerUrl.isNullOrEmpty()) {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$trailerUrl"))
            try {
                openYouTubeLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e("MovieDetailFragment", "Error opening YouTube app: $e")
                Toast.makeText(
                    requireContext(),
                    "Không tìm thấy ứng dụng YouTube",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resetPlayerView() {
        youtubePlayer.pause()
        binding.imgMovieDetail.visibility = View.VISIBLE
        binding.youtubePlayerView.visibility = View.GONE
        binding.btnCloseVideo?.visibility = View.GONE
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.youtubePlayerView.release()
    }
}