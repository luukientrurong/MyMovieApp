package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.movieapp.Models.Movie
import com.example.movieapp.R
import com.example.movieapp.databinding.ItemFilm2ColBinding

class Film2ColAdapter : RecyclerView.Adapter<Film2ColAdapter.Film2ColViewHolder>() {
    inner class Film2ColViewHolder(private val binding: ItemFilm2ColBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.apply {
                if (movie.poster_path.isNullOrEmpty()){
                    imgMovieImg.setImageResource(R.drawable.default_movie_img)
                }else{
                    Glide.with(itemView).load("https://image.tmdb.org/t/p/w780${movie!!.poster_path}").into(imgMovieImg)
                }
                tvTenPhim.text = movie.title
            }
        }

    }
    //so sánh ds cũ với mới giúp cập nhật ds ko cần làm mới toàn bộ
    private val diffUtil = object : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
    val diff = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Film2ColViewHolder {
        return Film2ColViewHolder(ItemFilm2ColBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: Film2ColViewHolder, position: Int) {
        val movie = diff.currentList[position]
        holder.bind(movie)
        holder.itemView.setOnClickListener {
            onClick?.invoke(movie)
        }
    }
    var onClick:((Movie)->Unit)? = null
}