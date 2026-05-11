package com.kelompoksatu.kafecraft.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(viewModel.isSuccess, viewModel.error) {
        if (viewModel.isSuccess) { onNavigateToChangePassword(); viewModel.resetState() }
        viewModel.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.resetState() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.align(Alignment.Start).size(24.dp).clickable { onNavigateBack() })
        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.size(80.dp).background(Color(0xFFF5E6E0), CircleShape), contentAlignment = Alignment.Center) {
            Text("✉️", fontSize = 32.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text("Lupa Password?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
        Spacer(Modifier.height(8.dp))
        Text("Masukkan email dan hint Anda untuk mereset password.\n(Maksimal 3 kali gagal)", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            placeholder = { Text("nama@email.com") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = hint, onValueChange = { hint = it }, label = { Text("Hint") },
            placeholder = { Text("Hint kamu") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                if (email.isNotEmpty() && hint.isNotEmpty()) viewModel.verifyForgotHint(email, hint)
                else Toast.makeText(context, "Harap isi email dan hint", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45)),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Verifikasi Hint", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
