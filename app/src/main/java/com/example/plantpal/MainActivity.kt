package com.example.plantpal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plantpal.ui.theme.PlantPalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, startDestination: String = "start") {
    val navController = rememberNavController()

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
                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            AccountCreationScreen(
                onSuccess = {
                    navController.navigate("home") { popUpTo("signup") { inclusive = true } }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        // Pass top-level onSignOut callback to PlantPalApp
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
