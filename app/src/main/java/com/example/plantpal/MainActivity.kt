package com.example.plantpal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                println("Notification permission not granted.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ notification permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Retrieve FCM token
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = AuthRepository.currentUserId()
            if (userId != null) {
                saveFcmTokenToFirestore(userId, token)
            }
        }

        setContent {
            PlantPalTheme {
                val startDestination =
                    if (AuthRepository.currentUserId() != null) "home" else "start"

                Scaffold { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    private fun saveFcmTokenToFirestore(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        userRef.update("fcmToken", token)
            .addOnSuccessListener { println("Token saved for user: $userId") }
            .addOnFailureListener { e -> println("Failed to save FCM token: ${e.message}") }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, startDestination: String = "start") {
    val navController = rememberNavController()
    val currentUserId = AuthRepository.currentUserId() ?: "TEST_USER_123"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // Start Screen
        composable("start") {
            AccountCreationScreen()
        }

        // Login Screen
        composable("login") {
            LoginScreen(
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        // Signup Screen
        composable("signup") {
            AccountCreationScreen(
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        // Home Screen
        composable("home") {
            PlantPalApp(
                currentUserId = currentUserId,
                onSignOut = {
                    AuthRepository.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // Plant Detail
        composable(
            route = "plantDetail/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            val repo = remember { PlantRepository() }

            var plant by remember { mutableStateOf<PlantProfile?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(plantId) {
                isLoading = true
                val res = repo.getPlant(plantId)
                if (res.isSuccess) {
                    plant = res.getOrNull()
                    error = null
                } else {
                    error = res.exceptionOrNull()?.message ?: "Unknown error"
                }
                isLoading = false
            }

            when {
                isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Error: $error", color = MaterialTheme.colorScheme.error) }

                plant != null -> {
                    PlantCareDetailScreen(
                        plant = plant!!,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
