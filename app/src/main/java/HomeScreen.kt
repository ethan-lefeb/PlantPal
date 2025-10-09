package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreenContent(
    username: String,
    onSignOut: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $username!",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onSignOut) {
            Text("Log out")
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
