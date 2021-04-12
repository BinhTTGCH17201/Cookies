package com.binh.android.cookies.home.newpost.viewmodel

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.binh.android.cookies.data.Post
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val TAG = "NewPostViewModel"

class NewPostViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

    private val _onUpload = MutableLiveData<Boolean?>()
    val onUpload: LiveData<Boolean?>
        get() = _onUpload

    private val _onUploadImage = MutableLiveData<Boolean?>()
    val onUploadImage: LiveData<Boolean?>
        get() = _onUploadImage

    private val _uploadSuccess = MutableLiveData<Boolean?>()
    val uploadSuccess: LiveData<Boolean?>
        get() = _uploadSuccess

    private val _deletedPost = MutableLiveData<Boolean?>()
    val deletedPost: LiveData<Boolean?>
        get() = _deletedPost

    var title = MutableLiveData<String?>()

    var ingredient = MutableLiveData<String?>()

    var people = MutableLiveData<String?>()

    var time = MutableLiveData<String?>()

    var preparation = MutableLiveData<String?>()

    var type = MutableLiveData<String?>()

    private val _photoUrl = MutableLiveData<Uri?>()
    val photoUrl
        get() = _photoUrl

    private val _uploadProgress = MutableLiveData<Int>()
    val uploadProgress
        get() = _uploadProgress

    init {
        _deletedPost.value = null
        uploadProgress.value = 0
        title.value = null
        type.value = null
        preparation.value = null
        ingredient.value = null
        people.value = null
        _photoUrl.value = null
        time.value = null
        _onUpload.value = null
        _uploadSuccess.value = null
    }

    fun updatePreviewPhoto(imageUrl: Uri?) {
        _photoUrl.value = imageUrl!!
        Log.d(TAG, "Photo URL: ${_photoUrl.value}")
    }

    fun addNewPost() {
        _onUpload.value = true
        val newPost = database.child("posts").push()
        val pushId = newPost.key
        viewModelScope.launch {
            pushId?.let {
                val uploadImage = async { uploadProfileImage(pushId) }
                val addNewPost = async { pushNewPost(pushId, uploadImage.await(), newPost, false) }

                addNewPost.await()
            }
        }
    }

    fun getPost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.child("posts").child(postId).get().addOnSuccessListener { snapshot ->
                val post = snapshot.getValue(Post::class.java)
                post?.let {
                    title.postValue(post.title)
                    type.postValue(post.type)
                    preparation.postValue(post.preparation)
                    ingredient.postValue(post.ingredient)
                    people.postValue(post.people.toString())
                    _photoUrl.postValue(post.photoUrl.toUri())
                    time.postValue(post.time.toString())
                }
            }
        }
    }

    fun deletePost(postId: String) {
        _onUpload.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val deletingPostRef = database.child("posts").child(postId)
            deletingPostRef.get().addOnSuccessListener {
                if (it.exists()) {
                    val post = it.getValue(Post::class.java)
                    val imageRef: StorageReference =
                        FirebaseStorage.getInstance().getReferenceFromUrl(post?.photoUrl!!)
                    imageRef.delete().addOnSuccessListener {
                        database.child("posts").child(postId).removeValue().addOnSuccessListener {
                            _deletedPost.value = true
                            _deletedPost.value = null
                            _onUpload.postValue(null)
                        }
                    }
                }
            }
        }
    }

    fun editPost(postId: String) {
        _onUpload.value = true
        val newPost = database.child("posts").child(postId)
        viewModelScope.launch(Dispatchers.IO) {
            val post = newPost.get().await().getValue(Post::class.java)
            if (post?.photoUrl == _photoUrl.value.toString()) {
                val addNewPost =
                    async { pushNewPost(postId, post.photoUrl, newPost, true) }
                addNewPost.await()
            } else {
                val uploadImage = async { uploadProfileImage(postId) }
                val addNewPost = async {
                    pushNewPost(
                        postId,
                        uploadImage.await(),
                        newPost,
                        true
                    )
                }
                addNewPost.await()
            }
        }
    }

    private suspend fun uploadProfileImage(pushId: String): String {
        return withContext(Dispatchers.IO) {
            _onUploadImage.postValue(true)
            storage.child("posts").child(pushId).putFile(_photoUrl.value!!)
                .addOnProgressListener { task ->
                    val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
                    _uploadProgress.value = progress.roundToInt()
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) _onUploadImage.value = false
                }
                .await()
                .storage
                .downloadUrl
                .await()
                .toString()
        }
    }

    private fun onUploadComplete() {
        _onUpload.value = null
        _uploadSuccess.value = null
        title.value = null
        type.value = null
        preparation.value = null
        ingredient.value = null
        people.value = null
        _photoUrl.value = null
        time.value = null
    }

    private fun pushNewPost(
        pushId: String,
        photoUri: String,
        newPost: DatabaseReference,
        updatePost: Boolean
    ) {
        if (time.value.isNullOrBlank()) time.value = "0"


        if (people.value.isNullOrBlank()) people.value = "0"

        val post = Post(
            pushId,
            title.value.toString(),
            ingredient.value.toString(),
            people.value!!.toInt(),
            time.value!!.toInt(),
            type.value.toString(),
            preparation.value.toString(),
            photoUri,
            0
        )

        newPost.setValue(post).addOnCompleteListener {
            if (it.isSuccessful) {
                _onUpload.value = null
                _uploadSuccess.value = true
            } else {
                _onUpload.value = null
                _uploadSuccess.value = false
            }
        }
        if (!updatePost) {
            viewModelScope.launch(Dispatchers.Main) {
                onUploadComplete()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class NewPostViewModelFactory :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewPostViewModel::class.java)) {
                return NewPostViewModel() as T
            }
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}