package com.kelompoksatu.kafecraft.ui.myrecipes

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Stateful wrapper for CreateEditRecipeScreen.
 * Handles ViewModel interactions, state management, and side effects.
 */
@Composable
fun CreateEditRecipeScreen(
    recipeId: String,
    viewModel: MyRecipesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = recipeId.isNotEmpty()
    val existingRecipe = if (isEditMode) viewModel.myRecipes.find { it.id == recipeId } else null

    var title by remember { mutableStateOf(existingRecipe?.recipe?.title ?: "") }
    var description by remember { mutableStateOf(existingRecipe?.recipe?.description ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        selectedImageUri = it
    }

    LaunchedEffect(viewModel.saveMessage) {
        viewModel.saveMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetMessage()
            if (it.contains("berhasil")) onNavigateBack()
        }
    }

    CreateEditRecipeContent(
        isEditMode = isEditMode,
        title = title,
        onTitleChange = { title = it },
        description = description,
        onDescriptionChange = { description = it },
        selectedImageUri = selectedImageUri,
        existingImageUrl = existingRecipe?.recipe?.imageUrl,
        isSaving = viewModel.isSaving,
        onImageClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onDeleteClick = { showDeleteDialog = true },
        onSaveClick = {
            if (title.isBlank() || description.isBlank()) {
                Toast.makeText(context, "Harap isi nama dan deskripsi resep", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveRecipe(
                    selectedImageUri, title, description,
                    if (isEditMode) recipeId else null,
                    existingRecipe?.recipe?.imageUrl
                )
            }
        },
        onCancelClick = onNavigateBack
    )

    if (showDeleteDialog && existingRecipe != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Resep?", fontWeight = FontWeight.Bold) },
            text = { Text("Resep ini akan dihapus permanen dan tidak bisa dikembalikan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecipe(recipeId, existingRecipe.recipe.imageUrl)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Color(0xFF332211))
                }
            },
            containerColor = Color.White
        )
    }
}

/**
 * Stateless UI component for CreateEditRecipeScreen.
 * Renders the UI and triggers callbacks based on user actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditRecipeContent(
    isEditMode: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedImageUri: Uri?,
    existingImageUrl: String?,
    isSaving: Boolean,
    onImageClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFFF7A45),
        unfocusedBorderColor = Color(0xFFE0E0E0),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(24.dp).clickable { onCancelClick() })
            Text(
                if (isEditMode) "Edit Resep" else "Buat Resep",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211)
            )
            if (isEditMode) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp).clickable { onDeleteClick() }
                )
            } else {
                Spacer(Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEFEBE9))
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedImageUri != null -> {
                    AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                isEditMode && existingImageUrl?.isNotEmpty() == true -> {
                    AsyncImage(model = existingImageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF8D6E63))
                        Spacer(Modifier.height(8.dp))
                        Text("Ubah Foto Resep", color = Color(0xFF8D6E63))
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Nama Resep", fontWeight = FontWeight.Bold, color = Color(0xFF332211))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("Nasi Goreng Spesial") },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )
        Spacer(Modifier.height(16.dp))
        Text("Deskripsi", fontWeight = FontWeight.Bold, color = Color(0xFF332211))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = { Text("Ceritakan tentang resep ini...") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            colors = fieldColors
        )
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEBE9))
            ) {
                Text("Batal", color = Color(0xFF332211), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45)),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateEditRecipeContentPreview() {
    MaterialTheme {
        CreateEditRecipeContent(
            isEditMode = false,
            title = "Nasi Goreng",
            onTitleChange = {},
            description = "Nasi goreng ayam",
            onDescriptionChange = {},
            selectedImageUri = null,
            existingImageUrl = null,
            isSaving = false,
            onImageClick = {},
            onDeleteClick = {},
            onSaveClick = {},
            onCancelClick = {}
        )
    }
}
