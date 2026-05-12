package com.kelompoksatu.kafecraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.ui.navigation.AppNavigation
import com.kelompoksatu.kafecraft.ui.theme.KafecraftTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

        enableEdgeToEdge()
        setContent {
            KafecraftTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // To handle the inner padding nicely across screens, we can either pass it or apply it to a Box
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(sessionManager = sessionManager)
                    }
                }
            }
        }
    }
}