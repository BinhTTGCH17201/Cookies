package com.binh.android.cookies.home.detail.viewmodel

import android.util.Log
import androidx.lifecycle.*
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

class PostDetailsViewModel(postId: String) : ViewModel() {
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val user = FirebaseAuth.getInstance().currentUser
    var post = MutableLiveData<Post>()

    private val postData = databaseRef.child("posts").child(postId)

    private val _thisPostLiked = MutableLiveData<Boolean>()
    val thisPostLiked: LiveData<Boolean>
        get() = _thisPostLiked

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean>
        get() = _isLoggedIn

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean>
        get() = _isAdmin

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        viewModelScope.launch(Dispatchers.IO) {
            user?.let {
                _isLoggedIn.postValue(true)
                val userDb =
                    FirebaseDatabase.getInstance().reference.child("users/${user.uid}/admin").get()
                        .await().getValue(Boolean::class.java)
                userDb?.let {
                    _isAdmin.postValue(it)
                }
            } ?: kotlin.run {
                _isLoggedIn.postValue(false)
                _isAdmin.postValue(false)
            }
        }
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

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
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
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

    @Suppress("UNCHECKED_CAST")
    class PostDetailsViewModelFactory constructor(
        private val postId: String,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostDetailsViewModel::class.java)) {
                return PostDetailsViewModel(postId) as T
            }
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}