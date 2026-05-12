package com.kelompoksatu.kafecraft.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kelompoksatu.kafecraft.data.BookmarkDao
import com.kelompoksatu.kafecraft.data.BookmarkEntity
import com.kelompoksatu.kafecraft.data.Comment
import com.kelompoksatu.kafecraft.data.Recipe
import com.kelompoksatu.kafecraft.data.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeWithId(val id: String, val recipe: Recipe)

class HomeViewModel(
    private val bookmarkDao: BookmarkDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val recipesRef = FirebaseDatabase.getInstance().getReference("recipes")
    private val commentsRef = FirebaseDatabase.getInstance().getReference("comments")

    var recipes by mutableStateOf<List<RecipeWithId>>(emptyList())
    var isLoading by mutableStateOf(true)
    var comments by mutableStateOf<List<Comment>>(emptyList())

    val bookmarks = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { fetchRecipes() }

    private fun fetchRecipes() {
        isLoading = true
        recipesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recipes = snapshot.children.mapNotNull { child ->
                    val recipe = child.getValue(Recipe::class.java)
                    val key = child.key
                    if (recipe != null && key != null) RecipeWithId(key, recipe) else null
                }.reversed()
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    fun fetchComments(recipeId: String) {
        commentsRef.child(recipeId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments = snapshot.children.mapNotNull { it.getValue(Comment::class.java) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addComment(recipeId: String, text: String) {
        val comment = Comment(sessionManager.getUserName() ?: "Unknown", text, System.currentTimeMillis())
        commentsRef.child(recipeId).push().setValue(comment)
    }

    fun toggleBookmark(recipeId: String, recipe: Recipe) {
        viewModelScope.launch {
            val entity = BookmarkEntity(recipeId, recipe.title, recipe.description, recipe.imageUrl, recipe.authorName)
            val isAlreadyBookmarked = bookmarks.value.any { it.recipeId == recipeId }
            if (isAlreadyBookmarked) {
                bookmarkDao.delete(entity)
            } else {
                bookmarkDao.insert(entity)
            }
        }
    }

    class Factory(
        private val bookmarkDao: BookmarkDao,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = HomeViewModel(bookmarkDao, sessionManager) as T
    }
}
