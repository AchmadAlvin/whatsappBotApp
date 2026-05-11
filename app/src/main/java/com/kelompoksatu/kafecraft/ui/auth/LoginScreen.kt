package com.kelompoksatu.kafecraft.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(viewModel.isSuccess, viewModel.error) {
        if (viewModel.isSuccess) { onNavigateToHome(); viewModel.resetState() }
        viewModel.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.resetState() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(80.dp).background(Color(0xFFFFEBE0), CircleShape), contentAlignment = Alignment.Center) {
            Text("🍴", fontSize = 32.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
        Spacer(Modifier.height(8.dp))
        Text("Sign in to discover delicious recipes", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            placeholder = { Text("your@email.com") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Password") },
            placeholder = { Text("Enter your password") }, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(8.dp))
        Text("Forgot Password?", color = Color(0xFFFF7A45), modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgot() }.padding(8.dp))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) viewModel.login(email, password)
                else Toast.makeText(context, "Harap isi email dan password", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45)),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account? ", color = Color.Gray)
            Text("Sign Up", color = Color(0xFFFF7A45), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
        }
    }
}
