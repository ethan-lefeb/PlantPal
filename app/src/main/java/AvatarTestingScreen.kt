package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarTestingScreen(
    onBack: () -> Unit
) {
    var family by remember { mutableStateOf("") }
    var genus by remember { mutableStateOf("") }
    var commonName by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    
    var testResult by remember { mutableStateOf<PlantTypeDatabase.MatchResult?>(null) }
    var generatedConfig by remember { mutableStateOf<AvatarConfig?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avatar Testing Tool") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Test Avatar Generation",
                style = MaterialTheme.typography.headlineSmall
            )
            
            OutlinedTextField(
                value = commonName,
                onValueChange = { commonName = it },
                label = { Text("Common Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = family,
                onValueChange = { family = it },
                label = { Text("Family") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = genus,
                onValueChange = { genus = it },
                label = { Text("Genus (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    testResult = PlantTypeDatabase.findMatch(
                        family = family,
                        genus = genus,
                        commonName = commonName,
                        scientificName = scientificName
                    )
                    generatedConfig = AvatarGenerator.generateAvatarForPlant(
                        family = family,
                        genus = genus,
                        commonName = commonName,
                        scientificName = scientificName
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = commonName.isNotBlank() || family.isNotBlank()
            ) {
                Text("Generate Avatar")
            }
            
            if (testResult != null && generatedConfig != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Avatar Preview",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PlantAvatar(
                                avatarConfig = generatedConfig!!,
                                health = "healthy",
                                size = 180.dp,
                                animated = true
                            )
                        }
                        
                        HorizontalDivider()
                        
                        Text("Avatar Type: ${generatedConfig!!.baseType}")
                        Text("Color: ${generatedConfig!!.color}")
                        Text("Matched By: ${testResult!!.matchedBy}")
                        Text("Confidence: ${(testResult!!.confidence * 100).toInt()}%")
                    }
                }
            }
        }
    }
}
