package com.kelompoksatu.kafecraft.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChangePasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(viewModel.isSuccess, viewModel.error) {
        if (viewModel.isSuccess) { showSuccess = true; viewModel.resetState() }
        viewModel.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.resetState() }
    }

    if (showSuccess) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(80.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                Text("✅", fontSize = 40.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text("Password Diganti", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
            Spacer(Modifier.height(48.dp))
            Button(onClick = { onNavigateToLogin() }, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45))) {
                Text("Kembali ke Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Ganti Password", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
            Spacer(Modifier.height(8.dp))
            Text("Masukkan password baru Anda", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Password Baru") },
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, colors = fieldColors)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = confirmNewPassword, onValueChange = { confirmNewPassword = it }, label = { Text("Konfirmasi Password Baru") },
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, colors = fieldColors)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    when {
                        newPassword.isEmpty() || confirmNewPassword.isEmpty() -> Toast.makeText(context, "Harap isi semua field", Toast.LENGTH_SHORT).show()
                        newPassword.length < 6 -> Toast.makeText(context, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                        newPassword != confirmNewPassword -> Toast.makeText(context, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                        else -> viewModel.changePassword(newPassword)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45)),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Ganti Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
