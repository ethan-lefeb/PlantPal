package com.example.plantpal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    // registers permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                println("Notification permission not granted.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // requests permissions to post notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // retrieves and stores FCM token
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

        composable("home") {
            PlantPalApp(
                currentUserId = currentUserId,
                onSignOut = {
                    AuthRepository.signOut()
                    navController.navigate("login") { popUpTo("home") { inclusive = true } }
                }
            )
        }

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
                val res = repo.getPlant(plantId) // Result<PlantProfile>
                if (res.isSuccess) {
                    plant = res.getOrNull()
                    error = null
                } else {
                    error = res.exceptionOrNull()?.message ?: "Unknown error"
                }
                isLoading = false
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }

                plant != null -> {
                    val currentPlant = plant!! // avoid delegated smart-cast issues
                    PlantCareDetailScreen(
                        plant = currentPlant,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
