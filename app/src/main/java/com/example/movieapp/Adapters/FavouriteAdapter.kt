package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Models.Movie
import com.example.movieapp.Models.MovieDetail
import com.example.movieapp.Models.database.DownloadedFile
import com.example.movieapp.databinding.ItemFavouriteBinding

class FavouriteAdapter : RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {
    inner class FavouriteViewHolder(private val binding: ItemFavouriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: MovieDetail) {
            binding.apply {
                Glide.with(itemView).load("https://image.tmdb.org/t/p/w780${movie!!.poster_path}").into(imgFavItem)
                tvTitleFavItem.text = movie.title
                tvOriginalTitleFavItem.text = "Tên gốc: " + movie.original_title
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<MovieDetail>() {
        override fun areItemsTheSame(
            oldItem: MovieDetail,
            newItem: MovieDetail
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MovieDetail,
            newItem: MovieDetail
        ): Boolean {
            return oldItem == newItem
        }

    }
    val diff = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouriteViewHolder {
        return FavouriteViewHolder(ItemFavouriteBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(
        holder: FavouriteViewHolder,
        position: Int
    ) {
        val movie = diff.currentList[position]
        holder.bind(movie)
        holder.itemView.setOnClickListener {
            onClick?.invoke(movie)
        }
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    var onClick:((MovieDetail)->Unit)? = null
}