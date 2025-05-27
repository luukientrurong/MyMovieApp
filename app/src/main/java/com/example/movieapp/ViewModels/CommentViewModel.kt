package com.example.movieapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.Models.Comment
import com.example.movieapp.Models.User
import com.example.movieapp.Untils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val dbRef: DatabaseReference,
    private val auth:FirebaseAuth
) : ViewModel() {
    private val _comments = MutableStateFlow<Resource<List<Comment>>>(Resource.Unspectified())
    val comments: Flow<Resource<List<Comment>>> = _comments

    fun fecthComments(movieId: String) {
        viewModelScope.launch {
            _comments.emit(Resource.Loading())
        }
        val dataRef = dbRef.child("comments").child(movieId)
        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<Comment>()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    comment?.let {
                        commentList.add(comment)
                    }
                    commentList.sortByDescending { it.timestamp }
                    viewModelScope.launch {
                        _comments.emit(Resource.Success(commentList))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                viewModelScope.launch {
                    _comments.emit(Resource.Error(error.message))
                }
            }

        })
    }
    fun sendComment(movieId:String,commentText:String,user:User){
        val currentUser = auth.currentUser
        val comment = Comment(
            userId = currentUser!!.uid,
            username = user.userName ?:"Ẩn danh",
            userPhotoUrl = user.imgPath,
            commentText = commentText,
            timestamp = System.currentTimeMillis()
        )
        val commentId = dbRef.child("comments").child(movieId).push().key?:return
        Log.e("sendComment",commentId)
        dbRef.child("comments").child(movieId).child(commentId).setValue(comment)
    }
}