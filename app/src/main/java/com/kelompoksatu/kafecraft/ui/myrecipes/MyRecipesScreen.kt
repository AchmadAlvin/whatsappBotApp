package com.kelompoksatu.kafecraft.ui.myrecipes

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompoksatu.kafecraft.data.Recipe
import com.kelompoksatu.kafecraft.ui.home.RecipeWithId

/**
 * Stateful wrapper for MyRecipesScreen.
 * Handles the ViewModel interactions and Dialog state.
 */
@Composable
fun MyRecipesScreen(
    viewModel: MyRecipesViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<RecipeWithId?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchMyRecipes()
    }

    LaunchedEffect(viewModel.saveMessage) {
        viewModel.saveMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetMessage()
        }
    }

    MyRecipesContent(
        recipes = viewModel.myRecipes,
        isLoading = viewModel.isLoading,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToEdit = onNavigateToEdit,
        onDeleteClick = { recipe -> 
            recipeToDelete = recipe
            showDeleteDialog = true 
        }
    )

    if (showDeleteDialog && recipeToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone. Are you sure you want to delete this recipe?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecipe(recipeToDelete!!.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color(0xFF332211))
                }
            },
            containerColor = Color.White
        )
    }
}

/**
 * Stateless UI component for MyRecipesScreen.
 * Only handles rendering the UI and passing up events.
 */
@Composable
fun MyRecipesContent(
    recipes: List<RecipeWithId>,
    isLoading: Boolean,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDeleteClick: (RecipeWithId) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Color(0xFFFF7A45),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                "My Recipes",
                fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                "${recipes.size} recipes in your collection",
                fontSize = 14.sp, color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
            
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF7A45))
                    }
                }
                recipes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Anda belum membuat resep.", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recipes) { recipe ->
                            MyRecipeCard(
                                recipeWithId = recipe,
                                onEdit = { onNavigateToEdit(recipe.id) },
                                onDelete = { onDeleteClick(recipe) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyRecipeCard(recipeWithId: RecipeWithId, onEdit: () -> Unit, onDelete: () -> Unit) {
    val recipe = recipeWithId.recipe
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(recipe.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                Spacer(Modifier.height(8.dp))
                Text(recipe.description, fontSize = 14.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color(0xFF332211), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit", color = Color(0xFF332211))
                    }
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete", color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyRecipesContentPreview() {
    val dummyRecipes = listOf(
        RecipeWithId("1", Recipe("authorId", "Author Name", "Nasi Goreng", "Nasi goreng spesial pakai telur", "", 0L)),
        RecipeWithId("2", Recipe("authorId", "Author Name", "Ayam Bakar", "Ayam bakar madu manis", "", 0L))
    )
    MaterialTheme {
        MyRecipesContent(
            recipes = dummyRecipes,
            isLoading = false,
            onNavigateToCreate = {},
            onNavigateToEdit = {},
            onDeleteClick = {}
        )
    }
}
