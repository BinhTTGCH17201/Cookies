package com.binh.android.cookies.viewmodel

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivityViewModel : ViewModel() {

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean>
        get() = _isAdmin

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        viewModelScope.launch(Dispatchers.IO) {
            user?.let {
                val userDb =
                    FirebaseDatabase.getInstance().reference.child("users/${user.uid}/admin").get()
                        .await().getValue(Boolean::class.java)
                userDb?.let {
                    _isAdmin.postValue(it)
                }
            } ?: kotlin.run { _isAdmin.postValue(false) }
        }
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }

    @Suppress("UNCHECKED_CAST")
    class MainActivityViewModelFactory :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel() as T
            }
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}