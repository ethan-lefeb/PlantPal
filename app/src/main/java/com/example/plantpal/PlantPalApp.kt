package com.example.plantpal

import android.util.Log
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
import com.example.plantpal.screens.profile.SettingsScreen
import com.example.plantpal.screens.profile.DeveloperSettingsScreen
import com.example.plantpal.screens.detail.PlantDetailScreenWrapper
import androidx.work.WorkManager
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.AvatarConfig
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.PlantProfile
import com.example.plantpal.com.example.plantpal.ui.screens.com.example.plantpal.ui.screens.DashboardScreen
import com.example.plantpal.screens.profile.SocialDashboardScreen
import com.example.plantpal.ui.theme.ForestGradientBalanced

val blossomPill = Color(0xFFF7D6E4)
val blossomOn = Color(0xFF6A3347)

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
                Brush.verticalGradient(ForestGradientBalanced)
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
                    currentRoute != "settings" &&
                    currentRoute != "developerSettings" &&
                    currentRoute != "badges" &&
                    currentRoute != "avatarCustomization" &&
                    currentRoute != "socialDashboard") {

                    NavigationBar(
                        containerColor = Color(0xFF52796F),
                        contentColor = Color.White
                    ) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo("home") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        tab.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    // 🌸 selected pill color
                                    indicatorColor = blossomPill,

                                    // selected icon/text on pill
                                    selectedIconColor = blossomOn,
                                    selectedTextColor = blossomOn,

                                    // unselected icon/text on green bar
                                    unselectedIconColor = Color.White,
                                    unselectedTextColor = Color.White
                                )
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
                            navController.navigate("plantDetail/$currentUserId/$plantId") {
                                launchSingleTop = true
                            }
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
                    ProfileScreen(
                        onSignOut = onSignOut,
                        onDeveloperSettings = { navController.navigate("developerSettings") },
                        onSettings = { navController.navigate("settings") },
                        onBadges = { navController.navigate("badges") },
                        onSocialDashboard = { navController.navigate("socialDashboard") }
                    )
                }

                composable("badges") {
                    BadgesScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("socialDashboard") {
                    SocialDashboardScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        workManager = WorkManager.getInstance(),
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("developerSettings") {
                    DeveloperSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
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
                            navController.popBackStack("library", inclusive = false)
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

object AvatarGenerator {
    private const val TAG = "AvatarGen"

    data class GenerationResult(
        val config: AvatarConfig,
        val confidence: Float,
        val matchedBy: String,
        val suggestions: List<String> = emptyList()
    )

    fun generateAvatarForPlant(
        family: String?,
        genus: String?,
        commonName: String,
        scientificName: String = ""
    ): AvatarConfig {
        Log.d(TAG, "=== Generating Avatar ===")
        Log.d(TAG, "Family: '$family'")
        Log.d(TAG, "Genus: '$genus'")
        Log.d(TAG, "Common Name: '$commonName'")
        Log.d(TAG, "Scientific Name: '$scientificName'")

        val match = PlantTypeDatabase.findMatch(family, genus, commonName, scientificName)

        val config = AvatarConfig(
            baseType = match.entry.avatarType,
            color = match.entry.defaultColor,
            potColor = "terracotta",
            potStyle = "classic"
        )

        Log.d(TAG, "Generated Base Type: '${config.baseType}'")
        Log.d(TAG, "Generated Color: '${config.color}'")
        Log.d(TAG, "Match Confidence: ${match.confidence}")
        Log.d(TAG, "Matched By: ${match.matchedBy}")
        Log.d(TAG, "=== Complete ===")

        return config
    }

    fun generateAvatarWithDetails(
        family: String?,
        genus: String?,
        commonName: String,
        scientificName: String = ""
    ): GenerationResult {
        Log.d(TAG, "=== Generating Avatar (Detailed) ===")
        Log.d(TAG, "Family: '$family'")
        Log.d(TAG, "Genus: '$genus'")
        Log.d(TAG, "Common Name: '$commonName'")
        Log.d(TAG, "Scientific Name: '$scientificName'")

        val match = PlantTypeDatabase.findMatch(family, genus, commonName, scientificName)

        val config = AvatarConfig(
            baseType = match.entry.avatarType,
            color = match.entry.defaultColor,
            potColor = "terracotta",
            potStyle = "classic"
        )

        Log.d(TAG, "Generated Base Type: '${config.baseType}'")
        Log.d(TAG, "Generated Color: '${config.color}'")
        Log.d(TAG, "Match Confidence: ${match.confidence}")
        Log.d(TAG, "Matched By: ${match.matchedBy}")
        Log.d(TAG, "=== Complete ===")

        val suggestions = if (match.confidence < 0.7f) {
            match.entry.alternateColors.take(2)
        } else {
            emptyList()
        }

        return GenerationResult(
            config = config,
            confidence = match.confidence,
            matchedBy = match.matchedBy,
            suggestions = suggestions
        )
    }

    fun generateForPlantProfile(plant: PlantProfile): GenerationResult {
        return generateAvatarWithDetails(
            family = plant.careInfo.family,
            genus = plant.careInfo.genus,
            commonName = plant.commonName,
            scientificName = plant.scientificName
        )
    }

    fun generateRandomAvatar(): AvatarConfig {
        val types = PlantTypeDatabase.getAllAvatarTypes()
        val colors = PlantTypeDatabase.getAllColors()
        val potColors = listOf("terracotta", "ceramic_white", "ceramic_blue", "ceramic_green",
            "modern_gray", "rustic_brown", "pink", "yellow", "purple")
        val potStyles = listOf("classic", "modern", "hanging")

        return AvatarConfig(
            baseType = types.random(),
            color = colors.random(),
            potColor = potColors.random(),
            potStyle = potStyles.random()
        )
    }

    fun updateAvatarForPlantState(
        currentConfig: AvatarConfig,
        health: String,
        lastWatered: Long,
        wateringFrequency: Int
    ): AvatarConfig {
        return currentConfig
    }

    fun suggestImprovedAvatar(
        plant: PlantProfile,
        currentConfig: AvatarConfig
    ): AvatarConfig? {
        val result = generateForPlantProfile(plant)
        return if (result.config != currentConfig && result.confidence > 0.8f) {
            result.config
        } else {
            null
        }
    }

    fun validateAvatar(plant: PlantProfile, config: AvatarConfig): ValidationResult {
        val expectedResult = generateForPlantProfile(plant)

        val typeMatches = config.baseType == expectedResult.config.baseType
        val colorReasonable = config.color in PlantTypeDatabase.getAllColors()

        val issues = mutableListOf<String>()

        if (!typeMatches && expectedResult.confidence > 0.7f) {
            issues.add("Avatar type '${config.baseType}' doesn't match plant family. Expected '${expectedResult.config.baseType}'")
        }

        if (!colorReasonable) {
            issues.add("Color '${config.color}' is not a valid option")
        }

        val isValid = issues.isEmpty()
        val suggestion = if (!isValid) expectedResult.config else null

        return ValidationResult(
            isValid = isValid,
            issues = issues,
            suggestion = suggestion
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val issues: List<String>,
        val suggestion: AvatarConfig?
    )

    fun explainAvatarChoice(result: GenerationResult): String {
        return when (result.matchedBy) {
            "family" -> "Avatar chosen based on plant family. This is highly accurate."
            "genus" -> "Avatar chosen based on plant genus. This is very reliable."
            "scientific_name" -> "Avatar matched using scientific name patterns."
            "keyword" -> "Avatar matched using common name keywords. " +
                    if (result.confidence > 0.7f) "Good match." else "Consider customizing if incorrect."
            "partial_family" -> "Avatar matched using partial family name. May need adjustment."
            "default" -> "Generic avatar used - plant type not in database. " +
                    "You can customize this avatar to better match your plant."
            else -> "Avatar generated using available plant information."
        }
    }
}