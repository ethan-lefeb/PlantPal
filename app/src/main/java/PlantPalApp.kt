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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Single nav tab model
data class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantPalApp(
    currentUserId: String,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()

    val tabs = listOf(
        BottomNavTab("home", "Home", Icons.Filled.Home),
        BottomNavTab("library", "Library", Icons.Filled.Star),
        BottomNavTab("alerts", "Alerts", Icons.Filled.Notifications),
        BottomNavTab("profile", "Profile", Icons.Filled.AccountCircle),
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
                    val currentRoute = backStackEntry?.destination?.route

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
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (currentRoute == tab.route)
                                        Color(0xFF2F5233)
                                    else
                                        Color(0xFF52796F)
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    color = if (currentRoute == tab.route)
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
                val currentRoute = backStackEntry?.destination?.route

                // Only show the + button on the Home screen
                if (currentRoute == "home") {
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
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                composable("home") {
                    HomeScreen(navController)
                }
                composable("library") {
                    CenterText("Explore the Plant Library 📚", title = "Library")
                }
                composable("alerts") {
                    CenterText("Stay on top of watering & care reminders 🔔", title = "Alerts")
                }
                composable("profile") {
                    ProfileScreen(onSignOut = onSignOut)
                }
                composable("addPlant") {
                    AddPlantCaptureScreen(
                        apiKey = PlantIdSecret.API_KEY,
                        currentUserId = currentUserId,
                        onSaved = {
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun CenterText(
    text: String,
    title: String? = null
) {
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
        PlantPalApp(
            currentUserId = "TEST_USER_123",
            onSignOut = {}
        )
    }
}


