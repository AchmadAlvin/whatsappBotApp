package com.kelompoksatu.kafecraft.ui.home

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeWithId(
    val id: String,
    val recipe: Recipe
)

class HomeViewModel(
    private val bookmarkDao: BookmarkDao,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val recipesRef = FirebaseDatabase.getInstance().getReference("recipes")
    private val commentsRef = FirebaseDatabase.getInstance().getReference("comments")

    private val _recipes = MutableStateFlow<List<RecipeWithId>>(emptyList())
    val recipes: StateFlow<List<RecipeWithId>> = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchRecipes()
    }

    private fun fetchRecipes() {
        _isLoading.value = true
        recipesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipeList = mutableListOf<RecipeWithId>()
                for (child in snapshot.children) {
                    val recipe = child.getValue(Recipe::class.java)
                    val id = child.key
                    if (recipe != null && id != null) {
                        recipeList.add(RecipeWithId(id, recipe))
                    }
                }
                _recipes.value = recipeList.reversed() // Tampilkan yang terbaru di atas
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        })
    }

    fun fetchComments(recipeId: String) {
        commentsRef.child(recipeId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<Comment>()
                for (child in snapshot.children) {
                    val comment = child.getValue(Comment::class.java)
                    if (comment != null) {
                        commentList.add(comment)
                    }
                }
                _comments.value = commentList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addComment(recipeId: String, text: String) {
        val userName = sessionManager.getUserName() ?: "Unknown"
        val comment = Comment(
            authorName = userName,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        commentsRef.child(recipeId).push().setValue(comment)
    }

    fun toggleBookmark(recipeId: String, recipe: Recipe) {
        viewModelScope.launch {
            val isBookmarked = bookmarks.value.any { it.recipeId == recipeId }
            val entity = BookmarkEntity(
                recipeId = recipeId,
                title = recipe.title,
                description = recipe.description,
                imageUrl = recipe.imageUrl,
                authorName = recipe.authorName
            )
            if (isBookmarked) {
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(bookmarkDao, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
