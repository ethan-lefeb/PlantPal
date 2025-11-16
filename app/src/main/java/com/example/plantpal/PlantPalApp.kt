package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.plantpal.screens.profile.ProfileScreen
import com.example.plantpal.screens.detail.PlantDetailScreenWrapper


data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantPalApp(
    currentUserId: String,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()

    val tabs = listOf(
        Tab("home", "Home", Icons.Filled.Home),
        Tab("library", "Library", Icons.Filled.Star),
        Tab("alerts", "Alerts", Icons.Filled.Notifications),
        Tab("profile", "Profile", Icons.Filled.AccountCircle),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB5E48C),
                        Color(0xFFD9ED92),
                        Color(0xFF99D98C)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                if (currentRoute != null &&
                    !currentRoute.startsWith("plantDetail/") &&
                    !currentRoute.startsWith("addPlant/") &&
                    currentRoute != "developerSettings" &&
                    currentRoute != "avatarCustomization") {

                    NavigationBar(
                        containerColor = Color(0xFF52796F),
                        contentColor = Color.White
                    ) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label, tint = Color.White) },
                                label = {
                                    Text(
                                        tab.label,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val current = backStackEntry?.destination?.route
                if (current == "library") {
                    FloatingActionButton(
                        onClick = { navController.navigate("addPlant/$currentUserId") },
                        containerColor = Color(0xFF52796F),
                        contentColor = Color.White,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plant")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(top = 8.dp)
            ) {

                composable("home") {
                    DashboardScreen(
                        onOpenLibrary = { navController.navigate("library") },
                        onAddPlant = { navController.navigate("addPlant/$currentUserId") },
                        onOpenPlant = { plantId ->
                            navController.navigate("plantDetail/$currentUserId/$plantId")
                        }
                    )
                }

                composable("library") {
                    val plantsViewModel: PlantsViewModel = viewModel()
                    LaunchedEffect(Unit) { plantsViewModel.loadPlants() }

                    PlantsHomeScreen(
                        viewModel = plantsViewModel,
                        onPlantClick = { plantId ->
                            navController.navigate("plantDetail/$currentUserId/$plantId")
                        }
                    )
                }

                composable("alerts") {
                    AlertsScreen(
                        onOpenPlant = { plantId ->
                            navController.navigate("plantDetail/$currentUserId/$plantId")
                        }
                    )
                }

                composable("profile") {
                    ProfileScreen(onSignOut = onSignOut)
                }

                composable(
                    route = "addPlant/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    AddPlantCaptureScreen(
                        apiKey = PlantIdSecret.API_KEY,
                        currentUserId = userId,
                        onSaved = {
                            navController.popBackStack(
                                "library",
                                inclusive = false
                            )
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "plantDetail/{userId}/{plantId}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("plantId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getString("plantId")
                    if (plantId != null) {
                        PlantDetailScreenWrapper(
                            plantId = plantId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF2F5233)
            )
        )
    }
}
