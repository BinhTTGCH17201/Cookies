package com.binh.android.cookies.home.searchable.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PostSearchViewModelFactory constructor(
    private val application: Application
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostSearchViewModel::class.java)) {
            return PostSearchViewModel(application) as T
        }
        throw IllegalArgumentException("ViewModel Not Found")
    }
}