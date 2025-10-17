package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun HomeScreenContent(
    username: String,
    onSignOut: () -> Unit = {}
) {
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
            //
            Text(
                text = "Welcome back, $username!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F5233)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "My Plants",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F5233)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlantCard("Monstera", "Healthy 🌿")
                PlantCard("Succulent", "Needs water 💧")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61)),
                shape = RoundedCornerShape(50)
            ) {
                Text("Log out", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "“Plants give us oxygen for the lungs and the soul.”",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF52796F),
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun PlantCard(name: String, status: String) {
    Card(
        modifier = Modifier
            .size(width = 120.dp, height = 120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F5DB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFlorist,
                contentDescription = null,
                tint = Color(0xFF2F5233),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.Bold, color = Color(0xFF2F5233))
            Text(status, fontSize = 12.sp, color = Color(0xFF52796F))
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var username by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        username = AuthRepository.getCurrentUserName()
    }

    HomeScreenContent(
        username = username ?: "User",
        onSignOut = {
            viewModel.resetState()
            AuthRepository.signOut()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(username = "PreviewUser")
}
