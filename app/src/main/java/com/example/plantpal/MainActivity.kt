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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plantpal.screens.detail.PlantDetailScreenWrapper
import com.example.plantpal.screens.start.StartScreen
import com.example.plantpal.ui.theme.PlantPalTheme
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

        // 🔔 Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 🔑 Save FCM token
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
        db.collection("users")
            .document(userId)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    startDestination: String = "start"
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // 🌱 Start
        composable("start") {
            StartScreen(navController)
        }

        // 🔐 Login
        composable("login") {
            LoginScreen(
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }

        // ✍️ Signup
        composable("signup") {
            AccountCreationScreen(
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        // 🏡 Home (ROOT SCREEN)
        composable("home") {
            val userId = AuthRepository.currentUserId()

            if (userId == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            } else {
                PlantPalApp(
                    currentUserId = userId,
                    onSignOut = {
                        AuthRepository.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }

        // 🌿 Plant Detail (child of Home)
        composable(
            route = "plantDetail/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantId =
                backStackEntry.arguments?.getString("plantId") ?: return@composable

            PlantDetailScreenWrapper(
                plantId = plantId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

