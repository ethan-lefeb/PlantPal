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
        // 🌱 Start / Auth Screens
        composable("StartScreen") {
            StartScreen(navController = navController)
        }

        composable("signup") {
            AccountCreationScreen(
                onSuccess = {
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = true } // Clear back stack
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        composable("login") {
            LoginScreen(
                onSuccess = {
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        // 🌿 Main App with Bottom Navigation Bar
        composable("main") {
            PlantPalApp() // This contains its own NavHost and bottom nav
        }
    }
}
