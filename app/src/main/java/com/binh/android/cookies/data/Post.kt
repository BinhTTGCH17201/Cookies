package com.binh.android.cookies.data

data class Post(
    val postId: String,
    val title: String,
    val author: String,
    val ingredient: String,
    val people: Int,
    val time: Int,
    val type: String,
    val preparation: String,
    var photoUrl: String,
    val like: Int
) {
    constructor() : this("", "", "", "", 0, 0, "", "", "", 0)
}

