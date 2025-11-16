package com.example.plantpal

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarCustomizationScreen(
    currentConfig: AvatarConfig,
    plantName: String,
    plant: PlantProfile? = null,
    onSave: (AvatarConfig) -> Unit,
    onBack: () -> Unit
) {
    var selectedBaseType by remember { mutableStateOf(currentConfig.baseType.ifEmpty { "generic" }) }
    var selectedColor by remember { mutableStateOf(currentConfig.color.ifEmpty { "green" }) }
    
    val previewConfig = AvatarConfig(
        baseType = selectedBaseType,
        color = selectedColor
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize $plantName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSave(previewConfig) }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Preview",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Card(
                modifier = Modifier.size(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PlantAvatar(
                        avatarConfig = previewConfig,
                        health = "healthy",
                        size = 180.dp,
                        animated = true
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))

            Text(
                "Plant Body",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            val baseTypes = listOf(
                "generic" to "Generic",
                "succulent" to "Succulent",
                "cactus" to "Cactus",
                "flower" to "Flower",
                "fern" to "Fern",
                "tree" to "Tree",
                "herb" to "Herb"
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(baseTypes) { (type, label) ->
                    AvatarOptionCard(
                        label = label,
                        isSelected = selectedBaseType == type,
                        onClick = { selectedBaseType = type }
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))

            Text(
                "Color",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            val colors = listOf(
                "green" to "Green",
                "dark_green" to "Dark Green",
                "light_green" to "Light Green",
                "blue" to "Blue",
                "purple" to "Purple",
                "pink" to "Pink",
                "red" to "Red",
                "orange" to "Orange",
                "yellow" to "Yellow",
                "brown" to "Brown"
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(colors) { (color, label) ->
                    AvatarOptionCard(
                        label = label,
                        isSelected = selectedColor == color,
                        onClick = { selectedColor = color }
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    val randomConfig = AvatarGenerator.generateRandomAvatar()
                    selectedBaseType = randomConfig.baseType
                    selectedColor = randomConfig.color
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Randomize")
            }
            
            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val autoConfig = if (plant != null) {
                        AvatarGenerator.generateAvatarForPlant(
                            family = plant.careInfo.family,
                            genus = plant.careInfo.genus,
                            commonName = plant.commonName,
                            scientificName = plant.scientificName
                        )
                    } else {
                        AvatarGenerator.generateRandomAvatar()
                    }
                    selectedBaseType = autoConfig.baseType
                    selectedColor = autoConfig.color
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Auto-Generate from Plant Type")
            }
        }
    }
}

@Composable
private fun AvatarOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
