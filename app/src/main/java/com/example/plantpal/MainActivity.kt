package com.example.plantpal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // requests permissions to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // retrieves  and stores FCM token
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = AuthRepository.currentUserId()
            if (userId != null) {
                saveFcmTokenToFirestore(userId, token)
            }
        }

        setContent {
            PlantPalTheme {
                val startDestination = if (AuthRepository.currentUserId() != null) {
                    "home"
                } else {
                    "start"
                }

                Scaffold { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    // registers permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                println("Notification permission not granted.")
            }
        }

    // stores fcm token in firestore for targeted notifications
    private fun saveFcmTokenToFirestore(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        userRef.update("fcmToken", token)
            .addOnSuccessListener { println("Token saved for user: $userId") }
            .addOnFailureListener { e ->
                println("Failed to save FCM token: ${e.message}")
            }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, startDestination: String = "start") {
    val navController = rememberNavController()
    val currentUserId = AuthRepository.currentUserId() ?: "TEST_USER_123" // fallback for testing

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // --- AUTH SCREENS ---
        composable("start") { HomeScreen(navController) }

        composable("login") {
            LoginScreen(
                onSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            AccountCreationScreen(
                onSuccess = {
                    navController.navigate("home") { popUpTo("signup") { inclusive = true } }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        // --- MAIN APP HOME ---
        composable("home") {
            PlantPalApp(
                currentUserId = currentUserId,
                onSignOut = {
                    AuthRepository.signOut()
                    navController.navigate("login") { popUpTo("home") { inclusive = true } }
                }
            )
        }

        // --- PLANT DETAIL SCREEN ---
        composable(
            route = "plantDetail/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            PlantDetailScreen(
                plantId = plantId,
                userId = currentUserId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
