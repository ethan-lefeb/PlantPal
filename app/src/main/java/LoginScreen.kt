package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {

            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF2F5233)
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Let's get you back to your plants 🌿",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF2F5233)),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = AuthRepository.login(email.trim(), password)
                        isLoading = false
                        result.onSuccess { onSuccess() }
                            .onFailure { e ->
                                errorMessage = e.message ?: "Login failed"
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF52796F),
                    contentColor = Color.White
                )
            ) {
                Text(text = if (isLoading) "Logging in..." else "Log in")
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToSignup,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF2F5233)
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Need an account? Sign up")
            }
        }
    }
}
