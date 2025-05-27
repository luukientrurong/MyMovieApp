package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Models.MyGenre
import com.example.movieapp.databinding.ItemGenreBinding

class GenreAdapter:RecyclerView.Adapter<GenreAdapter.genreViewHolder>() {
    inner class genreViewHolder(private val binding:ItemGenreBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(genre: MyGenre){
            binding.apply {
                tvGenreItem.text=genre.name
            }
        }
    }

    private val diffCallback = object:DiffUtil.ItemCallback<MyGenre>(){
        override fun areItemsTheSame(oldItem: MyGenre, newItem: MyGenre): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MyGenre, newItem: MyGenre): Boolean {
            return oldItem==newItem
        }

    }
    val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): genreViewHolder {
        return genreViewHolder(ItemGenreBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: genreViewHolder, position: Int) {
        val genre = differ.currentList[position]
        holder.bind(genre)
        holder.itemView.setOnClickListener {
            onClick?.invoke(genre)
        }
    }
    var onClick:((MyGenre)->Unit)?=null
}