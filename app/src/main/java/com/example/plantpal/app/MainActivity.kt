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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.AuthRepository
import com.example.plantpal.com.example.plantpal.ui.screens.com.example.plantpal.ui.screens.AccountCreationScreen
import com.example.plantpal.com.example.plantpal.ui.screens.com.example.plantpal.ui.screens.LoginScreen
import com.example.plantpal.screens.detail.PlantDetailScreenWrapper
import com.example.plantpal.screens.profile.SocialDashboardScreen
import com.example.plantpal.ui.theme.PlantPalTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

        // Request permissions for notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Save FCM token when available
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = AuthRepository.currentUserId()
            if (userId != null) {
                saveUserProfileToFirestore(userId, token)
            } else {
                println("⚠️ Token received but user not logged in yet: $token")
            }
        }

        setContent {
            PlantPalTheme {
                Scaffold { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun saveUserProfileToFirestore(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        val user = FirebaseAuth.getInstance().currentUser
        val emailLower = user?.email?.trim()?.lowercase()
        val displayName = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "User"

        val data = mutableMapOf<String, Any>(
            "fcmToken" to token,
            "displayName" to displayName
        )
        if (!emailLower.isNullOrBlank()) data["emailLower"] = emailLower

        userRef
            .set(data, SetOptions.merge())
            .addOnSuccessListener { println("✅ User profile updated: $userId") }
            .addOnFailureListener { e -> println("⚠️ Failed to update user profile: ${e.message}") }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentUserId = AuthRepository.currentUserId()

    // IMPORTANT: only use routes that actually exist in this NavHost
    val startDestination = if (currentUserId != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
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

        composable("home") {
            val uid = AuthRepository.currentUserId()
            if (uid == null) {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            } else {
                PlantPalApp(
                    currentUserId = uid,
                    onSignOut = {
                        AuthRepository.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("socialDashboard") {
            SocialDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "plantDetail/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            PlantDetailScreenWrapper(
                plantId = plantId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
