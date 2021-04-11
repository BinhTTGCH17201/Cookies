package com.binh.android.cookies.account

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.binh.android.cookies.R
import com.binh.android.cookies.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AccountViewModel"

class AccountViewModel(application: Application) :
    AndroidViewModel(application) {

    private var user = FirebaseAuth.getInstance().currentUser
    private val userStorage =
        FirebaseStorage.getInstance().reference.child("/userProfileImage")

    private val _uiVisible = MutableLiveData<Boolean>()
    val uiVisible: LiveData<Boolean>
        get() = _uiVisible

    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String>
        get() = _buttonText

    private val _isProfileChanged = MutableLiveData<Boolean?>()
    val isProfileChanged: LiveData<Boolean?>
        get() = _isProfileChanged

    val name = MutableLiveData<String?>()

    var email = MutableLiveData<String?>()

    var password = MutableLiveData<String?>()

    var rePassword = MutableLiveData<String?>()

    private val _photoUrl = MutableLiveData<Uri?>()
    val photoUrl: LiveData<Uri?>
        get() = _photoUrl

    init {
        _uiVisible.value = true
        user?.let {
            name.value = ""
            email.value = ""
            password.value = ""
            rePassword.value = ""
            _uiVisible.value = false
            _buttonText.value = "Sign In"
            _photoUrl.value = user?.photoUrl
            _isProfileChanged.value = null
        }
    }

    private fun onBindingUserSignedInProfile() {
        user?.let {
            name.value = it.displayName!!
            email.value = it.email!!
            _photoUrl.value = it.photoUrl
        }
    }

    private fun onBindingUserSignedOutProfile() {
        user?.let {
            Log.d("User", "User login with UID: ${user?.uid}")
            name.value = ""
            email.value = ""
            _photoUrl.value = null
        }
    }

    fun onLoggedOut() {
        viewModelScope.launch {
            _uiVisible.value = false
            _buttonText.value =
                getApplication<Application>().resources.getString(R.string.login_button_text)
            onBindingUserSignedOutProfile()
        }
    }

    fun onLoggedIn() {
        viewModelScope.launch {

            user?.reload()
            user = FirebaseAuth.getInstance().currentUser
            _uiVisible.value = true
            _buttonText.value =
                getApplication<Application>().resources.getString(R.string.logout_button_text)
            val listenerForSingleValueEvent = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userAuth = snapshot.getValue(User::class.java)
                        Log.d("AccountViewModel", "Admin: " + userAuth?.admin)
                        if (userAuth?.admin!! == false) {
                            val userDb: User = if (user?.photoUrl != null) {
                                User(
                                    user?.displayName!!,
                                    user?.photoUrl.toString(),
                                    false
                                )
                            } else {
                                User(
                                    user?.displayName!!,
                                    "https://firebasestorage.googleapis.com/v0/b/cooking-forum.appspot.com/o/userProfileImage%2Fuser_profile_placeholder.png?alt=media&token=bc97706a-2a5c-4365-a9e7-d48ed8602b45",
                                    false
                                )
                            }
                            FirebaseDatabase.getInstance().reference.child("users/${user?.uid}")
                                .setValue(userDb)
                        } else if (userAuth.admin == true) {
                            val userDb: User = if (user?.photoUrl != null) {
                                User(
                                    user?.displayName!!,
                                    user?.photoUrl.toString(),
                                    true
                                )
                            } else {
                                User(
                                    user?.displayName!!,
                                    "https://firebasestorage.googleapis.com/v0/b/cooking-forum.appspot.com/o/userProfileImage%2Fuser_profile_placeholder.png?alt=media&token=bc97706a-2a5c-4365-a9e7-d48ed8602b45",
                                    true
                                )
                            }
                            FirebaseDatabase.getInstance().reference.child("users/${user?.uid}")
                                .setValue(userDb)
                        }
                    }
                    onBindingUserSignedInProfile()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AccountViewModel", "Fail to save user info!!!")
                }
            }
            FirebaseDatabase.getInstance().reference.child("users/${user?.uid}")
                .addListenerForSingleValueEvent(listenerForSingleValueEvent)
        }
    }

    fun updateUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            user?.let {
                if (password.value != "" && rePassword.value != "") if (password.value == rePassword.value) {
                    it.updatePassword(password.value.toString()).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isProfileChanged.value = true
                        }
                    }
                    val newUserProfile = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.value)
                        .build()
                    it.updateProfile(newUserProfile).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isProfileChanged.value = true
                        }
                    }
                    if (user?.photoUrl != photoUrl.value) uploadProfileImage()

                    it.updateEmail(email.value.toString()).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isProfileChanged.value = true
                        }
                    }
                }
            }
        }
    }

    fun onProfileChangedComplete() {
        _isProfileChanged.value = null
    }

    fun updateProfileImagePreview(photoUrl: Uri?) {
        _photoUrl.value = photoUrl
    }

    private suspend fun uploadProfileImage() {
        withContext(Dispatchers.IO) {
            val imageRef = userStorage.child(user?.uid.toString())
            val uploadTask = _photoUrl.value?.let { imageRef.putFile(it) }

            uploadTask?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.e(TAG, "File failed to upload!", it)
                    }
                }
                imageRef.downloadUrl
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    val userProfileChange = UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build()

                    user?.updateProfile(userProfileChange)
                } else {
                    Log.e(TAG, "Update user task failed!")
                }
            }
        }
    }
}
