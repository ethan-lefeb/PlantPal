package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun StartScreen(navController: NavHostController) {
    StartScreenContent(
        onLoginClick = { navController.navigate("login") },
        onSignupClick = { navController.navigate("signup") }
    )
}

@Composable
fun StartScreenContent(
    onLoginClick: () -> Unit = {},
    onSignupClick: () -> Unit = {}
) {
    // 🌿 Gradient background (same as HomeScreen)
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
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🌿 Title
            Text(
                text = "Welcome to PlantPal!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F5233)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 🌿 Login Button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52796F))
            ) {
                Text("Go to Login", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🌿 Signup Button
            Button(
                onClick = onSignupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52796F))
            ) {
                Text("Create Account", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 🌿 Inspirational quote for balance
            Text(
                text = "“Every plant you nurture helps you grow too.”",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF2F5233)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StartScreenPreview() {
    StartScreenContent()
}
