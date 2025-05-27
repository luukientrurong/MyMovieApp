package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.movieapp.Models.Movie
import com.example.movieapp.databinding.ItemMovieSearchBinding

class SearchMovieAdapter:RecyclerView.Adapter<SearchMovieAdapter.SearchMovieViewHolder>() {
    inner class SearchMovieViewHolder(private val binding:ItemMovieSearchBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(movie: Movie?) {
            binding.apply {
                Glide.with(itemView).load("https://image.tmdb.org/t/p/w780${movie!!.poster_path}").into(imgSearchItem)
                tvTitleSearchItem.text = movie.title
                tvOriginalTitleSearchItem.text = "Tên gốc: " + movie.original_title
            }
        }

    }

    val diffUltil = object :DiffUtil.ItemCallback<Movie>(){
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,diffUltil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchMovieViewHolder {
        return SearchMovieViewHolder(ItemMovieSearchBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SearchMovieViewHolder, position: Int) {
        val movie = differ.currentList[position]
        holder.bind(movie)
    }
}