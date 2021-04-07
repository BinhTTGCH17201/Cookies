package com.binh.android.cookies.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.FirebaseDatabase

private const val TAG = "PostListViewModel"

class PostListViewModel(val database: FirebaseDatabase, application: Application) :
    AndroidViewModel(application) {

    val dataRef = database.reference

//    init {
//        viewModelScope.launch {
//            val postChildrenListener = object : ChildEventListener {
//                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                    val post = snapshot.getValue(Post::class.java)
//                    Log.i(TAG, post.toString())
//                }
//
//                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onChildRemoved(snapshot: DataSnapshot) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            }
//            withContext(Dispatchers.IO) {
//                database.reference.child("posts").addChildEventListener(postChildrenListener)
//            }
//        }
//    }


}