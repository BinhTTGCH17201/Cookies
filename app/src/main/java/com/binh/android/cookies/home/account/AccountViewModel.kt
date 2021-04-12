package com.binh.android.cookies.home.account

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.binh.android.cookies.R
import com.binh.android.cookies.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "AccountViewModel"

class AccountViewModel(application: Application) :
    AndroidViewModel(application) {

    private var user = FirebaseAuth.getInstance().currentUser
    private val userDb = FirebaseDatabase.getInstance().reference.child("users")
    private val userStorage =
        FirebaseStorage.getInstance().reference.child("/userProfileImage")

    private val _uiVisible = MutableLiveData<Boolean>()
    val uiVisible: LiveData<Boolean>
        get() = _uiVisible

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean>
        get() = _isLoggedIn

    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String>
        get() = _buttonText

    private val _isProfileChanged = MutableLiveData<Boolean?>()
    val isProfileChanged: LiveData<Boolean?>
        get() = _isProfileChanged

    val name = MutableLiveData<String?>()

    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?>
        get() = _email

    var password = MutableLiveData<String?>()

    var rePassword = MutableLiveData<String?>()

    private val _photoUrl = MutableLiveData<String?>()
    val photoUrl: LiveData<String?>
        get() = _photoUrl

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        viewModelScope.launch(Dispatchers.IO) {
            user?.let {
                val userDb =
                    FirebaseDatabase.getInstance().reference.child("users/${user.uid}/admin").get()
                        .await().getValue(Boolean::class.java)
                userDb?.let {
                    _isLoggedIn.postValue(it)
                }
            } ?: kotlin.run { _isLoggedIn.postValue(false) }
        }
    }

    init {
        _uiVisible.value = true
        name.value = ""
        _email.value = ""
        password.value = ""
        rePassword.value = ""
        _uiVisible.value = false
        _buttonText.value = ""
        _isProfileChanged.value = null
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    private fun onBindingUserSignedInProfile() {
        user?.let {
            name.value = it.displayName!!
            _email.value = it.email!!
            _photoUrl.value = it.photoUrl?.toString()
        }
    }

    private fun onBindingUserSignedOutProfile() {
        user?.let {
            name.value = ""
            _email.value = ""
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

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun onLoggedIn() {
        viewModelScope.launch {
            user?.reload()
            user = FirebaseAuth.getInstance().currentUser
            _uiVisible.value = true
            _buttonText.value =
                getApplication<Application>().resources.getString(R.string.logout_button_text)
            user?.let {
                val userAdmin = userDb.child(user.uid).get().await().exists()
                if (userAdmin) {
                    val admin = userDb.child("${user.uid}/admin").get().await()
                        .getValue(Boolean::class.java) ?: false
                    saveUserInfoToDb(admin)
                }
            } ?: run {
                Log.e(TAG, "Error to get user data!")
            }
            onBindingUserSignedInProfile()
        }
    }

    private suspend fun saveUserInfoToDb(isAdmin: Boolean) {
        withContext(Dispatchers.IO) {
            val newUserInfo = User(user?.displayName!!, user?.photoUrl.toString(), isAdmin)
            user?.uid?.let { userDb.child(it).setValue(newUserInfo) }
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun updateUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(
                TAG,
                "Name: ${name.value}, email: ${email.value}, password: ${password.value}, rePassword: ${rePassword.value}"
            )
            user?.let {
                if (password.value != "" && rePassword.value != "") {
                    it.updatePassword(password.value.toString()).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isProfileChanged.value = true
                        }
                    }
                }
                if (name.value != "" && name.value != user?.displayName) {
                    val newUserProfile = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.value)
                        .build()
                    it.updateProfile(newUserProfile).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _isProfileChanged.value = true
                        }
                    }
                }

                if (user?.photoUrl.toString() != photoUrl.value) uploadProfileImage()

                user?.reload()
                user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val userAdmin = userDb.child(user.uid).get().await().exists()
                    if (userAdmin) {
                        val admin = userDb.child("${user.uid}/admin").get().await()
                            .getValue(Boolean::class.java) ?: false
                        saveUserInfoToDb(admin)
                    }
                } ?: run {
                    Log.e(TAG, "Error to get user data!")
                }
            }

        }
    }

    fun onProfileChangedComplete() {
        _isProfileChanged.value = null
    }

    fun updateProfileImagePreview(photoUrl: Uri?) {
        _photoUrl.value = photoUrl.toString()
    }

    private suspend fun uploadProfileImage() {
        withContext(Dispatchers.IO) {
            val imageRef = userStorage.child(user?.uid.toString())
            val uploadTask = _photoUrl.value?.let { imageRef.putFile(it.toUri()) }

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

                    _photoUrl.postValue(downloadUri.toString())

                    _isProfileChanged.postValue(true)
                } else {
                    Log.e(TAG, "Update user task failed!")
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class AccountViewModelFactory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                return AccountViewModel(application) as T
            }
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
