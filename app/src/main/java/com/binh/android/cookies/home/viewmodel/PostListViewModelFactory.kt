package com.binh.android.cookies.home.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase

class PostListViewModelFactory constructor(
    private val database: FirebaseDatabase,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostListViewModel::class.java)) {
            return PostListViewModel(database, application) as T
        }
        throw IllegalArgumentException("ViewModel Not Found")
    }
}