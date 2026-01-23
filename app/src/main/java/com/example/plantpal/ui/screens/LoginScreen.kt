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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.plantpal.com.example.plantpal.systems.helpers.com.example.plantpal.systems.helpers.AuthRepository
import com.example.plantpal.ui.components.EntryButton
import com.example.plantpal.ui.theme.ForestGradientBalanced
import com.example.plantpal.ui.theme.ForestPrimary
import com.example.plantpal.ui.theme.ForestSecondaryText
import com.example.plantpal.ui.theme.LocalUIScale
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    onSuccess: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {}
) {
    val scaled = LocalUIScale.current
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestPrimary,
                    fontSize = scaled.headlineSmall
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = scaled.paddingLarge)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontSize = scaled.bodyMedium) },
                singleLine = true,
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
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
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

            EntryButton(
                text = "Login"
            ) {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password are required."
                    return@EntryButton
                }

                isLoading = true
                errorMessage = null

                scope.launch {
                    val result = AuthRepository.login(email.trim(), password)
                    isLoading = false
                    result
                        .onSuccess { onSuccess() }
                        .onFailure { e ->
                            errorMessage = e.message ?: "Login failed"
                        }
                }
            }

            Spacer(modifier = Modifier.height(scaled.spacingMedium))

            TextButton(onClick = onNavigateToSignup) {
                Text(
                    "Need an account? Sign up",
                    color = ForestPrimary,
                    fontSize = scaled.bodyMedium
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(scaled.spacingSmall))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = scaled.bodyMedium
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = scaled.paddingSmall)
                )
            }

            Spacer(modifier = Modifier.height(scaled.spacingMedium))

            Text(
                text = "Grow your green journey with PlantPal",
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
fun LoginScreenPreview() {
    LoginScreen()
}