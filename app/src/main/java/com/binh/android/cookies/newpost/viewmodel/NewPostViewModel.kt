package com.binh.android.cookies.newpost.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.binh.android.cookies.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "NewPostViewModel"

class NewPostViewModel(application: Application) :
    AndroidViewModel(application) {
    private val database = FirebaseDatabase.getInstance().reference
    private val user = FirebaseAuth.getInstance().currentUser
    private val storage = FirebaseStorage.getInstance().reference

    private val _onUpload = MutableLiveData<Boolean?>()
    val onUpload: LiveData<Boolean?>
        get() = _onUpload

    private val _uploadSuccess = MutableLiveData<Boolean?>()
    val uploadSuccess: LiveData<Boolean?>
        get() = _uploadSuccess

    var title = MutableLiveData<String?>()

    var ingredient = MutableLiveData<String?>()

    var people = MutableLiveData<String>()

    var time = MutableLiveData<String?>()

    var preparation = MutableLiveData<String?>()

    var type = MutableLiveData<String?>()

    private val _photoUrl = MutableLiveData<Uri?>()
    val photoUrl
        get() = _photoUrl

    init {
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
            uploadProfileImage(pushId!!, newPost)
        }
    }

    private suspend fun uploadProfileImage(pushId: String, newPost: DatabaseReference) {
        withContext(Dispatchers.IO) {
            val imageRef = storage.child("/posts").child(pushId)
            val uploadTask = imageRef.putFile(_photoUrl.value!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.e(TAG, "File failed to upload!", it)
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    val post = Post(
                        pushId,
                        title.value.toString(),
                        user?.displayName.toString(),
                        ingredient.value.toString(),
                        people.value!!.toInt(),
                        time.value!!.toInt(),
                        type.value.toString(),
                        preparation.value.toString(),
                        downloadUri.toString(),
                        0
                    )
                    pushNewPost(post, newPost)

                } else {
                    _onUpload.value = null
                    _uploadSuccess.value = false
                }
            }
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

    private fun pushNewPost(post: Post, newPost: DatabaseReference) {
        newPost.setValue(post).addOnCompleteListener {
            if (it.isSuccessful) {
                _onUpload.value = null
                _uploadSuccess.value = true
            } else {
                _onUpload.value = null
                _uploadSuccess.value = false
            }
        }
        onUploadComplete()
    }
}