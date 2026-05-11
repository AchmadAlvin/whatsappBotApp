package com.kelompoksatu.kafecraft.ui.myrecipes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.kelompoksatu.kafecraft.data.Recipe
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.ui.home.RecipeWithId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    data class Success(val message: String) : SaveState()
    data class Error(val error: String) : SaveState()
}

class MyRecipesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val recipesRef = FirebaseDatabase.getInstance().getReference("recipes")
    private val storageRef = FirebaseStorage.getInstance().reference

    private val _myRecipes = MutableStateFlow<List<RecipeWithId>>(emptyList())
    val myRecipes: StateFlow<List<RecipeWithId>> = _myRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        fetchMyRecipes()
    }

    private fun fetchMyRecipes() {
        val currentUserId = sessionManager.getUserId()
        if (currentUserId.isNullOrEmpty()) {
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        // Fetch and filter locally, or use query orderByChild("authorId").equalTo(currentUserId)
        recipesRef.orderByChild("authorId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recipeList = mutableListOf<RecipeWithId>()
                    for (child in snapshot.children) {
                        val recipe = child.getValue(Recipe::class.java)
                        val id = child.key
                        if (recipe != null && id != null) {
                            recipeList.add(RecipeWithId(id, recipe))
                        }
                    }
                    _myRecipes.value = recipeList.reversed()
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                }
            })
    }

    fun saveRecipe(
        uri: Uri?,
        title: String,
        description: String,
        existingRecipeId: String? = null,
        existingImageUrl: String? = null
    ) {
        val currentUserId = sessionManager.getUserId()
        val currentUserName = sessionManager.getUserName()
        if (currentUserId.isNullOrEmpty() || currentUserName.isNullOrEmpty()) {
            _saveState.value = SaveState.Error("User tidak ditemukan, harap login ulang.")
            return
        }

        _saveState.value = SaveState.Loading

        if (uri != null) {
            // Upload foto baru
            val imageRef = storageRef.child("recipe_images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveToDatabase(
                            currentUserId,
                            currentUserName,
                            title,
                            description,
                            downloadUrl.toString(),
                            existingRecipeId
                        )
                        // Jika ada gambar lama dan ID bukan null, hapus gambar lama dari storage
                        if (existingImageUrl != null && existingImageUrl.isNotEmpty() && existingRecipeId != null) {
                            deleteImageFromStorage(existingImageUrl)
                        }
                    }.addOnFailureListener {
                        _saveState.value = SaveState.Error("Gagal mendapatkan URL gambar.")
                    }
                }
                .addOnFailureListener {
                    _saveState.value = SaveState.Error("Gagal mengunggah gambar: ${it.message}")
                }
        } else {
            // Tidak ada foto baru
            saveToDatabase(
                currentUserId,
                currentUserName,
                title,
                description,
                existingImageUrl ?: "",
                existingRecipeId
            )
        }
    }

    private fun saveToDatabase(
        authorId: String,
        authorName: String,
        title: String,
        description: String,
        imageUrl: String,
        existingRecipeId: String?
    ) {
        val recipe = Recipe(
            authorId = authorId,
            authorName = authorName,
            title = title,
            description = description,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        val targetRef = if (existingRecipeId != null) {
            recipesRef.child(existingRecipeId)
        } else {
            recipesRef.push()
        }

        targetRef.setValue(recipe)
            .addOnSuccessListener {
                _saveState.value = SaveState.Success("Resep berhasil disimpan!")
            }
            .addOnFailureListener {
                _saveState.value = SaveState.Error("Gagal menyimpan resep: ${it.message}")
            }
    }

    fun deleteRecipe(recipeId: String, imageUrl: String) {
        _saveState.value = SaveState.Loading
        recipesRef.child(recipeId).removeValue()
            .addOnSuccessListener {
                if (imageUrl.isNotEmpty()) {
                    deleteImageFromStorage(imageUrl)
                }
                _saveState.value = SaveState.Success("Resep berhasil dihapus")
            }
            .addOnFailureListener {
                _saveState.value = SaveState.Error("Gagal menghapus resep")
            }
    }

    private fun deleteImageFromStorage(imageUrl: String) {
        try {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            ref.delete()
        } catch (e: Exception) {
            // Ignored, might not be a valid storage URL or already deleted
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }

    class Factory(
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyRecipesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyRecipesViewModel(sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
