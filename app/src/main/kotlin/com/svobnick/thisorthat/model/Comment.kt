package com.svobnick.thisorthat.model

data class Comment(
    val name: String,
    val commentId: Long,
    val userId: Long,
    val parentId: Long,
    val text: String,
    val avatarUrl: String
)