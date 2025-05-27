package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.movieapp.Models.Movie
import com.example.movieapp.databinding.ItemSildeHomeBinding

class SildeAdapter:RecyclerView.Adapter<SildeAdapter.SildeViewHolder>() {
    inner class SildeViewHolder(private val binding:ItemSildeHomeBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(movie: Movie?) {

            binding.apply {
                val requestOption = RequestOptions().transform(RoundedCorners(16))
                Glide.with(itemView).load("https://image.tmdb.org/t/p/w780${movie!!.backdrop_path}").apply(requestOption).into(imageView)
            }
        }

    }
    private val diffCallback = object :DiffUtil.ItemCallback<Movie>(){
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem==newItem
        }

    }
    val differ = AsyncListDiffer(this,diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SildeViewHolder {
        return SildeViewHolder(ItemSildeHomeBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SildeViewHolder, position: Int) {
        val movie = differ.currentList[position]
        holder.bind(movie)
        holder.itemView.setOnClickListener {
            onClick?.invoke(movie)
        }

    }
    var onClick: ((Movie)->Unit)? = null
}