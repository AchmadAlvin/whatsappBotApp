package com.kelompoksatu.kafecraft.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
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
import coil.compose.AsyncImage
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.ui.home.HomeViewModel
import com.kelompoksatu.kafecraft.ui.myrecipes.MyRecipesViewModel

@Composable
fun ProfileScreen(
    sessionManager: SessionManager,
    homeViewModel: HomeViewModel,
    myRecipesViewModel: MyRecipesViewModel,
    onLogout: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val bookmarks by homeViewModel.bookmarks.collectAsState()
    val userName = sessionManager.getUserName() ?: "User"
    val userHandle = "@${userName.lowercase().replace(" ", "")}"

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFEF9F6))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onLogout() }) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = "Logout", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Keluar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        HorizontalDivider(color = Color(0xFFF5E6E0))
        Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(model = "https://i.pravatar.cc/300?u=$userHandle", contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop, modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFE0E0E0)))
            Spacer(Modifier.height(16.dp))
            Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
            Spacer(Modifier.height(4.dp))
            Text(userHandle, fontSize = 14.sp, color = Color(0xFFFF7A45))
            Spacer(Modifier.height(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(myRecipesViewModel.myRecipes.size.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                Text("Posts", fontSize = 14.sp, color = Color.Gray)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            HorizontalDivider(color = Color(0xFFF5E6E0))
            Box(modifier = Modifier.background(Color(0xFFFEF9F6)).padding(horizontal = 16.dp)) {
                Text("My Bookmark", fontSize = 14.sp, color = Color(0xFFFF7A45), fontWeight = FontWeight.Medium)
            }
        }
        HorizontalDivider(color = Color(0xFFFF7A45), modifier = Modifier.padding(bottom = 16.dp))
        if (bookmarks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Text("Belum ada resep yang dibookmark.", color = Color.Gray, modifier = Modifier.padding(top = 32.dp))
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(bookmarks) { bookmark ->
                    AsyncImage(model = bookmark.imageUrl.ifEmpty { "https://picsum.photos/200/200" }, contentDescription = bookmark.title,
                        contentScale = ContentScale.Crop, modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable { onNavigateToDetail(bookmark.recipeId) })
                }
            }
        }
    }
}
