package com.binh.android.cookies.data

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FirebaseApp : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseDatabase.getInstance().reference.keepSynced(true)
    }
}