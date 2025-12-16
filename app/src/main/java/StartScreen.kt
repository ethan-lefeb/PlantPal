package com.example.plantpal.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.plantpal.ui.theme.ForestButton
import com.example.plantpal.ui.theme.ForestGradientBalanced
import com.example.plantpal.ui.theme.ForestPrimary
import com.example.plantpal.ui.theme.ForestSecondaryText

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = ForestGradientBalanced
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to PlantPal!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestPrimary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = ForestButton)
            ) {
                Text("Go to Login", color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = ForestButton)
            ) {
                Text("Create Account", color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "“Every plant you nurture helps you grow too.”",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ForestSecondaryText
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
