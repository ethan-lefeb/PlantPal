package com.example.plantpal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.util.Base64
import java.security.MessageDigest
import com.example.plantpal.ui.screens.AccountCreationScreen
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.firebase.FirebaseApp
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

fun checkFirebaseConfig() {
    try {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        Log.d("FirebaseCheck", "FirebaseAuth instance created. Current user: $user")
    } catch (e: Exception) {
        Log.e("FirebaseCheck", "Firebase configuration not found or invalid!", e)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Make sure Firebase is initialized
        checkFirebaseConfig()

        enableEdgeToEdge()
        setContent {
            PlantPalTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AccountCreationScreen(
                        onSuccess = {
                            // for now just toast; later you’ll navigate
                            Toast.makeText(
                                this,
                                "Account created successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }
    }
}
