package com.kelompoksatu.kafecraft.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val recipeId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val authorName: String
)
