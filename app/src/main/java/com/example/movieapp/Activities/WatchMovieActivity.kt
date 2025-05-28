package com.example.movieapp.Activities

import android.app.ComponentCaller
import android.app.DownloadManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.movieapp.Adapters.ViewPagerAdapter
import com.example.movieapp.Fragments.CommentFragment
import com.example.movieapp.Fragments.RecommendFragment
import com.example.movieapp.Models.MovieDetail
import com.example.movieapp.Models.database.DownloadedFile
import com.example.movieapp.Models.database.DownloadedFileDao
import com.example.movieapp.R
import com.example.movieapp.Receiver.DownloadCompleteReceiver
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.WatchMovieViewModel
import com.example.movieapp.databinding.ActivityWatchMovieBinding
import com.example.movieapp.utils.PlayerManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class WatchMovieActivity : AppCompatActivity() {
    lateinit var binding: ActivityWatchMovieBinding
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private var movieDetail: MovieDetail? = null
    private var isFullscreen = false
    private var previousOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private var playerViewHeight = 0
    private val viewModel by viewModels<WatchMovieViewModel>()
    private var filePath: String? = null
    private var fileName: String? = null
    private var isLocalFile: Boolean = false

    private lateinit var url: String
    private lateinit var downloadReceiver: DownloadCompleteReceiver
    @Inject
    lateinit var playerManager: PlayerManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWatchMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        downloadReceiver = DownloadCompleteReceiver()
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(downloadReceiver, filter, RECEIVER_NOT_EXPORTED)
        }

        // Initialize ExoPlayer early
        player = ExoPlayer.Builder(this).build()
        playerManager.setCurrentPlayer(player)
        binding.playerView.player = player
        playerView = findViewById(R.id.playerView)

        // Lấy dữ liệu từ Intent
        isLocalFile = intent.getBooleanExtra("isLocalFile", false)
        filePath = intent.getStringExtra("filePath")?.substring(7)
        fileName = intent.getStringExtra("fileName")
        movieDetail = intent.getParcelableExtra<MovieDetail>("movie")

        binding.playerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                playerViewHeight = binding.playerView.height
                binding.playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        if (isLocalFile && filePath != null) {
            // Phát video cục bộ
            binding.tvTenPhim.text = fileName ?: "Phim đã tải"
            binding.iconDownload.visibility = View.GONE
            binding.viewPagerWathcMovie.visibility = View.GONE
            startMovie(filePath!!)
        } else if (movieDetail != null) {
            setUpViewPager()
            getDataAndPlay()
        } else {
            Toast.makeText(this, "Không có dữ liệu để phát", Toast.LENGTH_SHORT).show()
            finish()
        }

        Log.e("ActivityWathMovie", playerViewHeight.toString())

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.iconDownload.setOnClickListener {
            if (movieDetail != null && !url.isNullOrEmpty()) {
                downloadMovie(movieDetail!!.title, url)
            } else {
                Toast.makeText(this, "Không có link phim để tải", Toast.LENGTH_SHORT).show()
            }
        }
        binding.imgIconBack.setOnClickListener {
            this@WatchMovieActivity.finish()
        }
    }

    private fun startMovie(url: String) {
        binding.progressBarLoadUrlFilm.visibility = View.GONE
        Log.e("WatchCheckLoad","da vao startMovie voi url $url")
        val uri = if (isLocalFile) {
            // Đường dẫn tệp cục bộ
            Uri.fromFile(File(url))
        } else {
            // URL trực tuyến
            Uri.parse(url)
        }

        val dataSourceFactory = if (isLocalFile) {
            DefaultDataSource.Factory(this)
        } else {
            DefaultHttpDataSource.Factory()
        }
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))

        // Prepare the player
        player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.playWhenReady = true
        setupCustomControls()
        //xử lý lỗi
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    this@WatchMovieActivity,
                    "Error: " + error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE ->
                        Log.e("WatchCheckLoad","idle")

                    Player.STATE_BUFFERING ->
                        Log.e("WatchCheckLoad","buffering")

                    Player.STATE_READY ->
                        Log.e("WatchCheckLoad","ready")

                    Player.STATE_ENDED ->
                        Log.e("WatchCheckLoad","ended")

                }

            }
        })
    }
    private fun getDataAndPlay(){
        movieDetail = intent.getParcelableExtra<MovieDetail>("movie")
        setUpViewPager()
        binding.tvTenPhim.text = movieDetail!!.title
        viewModel.fetchMovieUrl(movieDetail!!.id.toString())
        lifecycleScope.launch {
            viewModel.movieUrl.collect {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressBarLoadUrlFilm.visibility = View.VISIBLE
                        binding.iconDownload.visibility = View.GONE
                        Log.e("WatchCheckLoad","dang load")
                    }

                    is Resource.Success -> {
                        binding.progressBarLoadUrlFilm.visibility = View.GONE
                        binding.imgErrorVideo.visibility = View.GONE
                        binding.iconDownload.visibility = View.VISIBLE
                        Log.e("WatchCheckLoad","thanh cong")
                        url = it.data!!
                        startMovie(it.data!!)

                    }

                    is Resource.Error -> {
                        binding.progressBarLoadUrlFilm.visibility = View.GONE
                        binding.imgErrorVideo.visibility = View.VISIBLE
                        Log.e("WatchCheckLoad","loi")

                    }
                    else -> {Unit
                        Log.e("WatchCheckLoad","ko co gi")
                    }
                }
            }
        }
    }

    private fun setupCustomControls() {
        //Rewind
        playerView.findViewById<ImageButton>(R.id.exo_rewind).setOnClickListener {
            val currentPos = player.currentPosition
            player.seekTo((currentPos - 10000).coerceAtLeast(0))
        }
        //Forward
        playerView.findViewById<ImageButton>(R.id.exo_fast_forward).setOnClickListener {
            val currentPos = player.currentPosition
            player.seekTo((currentPos + 10000).coerceAtMost(player.duration))
        }
        //Fullscreen
        playerView.findViewById<ImageButton>(R.id.exo_fullscreen_exit_fullscreen)
            .setOnClickListener {
                toggleFullscreen()
                Log.e("ActivityWathMovie", "Full screen")
            }
        //Pip
        playerView.findViewById<ImageButton>(R.id.exo_pip).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            }
        }
    }

    private fun makeFullscreen() {
        binding.imgIconBack.visibility = View.GONE
        binding.playerView.layoutParams.apply {
            width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
        }
        binding.playerView.requestLayout()
    }

    private fun exitFullScreen() {
        binding.imgIconBack.visibility = View.VISIBLE
        binding.playerView.layoutParams.apply {
            width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            height = playerViewHeight
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.playerView.requestLayout()

    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        val btnFullscreen =
            playerView.findViewById<ImageButton>(R.id.exo_fullscreen_exit_fullscreen)
        btnFullscreen.isSelected = isFullscreen
        if (isFullscreen) {
            previousOrientation = requestedOrientation
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }

            } else {
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }
            makeFullscreen()
        } else {
            requestedOrientation = previousOrientation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
            exitFullScreen()

        }

    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        playerView.useController = !isInPictureInPictureMode
        //an nut fullscreen
        val fullCreenBtn = playerView.findViewById<ImageButton>(R.id.exo_fullscreen_exit_fullscreen)
        if (isInPictureInPictureMode) {
            fullCreenBtn.visibility = View.GONE
            makeFullscreen()
        } else {
            fullCreenBtn.visibility = View.VISIBLE
            exitFullScreen()
        }
        //thoat fullscreen
        if (isInPictureInPictureMode && isFullscreen) {
            isFullscreen = false
            fullCreenBtn.isSelected = false
            requestedOrientation = previousOrientation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }

    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Giải phóng player cũ và tạo player mới
        playerManager.releasePlayer()
        player = ExoPlayer.Builder(this).build()
        playerManager.setCurrentPlayer(player)
        binding.playerView.player = player
        getDataAndPlay()
    }

    private fun downloadMovie(title: String, url: String) {
        var titleSave = title.replace(Regex("[^a-zA-Z0-9]"), "_")
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Đang tải: $titleSave")
            .setDescription("Đang tải phim về thiết bị...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_MOVIES, "$titleSave.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)


        val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Bắt đầu tải phim: $title", Toast.LENGTH_SHORT).show()
    }

    private fun setUpViewPager(){
        val movieId = movieDetail?.id.toString()
        val commentFragment = CommentFragment().apply {
            arguments = Bundle().apply {
                putString("movieId", movieId)
            }
        }
        val recommendFragment = RecommendFragment().apply {
            arguments = Bundle().apply {
                putString("movieId",movieId)
            }
        }
        val fragments = listOf(
            commentFragment,
            recommendFragment
        )
        binding.viewPagerWathcMovie.adapter = ViewPagerAdapter(this, fragments)
        val tabTitles = listOf("Bình luận","Đề xuất" )
        TabLayoutMediator(
            binding.tabLayoutWathcMovie,
            binding.viewPagerWathcMovie
        ) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode) {
            player.playWhenReady = true
        } else {
            player.playWhenReady = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (playerManager.getCurrentPlayer() == player) {
            playerManager.releasePlayer()
        }
        unregisterReceiver(downloadReceiver)
    }
}