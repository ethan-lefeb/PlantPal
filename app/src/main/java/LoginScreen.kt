package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onSuccess: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // NEW: Show username if available
        uiState.username?.let { name ->
            Text(
                text = "Welcome, $name!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(email.trim(), password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Log in")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToSignup) {
            Text("Need an account? Sign up")
        }

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.error != null -> Text(
                text = "Error: ${uiState.error}",
                color = MaterialTheme.colorScheme.error
            )
            uiState.success -> {
                LaunchedEffect(uiState.success) {
                    onSuccess()
                    viewModel.resetState()
                }
            }
        }
    }
}