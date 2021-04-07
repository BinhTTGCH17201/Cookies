package com.binh.android.cookies.home.detail.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PostDetailsViewModelFactory constructor(
    private val postId: String,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostDetailsViewModel::class.java)) {
            return PostDetailsViewModel(postId, application) as T
        }
        throw IllegalArgumentException("ViewModel Not Found")
    }
}