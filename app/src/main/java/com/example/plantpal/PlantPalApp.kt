package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

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

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            if (currentRoute != null && !currentRoute.startsWith("plantDetail/")) {
                NavigationBar {
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
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val current = backStackEntry?.destination?.route
            if (current == "home") {
                FloatingActionButton(onClick = { navController.navigate("addPlant/$currentUserId") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Plant")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val plantsViewModel: PlantsViewModel = viewModel()
                PlantsHomeScreen(
                    viewModel = plantsViewModel,
                    onPlantClick = { plantId ->
                        navController.navigate("plantDetail/$currentUserId/$plantId")
                    }
                )
            }

                LaunchedEffect(Unit) {
                    plantsViewModel.loadPlants()
                }

                PlantsHomeScreen(
                    viewModel = plantsViewModel,
                    onPlantClick = { plantId ->
                        navController.navigate("plantDetail/$plantId")
                    }
                )
            }

            composable("library") { CenterText("Plant Library (placeholder)") }
            composable("alerts") { CenterText("Notifications (placeholder)") }
            composable("profile") { ProfileScreen(onSignOut = onSignOut) }

            composable(
                route = "addPlant/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                AddPlantCaptureScreen(
                    apiKey = PlantIdSecret.API_KEY,
                    currentUserId = userId,
                    onSaved = { savedPlantId ->
                        // Return to home after saving
                        navController.popBackStack("home", inclusive = false)
                    }
                )
            }
            composable(
                route = "plantDetail/{plantId}",
                arguments = listOf(navArgument("plantId") { type = NavType.StringType })
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

@Composable
fun PlantDetailScreenWrapper(
    plantId: String,
    onBack: () -> Unit
) {
    val plantsViewModel: PlantsViewModel = viewModel()
    val uiState by plantsViewModel.uiState.collectAsState()

    LaunchedEffect(plantId) {
        plantsViewModel.loadPlants()
    }

    val plant = uiState.plants.find { it.plantId == plantId }

    if (plant != null) {
        PlantCareDetailScreen(
            plant = plant,
            onBack = onBack
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Plant not found", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Sign Out") }
    }
}

@Composable
private fun CenterText(text: String) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
