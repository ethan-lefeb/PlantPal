package com.example.plantpal

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "signup") {

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

        composable("home") {
            PlantPalApp(
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