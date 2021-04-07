package com.binh.android.cookies.account

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AccountViewModel"

class AccountViewModel(application: Application) :
    AndroidViewModel(application) {

    private val user = FirebaseAuth.getInstance().currentUser
    private val userStorage =
        FirebaseStorage.getInstance().reference.child("/userProfileImage")

    private val _uiVisible = MutableLiveData<Boolean>()
    val uiVisible: LiveData<Boolean>
        get() = _uiVisible

    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String>
        get() = _buttonText

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean>
        get() = _isLoggedIn

    private val _isProfileChanged = MutableLiveData<Boolean>()
    val isProfileChanged: LiveData<Boolean>
        get() = _isProfileChanged

    //    var name = ""
    val name = MutableLiveData<String?>()

    var email = MutableLiveData<String?>()

    var password = MutableLiveData<String?>()

    var rePassword = MutableLiveData<String?>()

    private val _photoUrl = MutableLiveData<Uri?>()
    val photoUrl: LiveData<Uri?>
        get() = _photoUrl

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState>
        get() = _authenticationState

    init {
        _uiVisible.value = true
        user?.let {
            name.value = ""
            email.value = ""
            password.value = ""
            rePassword.value = ""
            _uiVisible.value = false
            _buttonText.value = "Sign In"
            _isLoggedIn.value = false
            _photoUrl.value = user.photoUrl
            _isProfileChanged.value = null
        }

        // Add authentication listener
        val authenticationStateListener = FirebaseAuth.AuthStateListener { user ->
            if (user.currentUser != null) {
                _authenticationState.value = AuthenticationState.AUTHENTICATED
            } else {
                _authenticationState.value = AuthenticationState.UNAUTHENTICATED
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authenticationStateListener)
    }

    fun userLoggedIn() {
        _isLoggedIn.value = true
    }

    fun userLoggedOut() {
        _isLoggedIn.value = false
    }

    private fun onBindingUserSignedInProfile() {
        user?.let {
            name.value = it.displayName!!
            email.value = it.email!!
            _photoUrl.value = it.photoUrl
            Log.d(TAG, "Successfully change var for binding")
        }
    }

    private fun onBindingUserSignedOutProfile() {
        user?.let {
            name.value = ""
            email.value = ""
            _photoUrl.value = null
            Log.d(TAG, "Successfully change var for binding")
        }
    }

    fun onLoggedOut() {
        viewModelScope.launch {
            _uiVisible.value = false
            _buttonText.value = "Sign In"
            onBindingUserSignedOutProfile()
        }
    }

    fun onLoggedIn() {
        viewModelScope.launch {
            user?.reload()
            _uiVisible.value = true
            _buttonText.value = "Sign Out"
            onBindingUserSignedInProfile()
        }
    }

    fun updateUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            user?.let {
                if (password.value != "" && rePassword.value != "") if (password.value == rePassword.value) {
                    it.updatePassword(password.value.toString()).addOnCompleteListener {
                        if (it.isSuccessful) {
                            _isProfileChanged.value = true
                        }
                    }
                    val newUserProfile = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.value)
                        .build()
                    it.updateProfile(newUserProfile).addOnCompleteListener {
                        if (it.isSuccessful) {
                            _isProfileChanged.value = true
                        }
                    }
                    if (user.photoUrl != photoUrl.value) uploadProfileImage()

                    it.updateEmail(email.value.toString()).addOnCompleteListener {
                        if (it.isSuccessful) {
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
