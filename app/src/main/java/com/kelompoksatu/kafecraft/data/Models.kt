package com.kelompoksatu.kafecraft.data

data class User(
    val name: String = "",
    val email: String = ""
)

data class Recipe(
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)

data class Comment(
    val authorName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
