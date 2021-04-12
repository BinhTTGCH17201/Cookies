package com.binh.android.cookies.home.detail.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.binh.android.cookies.data.Comment
import com.binh.android.cookies.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "PostDetailsViewModel"

class PostDetailsViewModel(postId: String, application: Application) :
    AndroidViewModel(application) {
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val user = FirebaseAuth.getInstance().currentUser
    var post = MutableLiveData<Post>()

    private val postData = databaseRef.child("posts").child(postId)

    private val _thisPostLiked = MutableLiveData<Boolean>()
    val thisPostLiked: LiveData<Boolean>
        get() = _thisPostLiked

    var commentContent = MutableLiveData<String>()

    var likeNumber = MutableLiveData<Int>()

    private val valueEventListenerPost: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                post.value = snapshot.getValue(Post::class.java)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "Fail to get data with error:\n $error")
        }
    }

    private val likeNumberListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            likeNumber.value = snapshot.getValue(Int::class.java) ?: run { 0 }
        }

        override fun onCancelled(error: DatabaseError) {
            likeNumber.value = 0
            Log.e("PostDetailsViewModel", "Fail to get like number!")
        }
    }

    init {
        postData.addValueEventListener(valueEventListenerPost)
        postData.child("like").addValueEventListener(likeNumberListener)

        viewModelScope.launch(Dispatchers.IO) {
            post.postValue(postData.get().await().getValue(Post::class.java))
            setUpBinding()
        }
    }

    private suspend fun setUpBinding() {
        withContext(Dispatchers.Main) {
            post.value?.let {
                getPostLiked(it.postId)
                likeNumber.value = it.like
            }
            Log.d(TAG, "Like number: ${likeNumber.value}")
            commentContent.value = ""
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private suspend fun getPostLiked(postId: String) {
        withContext(Dispatchers.IO) {
            user?.let {
                val doesPostLiked = async {
                    FirebaseDatabase.getInstance().reference.child("users/${user.uid}/likedPosts/${postId}")
                        .get().await().exists()
                }
                Log.d("PostLikeTest", "Post like: ${doesPostLiked.await()}")
                _thisPostLiked.postValue(doesPostLiked.await())
            }
        }
    }

    fun postLiked() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLike = databaseRef.child("posts/${post.value?.postId}/like").get().await()
                .getValue(Int::class.java) ?: 0
            databaseRef.child("posts/${post.value?.postId}/like").setValue(currentLike + 1).await()
            databaseRef.child("users/${user?.uid}/likedPosts/${post.value?.postId}").setValue(true)
                .await()
            _thisPostLiked.postValue(true)
        }
    }

    fun postUnlike() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLike = databaseRef.child("posts/${post.value?.postId}/like").get().await()
                .getValue(Int::class.java) ?: 0
            databaseRef.child("posts/${post.value?.postId}/like").setValue(currentLike - 1).await()
            databaseRef.child("users/${user?.uid}/likedPosts/${post.value?.postId}").removeValue()
                .await()
            _thisPostLiked.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        postData.run {
            removeEventListener(valueEventListenerPost)
            removeEventListener(likeNumberListener)
        }
    }

    fun submitComment() {
        viewModelScope.launch(Dispatchers.IO) {
            val commentRef = databaseRef.child("/comments").push()
            val pushId = commentRef.key

            val comment = Comment(
                pushId!!,
                post.value!!.postId,
                user!!.uid,
                commentContent.value.toString(),
                0
            )

            commentRef.setValue(comment).addOnCompleteListener {
                if (it.isSuccessful) Log.d(TAG, "Successful add comment")
                commentContent.postValue("")
            }
        }
    }
}