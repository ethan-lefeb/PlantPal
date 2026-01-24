package com.example.plantpal.com.example.plantpal.ui.screens.com.example.plantpal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plantpal.com.example.plantpal.data.com.example.plantpal.data.AuthViewModel
import com.example.plantpal.ui.theme.ForestButton
import com.example.plantpal.ui.theme.ForestGradientBalanced
import com.example.plantpal.ui.theme.ForestPrimary
import com.example.plantpal.ui.theme.ForestSecondaryText
import com.example.plantpal.ui.theme.LocalUIScale

@Composable
fun AccountCreationScreen(
    viewModel: AuthViewModel = viewModel(),
    onSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val scaled = LocalUIScale.current  // GET SCALED VALUES

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = ForestPrimary,
                modifier = Modifier
                    .size(scaled.iconSizeLarge * 2)
                    .padding(bottom = scaled.paddingSmall)
            )

            // Header Text
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestPrimary,
                    fontSize = scaled.headlineSmall
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = scaled.paddingLarge)
            )

            // Display Name Field
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display name", fontSize = scaled.bodyMedium) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestPrimary,
                    unfocusedBorderColor = ForestPrimary.copy(alpha = 0.4f),
                    cursorColor = ForestPrimary,
                    focusedLabelColor = ForestPrimary
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = scaled.bodyLarge)
            )

            Spacer(modifier = Modifier.height(scaled.spacingMedium))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontSize = scaled.bodyMedium) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestPrimary,
                    unfocusedBorderColor = ForestPrimary.copy(alpha = 0.4f),
                    cursorColor = ForestPrimary,
                    focusedLabelColor = ForestPrimary
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = scaled.bodyLarge)
            )

            Spacer(modifier = Modifier.height(scaled.spacingMedium))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontSize = scaled.bodyMedium) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestPrimary,
                    unfocusedBorderColor = ForestPrimary.copy(alpha = 0.4f),
                    cursorColor = ForestPrimary,
                    focusedLabelColor = ForestPrimary
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = scaled.bodyLarge)
            )

            Spacer(modifier = Modifier.height(scaled.spacingLarge))

            // Sign Up Button
            Button(
                onClick = { viewModel.register(email.trim(), password, displayName.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaled.buttonHeight),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ForestButton),
                shape = RoundedCornerShape(50)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(scaled.iconSizeSmall)
                    )
                } else {
                    Text(
                        "Sign up",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = scaled.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(scaled.spacingMedium))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "Already have an account? Log in",
                    color = ForestPrimary,
                    fontSize = scaled.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(scaled.spacingSmall))

            // Error or success feedback
            when {
                uiState.error != null -> Text(
                    text = "Error: ${uiState.error}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = scaled.bodyMedium
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = scaled.paddingSmall)
                )

                uiState.success -> {
                    LaunchedEffect(uiState.success) {
                        onSuccess()
                        viewModel.resetState()
                    }
                }
            }

            Spacer(modifier = Modifier.height(scaled.spacingMedium))

            Text(
                text = "Join the PlantPal community and start growing!",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ForestSecondaryText,
                    textAlign = TextAlign.Center,
                    fontSize = scaled.bodySmall
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccountCreationScreenPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        AccountCreationScreen(
            onNavigateToLogin = {},
            onSuccess = {}
        )
    }
}