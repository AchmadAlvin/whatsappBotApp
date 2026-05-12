package com.kelompoksatu.kafecraft.ui.myrecipes

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import java.util.UUID

class MyRecipesViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val recipesRef = FirebaseDatabase.getInstance().getReference("recipes")
    private val storageRef = FirebaseStorage.getInstance().reference
    private var recipesListener: ValueEventListener? = null
    private var lastQueryUid: String? = null

    var myRecipes by mutableStateOf<List<RecipeWithId>>(emptyList())
    var isLoading by mutableStateOf(true)
    var isSaving by mutableStateOf(false)
    var saveMessage by mutableStateOf<String?>(null)

    init { fetchMyRecipes() }

    fun fetchMyRecipes() {
        val uid = sessionManager.getUserId()
        if (uid == null) {
            isLoading = false
            return
        }
        
        if (uid == lastQueryUid && recipesListener != null) return
        
        recipesListener?.let { recipesRef.removeEventListener(it) }
        lastQueryUid = uid

        isLoading = true
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myRecipes = snapshot.children.mapNotNull { child ->
                    val recipe = child.getValue(Recipe::class.java)
                    val key = child.key
                    if (recipe != null && key != null) RecipeWithId(key, recipe) else null
                }.reversed()
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        }
        recipesListener = listener
        recipesRef.orderByChild("authorId").equalTo(uid).addValueEventListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        recipesListener?.let { recipesRef.removeEventListener(it) }
    }

    fun saveRecipe(
        uri: Uri?,
        title: String,
        description: String,
        existingRecipeId: String? = null,
        existingImageUrl: String? = null
    ) {
        val uid = sessionManager.getUserId()
        val userName = sessionManager.getUserName()
        if (uid == null || userName == null) {
            saveMessage = "User tidak ditemukan, harap login ulang."
            return
        }
        isSaving = true
        if (uri != null) {
            val imageRef = storageRef.child("recipe_images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl
                        .addOnSuccessListener { url ->
                            saveToDb(uid, userName, title, description, url.toString(), existingRecipeId)
                            if (!existingImageUrl.isNullOrEmpty() && existingRecipeId != null) {
                                deleteImage(existingImageUrl)
                            }
                        }
                        .addOnFailureListener {
                            isSaving = false
                            saveMessage = "Gagal mendapatkan URL gambar."
                        }
                }
                .addOnFailureListener {
                    isSaving = false
                    saveMessage = "Gagal mengunggah gambar: ${it.message}"
                }
        } else {
            saveToDb(uid, userName, title, description, existingImageUrl ?: "", existingRecipeId)
        }
    }

    private fun saveToDb(
        authorId: String,
        authorName: String,
        title: String,
        description: String,
        imageUrl: String,
        existingId: String?
    ) {
        val recipe = Recipe(authorId, authorName, title, description, imageUrl, System.currentTimeMillis())
        val ref = if (existingId != null) recipesRef.child(existingId) else recipesRef.push()
        ref.setValue(recipe)
            .addOnSuccessListener {
                isSaving = false
                saveMessage = "Resep berhasil disimpan!"
            }
            .addOnFailureListener {
                isSaving = false
                saveMessage = "Gagal menyimpan resep: ${it.message}"
            }
    }

    fun deleteRecipe(recipeId: String, imageUrl: String) {
        isSaving = true
        recipesRef.child(recipeId).removeValue()
            .addOnSuccessListener {
                isSaving = false
                saveMessage = "Resep berhasil dihapus"
                if (imageUrl.isNotEmpty()) deleteImage(imageUrl)
            }
            .addOnFailureListener {
                isSaving = false
                saveMessage = "Gagal menghapus resep"
            }
    }

    private fun deleteImage(url: String) {
        try {
            FirebaseStorage.getInstance().getReferenceFromUrl(url).delete()
        } catch (_: Exception) {}
    }

    fun resetMessage() { saveMessage = null }

    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = MyRecipesViewModel(sessionManager) as T
    }
}
