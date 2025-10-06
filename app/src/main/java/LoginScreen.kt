package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.plantpal.ui.theme.PlantPalBackground

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreen(
    onSuccess: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {}
) {
    // Fake ViewModel-less state for preview
    val fakeUiState = AuthUiState(
        username = "Plant Lover",
        isLoading = false,
        error = null,
        success = false
    )

    PlantPalBackground {
        MaterialTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Welcome, ${fakeUiState.username}!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = "test@email.com",
                    onValueChange = {},
                    label = { Text("Email") }
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = "password",
                    onValueChange = {},
                    label = { Text("Password") }
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { onSuccess() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log in")
                }

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = { onNavigateToSignup() }) {
                    Text("Need an account? Sign up")
                }
            }
        }
    }
}
