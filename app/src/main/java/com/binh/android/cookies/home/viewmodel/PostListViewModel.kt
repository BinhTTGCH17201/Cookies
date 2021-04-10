package com.binh.android.cookies.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.FirebaseDatabase

class PostListViewModel(val database: FirebaseDatabase, application: Application) :
    AndroidViewModel(application)