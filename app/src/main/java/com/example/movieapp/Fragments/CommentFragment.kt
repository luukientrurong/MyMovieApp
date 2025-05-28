package com.example.movieapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.CommentAdapter
import com.example.movieapp.Models.Comment
import com.example.movieapp.Models.User
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.CommentViewModel
import com.example.movieapp.ViewModels.ProfileViewModel
import com.example.movieapp.ViewModels.SharedViewModel.UserSharedViewModel
import com.example.movieapp.databinding.FragmentCommentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CommentFragment  : Fragment() {
    lateinit var binding: FragmentCommentBinding
    @Inject
    lateinit var auth: FirebaseAuth
    lateinit var movieId:String
    private var commentList = mutableListOf<Comment>()
    private  val commentAdapter by lazy { CommentAdapter(auth) }
    private val commentViewModel by viewModels<CommentViewModel>()
    private val sharedViewModel by viewModels<UserSharedViewModel>()
    lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        movieId = arguments?.getString("movieId")?:"n"
        commentAdapter.diff.submitList(commentList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }
        commentViewModel.fecthComments(movieId)
        lifecycleScope.launch {
            sharedViewModel.user.collect{
                when(it){
                    is Resource.Success -> user = it.data!!
                    else ->Unit
                }
            }
        }

        lifecycleScope.launch {
            commentViewModel.comments.collect{
                when(it){
                    is Resource.Loading -> {
                        binding.tvNoCommnet.text = "Đang tải comment"
                    }
                    is Resource.Success -> {
                        commentList.clear()
                        commentList.addAll(it.data ?: emptyList())
                        if(commentList.isEmpty()){
                            binding.tvNoCommnet.visibility = View.VISIBLE
                        }else{
                            binding.tvNoCommnet.visibility = View.GONE
                        }
                        commentAdapter.notifyDataSetChanged()
                    }
                    is Resource.Error -> {
                        binding.tvNoCommnet.text = it.message
                        binding.tvNoCommnet.visibility = View.VISIBLE
                    }
                    else -> Unit
                }
            }
        }
        binding.editTextComment
        binding.imgSendCommnet.setOnClickListener {
            val commentText = binding.editTextComment.text.toString().trim()
            if(!commentText.isNullOrEmpty()){
                commentViewModel.sendComment(movieId,commentText,user)
                binding.editTextComment.text?.clear()
            }
        }
    }
}