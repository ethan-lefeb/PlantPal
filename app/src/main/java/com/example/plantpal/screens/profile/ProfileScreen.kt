package com.example.plantpal.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.plantpal.ui.theme.ForestGradientBalanced
import com.example.plantpal.ui.theme.LocalUIScale
import androidx.compose.material.icons.filled.People

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onSettings: () -> Unit = {},
    onDeveloperSettings: () -> Unit = {},
    onSocialDashboard: () -> Unit = {},
    onBadges: () -> Unit = {}
) {
    val scaled = LocalUIScale.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(ForestGradientBalanced)
            )
            .padding(scaled.paddingLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(scaled.spacingMedium)
        ) {
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF2F5233),
                    fontSize = scaled.headlineMedium,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = scaled.paddingLarge)
            )

            OutlinedButton(
                onClick = onBadges,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaled.buttonHeight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2F5233)
                )
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Badges",
                    modifier = Modifier.size(scaled.iconSizeMedium)
                )
                Spacer(Modifier.width(scaled.spacingSmall))
                Text(
                    "Badges & Progress",
                    fontSize = scaled.labelLarge
                )
            }

            OutlinedButton(
                onClick = onSocialDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaled.buttonHeight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2F5233)
                )
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = "Social",
                    modifier = Modifier.size(scaled.iconSizeMedium)
                )
                Spacer(Modifier.width(scaled.spacingSmall))
                Text(
                    "Social Dashboard",
                    fontSize = scaled.labelLarge
                )
            }

            OutlinedButton(
                onClick = onSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaled.buttonHeight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2F5233)
                )
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Settings",
                    modifier = Modifier.size(scaled.iconSizeMedium)
                )
                Spacer(Modifier.width(scaled.spacingSmall))
                Text(
                    "Settings",
                    fontSize = scaled.labelLarge
                )
            }

            OutlinedButton(
                onClick = onDeveloperSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaled.buttonHeight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2F5233)
                )
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Developer Settings",
                    modifier = Modifier.size(scaled.iconSizeMedium)
                )
                Spacer(Modifier.width(scaled.spacingSmall))
                Text(
                    "Developer Settings",
                    fontSize = scaled.labelLarge
                )
            }

            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaled.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF52796F),
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = "Sign out",
                    modifier = Modifier.size(scaled.iconSizeMedium)
                )
                Spacer(Modifier.width(scaled.spacingSmall))
                Text(
                    "Sign Out",
                    fontSize = scaled.labelLarge
                )
            }
        }
    }
}