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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val bookmarks by homeViewModel.bookmarks.collectAsStateWithLifecycle()
    val myRecipes by myRecipesViewModel.myRecipes.collectAsStateWithLifecycle()
    
    val userName = sessionManager.getUserName() ?: "User"
    val userHandle = "@${userName.lowercase().replace(" ", "")}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF9F6))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF332211)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLogout() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Keluar",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        HorizontalDivider(color = Color(0xFFF5E6E0))
        
        // Profile Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                // Dummy profile image
                AsyncImage(
                    model = "https://i.pravatar.cc/300?u=$userHandle",
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF332211)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = userHandle,
                fontSize = 14.sp,
                color = Color(0xFFFF7A45)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = myRecipes.size.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF332211)
                )
                Text(
                    text = "Posts",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Bookmark Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(color = Color(0xFFF5E6E0))
            Box(
                modifier = Modifier
                    .background(Color(0xFFFEF9F6))
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "My Bookmark",
                    fontSize = 14.sp,
                    color = Color(0xFFFF7A45),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        HorizontalDivider(color = Color(0xFFFF7A45), modifier = Modifier.padding(bottom = 16.dp))
        
        // Bookmark Grid
        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Belum ada resep yang dibookmark.",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bookmarks) { bookmark ->
                    AsyncImage(
                        model = bookmark.imageUrl.ifEmpty { "https://picsum.photos/200/200" },
                        contentDescription = bookmark.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToDetail(bookmark.recipeId) }
                    )
                }
            }
        }
    }
}
