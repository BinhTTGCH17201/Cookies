package com.binh.android.cookies.newpost.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NewPostViewModelFactory constructor(
    private val application: Application
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewPostViewModel::class.java)) {
            return NewPostViewModel(application) as T
        }
        throw IllegalArgumentException("ViewModel Not Found")
    }
}