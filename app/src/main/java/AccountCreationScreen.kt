package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AccountCreationScreen(
    viewModel: AuthViewModel = viewModel(),
    onSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

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
            onClick = { viewModel.register(email.trim(), password, displayName.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Sign up")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in")
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
