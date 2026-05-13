package com.kelompoksatu.kafecraft.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kelompoksatu.kafecraft.data.BookmarkEntity
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.ui.home.HomeViewModel
import com.kelompoksatu.kafecraft.ui.myrecipes.MyRecipesViewModel

/**
 * Stateful wrapper for the Profile Screen.
 * Handles ViewModels, Firebase interactions, and SessionManager.
 */
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
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    // Fetch user profile photo URL
    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("photoUrl").get()
                .addOnSuccessListener { snap ->
                    photoUrl = snap.getValue(String::class.java)
                }
        }
    }

    // Handle photo picking and uploading
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && uid != null) {
            isUploadingPhoto = true
            val ref = FirebaseStorage.getInstance().reference.child("profile_images/${uid}.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        photoUrl = url.toString()
                        FirebaseDatabase.getInstance().getReference("users").child(uid).child("photoUrl").setValue(url.toString())
                        isUploadingPhoto = false
                    }
                }
                .addOnFailureListener { isUploadingPhoto = false }
        }
    }

    ProfileContent(
        userName = userName,
        userHandle = userHandle,
        photoUrl = photoUrl,
        isUploadingPhoto = isUploadingPhoto,
        postCount = myRecipesViewModel.myRecipes.size,
        bookmarks = bookmarks,
        onPhotoChangeClick = { imagePicker.launch("image/*") },
        onLogoutClick = onLogout,
        onNavigateToDetail = onNavigateToDetail
    )
}

/**
 * Stateless UI component for the Profile Screen.
 * Accepts only primitive values and lambdas.
 */
@Composable
fun ProfileContent(
    userName: String,
    userHandle: String,
    photoUrl: String?,
    isUploadingPhoto: Boolean,
    postCount: Int,
    bookmarks: List<BookmarkEntity>,
    onPhotoChangeClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onLogoutClick() }) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = "Logout", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Keluar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        HorizontalDivider(color = Color(0xFFF5E6E0))

        // Profile Info Section
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).clickable { onPhotoChangeClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0D5CB)), contentAlignment = Alignment.Center) {
                            Text(userName.take(1).uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF7A45))
                        }
                    }
                }
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFFF7A45)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✏️", fontSize = 12.sp)
                }
            }
            if (isUploadingPhoto) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFFF7A45), strokeWidth = 2.dp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Ganti Foto", fontSize = 12.sp, color = Color(0xFFFF7A45), modifier = Modifier.clickable { onPhotoChangeClick() })
            Spacer(Modifier.height(8.dp))
            Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
            Spacer(Modifier.height(4.dp))
            Text(userHandle, fontSize = 14.sp, color = Color(0xFFFF7A45))
            Spacer(Modifier.height(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(postCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
                Text("Posts", fontSize = 14.sp, color = Color.Gray)
            }
        }

        // Bookmark Header
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            HorizontalDivider(color = Color(0xFFF5E6E0))
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp)) {
                Text("My Bookmark", fontSize = 14.sp, color = Color(0xFFFF7A45), fontWeight = FontWeight.Medium)
            }
        }
        HorizontalDivider(color = Color(0xFFFF7A45), modifier = Modifier.padding(bottom = 16.dp))

        // Bookmarks List
        if (bookmarks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Text("Belum ada resep yang dibookmark.", color = Color.Gray, modifier = Modifier.padding(top = 32.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookmarks) { bookmark ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(bookmark.recipeId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bookmark.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF332211)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFFFF7A45)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    val dummyBookmarks = listOf(
        BookmarkEntity("1", "Nasi Goreng Spesial", "Enak banget", "", "Chef A"),
        BookmarkEntity("2", "Ayam Bakar Madu", "Mantap", "", "Chef B")
    )
    MaterialTheme {
        ProfileContent(
            userName = "Budi Santoso",
            userHandle = "@budisantoso",
            photoUrl = null,
            isUploadingPhoto = false,
            postCount = 5,
            bookmarks = dummyBookmarks,
            onPhotoChangeClick = {},
            onLogoutClick = {},
            onNavigateToDetail = {}
        )
    }
}
