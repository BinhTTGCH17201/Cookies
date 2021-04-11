package com.binh.android.cookies.data


data class Post(
    val postId: String = "",
    val title: String = "",
    val ingredient: String = "",
    val people: Int = 0,
    val time: Int = 0,
    val type: String = "",
    val preparation: String = "",
    var photoUrl: String = "",
    val like: Int = 0,
)

