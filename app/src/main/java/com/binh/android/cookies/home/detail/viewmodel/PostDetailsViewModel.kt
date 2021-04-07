package com.binh.android.cookies.home.detail.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.binh.android.cookies.data.Comment
import com.binh.android.cookies.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private const val TAG = "PostDetailsViewModel"

class PostDetailsViewModel(postId: String, application: Application) :
    AndroidViewModel(application) {
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val user = FirebaseAuth.getInstance().currentUser
    var post = MutableLiveData<Post>()

    private val postData = databaseRef.child("posts").child(postId)

    var comment: String = ""

    init {
        postData.get().addOnSuccessListener {
            post.value = it.getValue(Post::class.java)!!
        }
    }

    fun submitComment() {
        val commentRef = databaseRef.child("/comments").push()
        val pushId = commentRef.key

        val comment = Comment(
            pushId!!,
            post.value!!.postId,
            user!!.uid,
            comment,
            0
        )


        commentRef.setValue(comment).addOnCompleteListener {
            if (it.isSuccessful) Log.d(TAG, "Successful add comment")
        }
    }
}