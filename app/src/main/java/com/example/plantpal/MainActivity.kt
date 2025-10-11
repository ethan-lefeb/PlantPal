package com.example.plantpal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.plantpal.ui.theme.PlantPalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantPalTheme {
                val navController = rememberNavController()

                Scaffold { innerPadding ->
                    AppNavGraph(
                        navController = navController,

                        modifier = Modifier.padding(innerPadding)
                    )
                }

            }
        }
    }
}
