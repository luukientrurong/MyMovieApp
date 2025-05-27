package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Models.Company
import com.example.movieapp.Models.Movie
import com.example.movieapp.R
import com.example.movieapp.databinding.ItemCompanyInMovieDetailBinding
import com.example.movieapp.databinding.ItemFilm2ColBinding

class CompanyAdapter : RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {
    inner class CompanyViewHolder(private val binding: ItemCompanyInMovieDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(company: Company) {
            binding.apply {
                Glide.with(itemView).load("https://image.tmdb.org/t/p/w780${company!!.logo_path}").into(imgLogoCompany)
                tvTenCompany.text = company.name
            }
        }

    }
    private val diffUtil = object : DiffUtil.ItemCallback<Company>() {
        override fun areItemsTheSame(oldItem: Company, newItem: Company): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Company, newItem: Company): Boolean {
            return oldItem == newItem
        }
    }
    val diff = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(ItemCompanyInMovieDetailBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = diff.currentList[position]
        holder.bind(company)
        holder.itemView.setOnClickListener {
            onClick?.invoke(company)
        }
    }
    var onClick:((Company)->Unit)? = null
}