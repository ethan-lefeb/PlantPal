package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

data class Tab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantPalApp() {
    val navController = rememberNavController()
    val tabs = listOf(
        Tab("home", "Home", Icons.Filled.Home),
        Tab("library", "Library", Icons.Filled.Star),
        Tab("alerts", "Alerts", Icons.Filled.Notifications),
        Tab("profile", "Profile", Icons.Filled.AccountCircle),
    )

    // Background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFB5E48C),
                        Color(0xFFD9ED92),
                        Color(0xFF99D98C)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFE9F5DB),
                    tonalElevation = 8.dp
                ) {
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val current = backStackEntry?.destination?.route

                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (current == tab.route)
                                        Color(0xFF2F5233)
                                    else
                                        Color(0xFF52796F)
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    color = if (current == tab.route)
                                        Color(0xFF2F5233)
                                    else
                                        Color(0xFF52796F),
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val current = backStackEntry?.destination?.route

                // Only show the + button on the Home screen
                if (current == "home") {
                    FloatingActionButton(
                        onClick = { navController.navigate("addPlant") },
                        containerColor = Color(0xFFFF6F61),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plant")
                    }
                }
            }
        ) { innerPadding ->
            // Ensure screens respect the inner padding so FAB and nav bar are visible
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                composable("home") {
                    // Routes to real Home Screen
                    HomeScreen(navController)
                }
                composable("library") {
                    CenterText("Explore the Plant Library 📚", title = "Library")
                }
                composable("alerts") {
                    CenterText("Stay on top of watering & care reminders 🔔", title = "Alerts")
                }
                composable("profile") {
                    CenterText("Manage your PlantPal profile 🌿", title = "Profile")
                }
                composable("addPlant") {
                    AddPlantCaptureScreen(
                        onSaved = { uri ->
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterText(text: String, title: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (title != null) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF2F5233),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = Color(0xFF52796F),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlantPalAppPreview() {
    MaterialTheme {
        PlantPalApp()
    }
}
