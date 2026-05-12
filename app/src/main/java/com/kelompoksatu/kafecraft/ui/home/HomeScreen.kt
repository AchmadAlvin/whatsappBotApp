package com.kelompoksatu.kafecraft.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigateToDetail: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Recipe Feed", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        Text("Discover delicious recipes from our community", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))
        when {
            viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF7A45)) }
            viewModel.recipes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada resep. Jadilah yang pertama!", color = Color.Gray) }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(viewModel.recipes) { RecipeCard(it) { onNavigateToDetail(it.id) } }
            }
        }
    }
}

@Composable
fun RecipeCard(recipeWithId: RecipeWithId, onClick: () -> Unit) {
    val r = recipeWithId.recipe
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            if (r.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = r.imageUrl,
                    contentDescription = r.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(r.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                Text("by ${r.authorName}", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text(r.description, fontSize = 14.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text("Comments", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
