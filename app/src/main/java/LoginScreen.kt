package com.example.plantpal

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.plantpal.ui.theme.ForestPrimary
import com.example.plantpal.ui.theme.ForestGradientBalanced
import com.example.plantpal.ui.theme.ForestButton
import com.example.plantpal.ui.theme.ForestSecondaryText
import com.example.plantpal.ui.theme.ForestCardBackground
import com.example.plantpal.ui.components.EntryButton


@Composable
fun LoginScreen(
    onSuccess: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {}
) {
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
            .padding(24.dp)
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
                    .size(64.dp)
                    .padding(bottom = 8.dp)
            )

            // Header Text
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestPrimary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestPrimary,
                    unfocusedBorderColor = ForestPrimary.copy(alpha = 0.4f),
                    cursorColor = ForestPrimary,
                    focusedLabelColor = ForestPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestPrimary,
                    unfocusedBorderColor = ForestPrimary.copy(alpha = 0.4f),
                    cursorColor = ForestPrimary,
                    focusedLabelColor = ForestPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

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


            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToSignup) {
                Text(
                    "Need an account? Sign up",
                    color = ForestPrimary,
                    fontSize = 14.sp
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Grow your green journey with PlantPal",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ForestSecondaryText,
                    textAlign = TextAlign.Center
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
