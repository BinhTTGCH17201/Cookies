package com.binh.android.cookies.data

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val like: Int? = 0
)
