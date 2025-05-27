package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cloudinary.utils.ObjectUtils
import com.example.movieapp.Models.Comment
import com.example.movieapp.databinding.FragmentCommentBinding
import com.example.movieapp.databinding.ItemCommentBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CommentAdapter @Inject constructor(
    private val auth: FirebaseAuth
): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(comment: Comment){
            if(comment.userPhotoUrl.isNotEmpty()){
                Glide.with(itemView).load(comment.userPhotoUrl).into(binding.imageViewAvatar)
            }
            binding.textViewComment.text = comment.commentText
            binding.textViewUsername.apply {
                if (comment.userId==auth.currentUser?.uid){
                    text = "Tôi"
                }else{
                    text = comment.username
                }
            }
            binding.textViewTimestamp.text = formatTimestamp(comment.timestamp)
        }
    }
    private val diffUtil = object : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

    }
    val diff = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = diff.currentList[position]
        holder.bind(comment)
    }
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}