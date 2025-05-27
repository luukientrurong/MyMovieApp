package com.example.movieapp.Adapters

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Models.Movie
import com.example.movieapp.Models.database.DownloadedFile
import com.example.movieapp.R
import com.example.movieapp.databinding.ItemDownloadedMovieBinding

class DownloadedMoviesAdapter() : RecyclerView.Adapter<DownloadedMoviesAdapter.DownloadViewHolder>() {

    class DownloadViewHolder(private val binding: ItemDownloadedMovieBinding, ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: DownloadedFile) {
            binding.apply {
                movieName.text = file.fileName
                // Tạo thumbnail từ video
                val thumbnail = getVideoThumbnail(file.filePath)
                Glide.with(root.context)
                    .load(thumbnail ?: R.drawable.default_movie_img)
                    .into(movieThumbnail)

            }
        }

        private fun getVideoThumbnail(filePath: String): Bitmap? {
            return try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(binding.root.context, Uri.parse(filePath))
                // Lấy frame tại giây thứ 1 (1 000 000 microsecond = 1 giây)
                val bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST)
                retriever.release()
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
    private val diffUtil = object : DiffUtil.ItemCallback<DownloadedFile>(){
        override fun areItemsTheSame(
            oldItem: DownloadedFile,
            newItem: DownloadedFile
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DownloadedFile,
            newItem: DownloadedFile
        ): Boolean {
            return oldItem==newItem
        }

    }
    val diff = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        return DownloadViewHolder(ItemDownloadedMovieBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        val movieDownload = diff.currentList[position]
        holder.bind(movieDownload)
        holder.itemView.setOnClickListener {
            onClick?.invoke(movieDownload)
        }
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }
    var onClick:((DownloadedFile)->Unit)? = null

}