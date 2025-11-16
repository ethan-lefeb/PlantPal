package com.example.plantpal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "StartScreen",
        modifier = modifier
    ) {
        // 🌱 Start Screen
        composable("StartScreen") {
            StartScreen(navController = navController)
        }

        // 🌱 Signup Screen
        composable("signup") {
            AccountCreationScreen(
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("StartScreen") { inclusive = true } // Clear auth flow
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        // 🌱 Login Screen
        composable("login") {
            LoginScreen(
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("StartScreen") { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        // 🌿 Home / Main App Screen
        composable("home") {
            val currentUser =
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid ?: "TEST_USER_123" // fallback for testing

            PlantPalApp(
                currentUserId = currentUserId,
                onSignOut = {
                    AuthRepository.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true } // clear backstack
                    }
                }
            )
        }
    }
}


