package com.kelompoksatu.kafecraft.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompoksatu.kafecraft.data.Comment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val recipeWithId = viewModel.recipes.find { it.id == recipeId }
    val bookmarkList by viewModel.bookmarks.collectAsState()

    LaunchedEffect(recipeId) { viewModel.fetchComments(recipeId) }

    val isBookmarked = bookmarkList.any { it.recipeId == recipeId }
    val bookmarkedEntity = bookmarkList.find { it.recipeId == recipeId }

    val recipe = recipeWithId?.recipe ?: if (bookmarkedEntity != null) {
        Recipe(
            authorId = "",
            authorName = bookmarkedEntity.authorName,
            title = bookmarkedEntity.title,
            description = bookmarkedEntity.description,
            imageUrl = bookmarkedEntity.imageUrl,
            timestamp = 0L
        )
    } else null

    if (recipe == null) {
        Box(Modifier.fillMaxSize().background(Color(0xFFFEF9F6)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Resep tidak ditemukan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                if (isBookmarked && bookmarkedEntity != null) {
                    Button(
                        onClick = { viewModel.removeBookmark(bookmarkedEntity) },
                        modifier = Modifier.padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hapus dari Bookmark", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kembali", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Scaffold(
        bottomBar = {
            CommentInputBar(
                commentText = commentText,
                onCommentChange = { commentText = it },
                onSend = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(recipeId, commentText)
                        commentText = ""
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFFFF7A45)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(recipe.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier.padding(16.dp).size(40.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                            Text("by ${recipe.authorName}", fontSize = 14.sp, color = Color(0xFFFF7A45), modifier = Modifier.padding(top = 4.dp))
                        }
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFFF7A45) else Color.Black,
                            modifier = Modifier.size(32.dp).clickable { viewModel.toggleBookmark(recipeId, recipe) }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                    Spacer(Modifier.height(8.dp))
                    Text(recipe.description, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                    Spacer(Modifier.height(24.dp))
                    Text("Comments (${viewModel.comments.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                    Spacer(Modifier.height(16.dp))
                }
            }
            items(viewModel.comments) { CommentItem(it) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun CommentInputBar(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF7A45),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(50.dp).background(Color(0xFFFF7A45), CircleShape).clickable { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFF5E6E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.authorName.take(1).uppercase(), color = Color(0xFFFF7A45), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF332211))
                Spacer(Modifier.height(4.dp))
                Text(comment.text, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
