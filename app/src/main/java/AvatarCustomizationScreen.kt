package com.example.plantpal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Customize $plantName",
                            color = Color(0xFF2F5233)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF2F5233)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { onSave(previewConfig) }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Save",
                                tint = Color(0xFF2F5233)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFB5E48C)
                    )
                )
            },
            containerColor = Color.Transparent
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
                    color = Color(0xFF2F5233),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Card(
                    modifier = Modifier.size(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        PlantAvatar(
                            avatarConfig = previewConfig,
                            health = "healthy",
                            size = 160.dp,
                            animated = false
                        )
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    "Plant Body",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2F5233),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                
                val plantTypes = listOf(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    items(plantTypes) { (type, label) ->
                        ModernOptionCard(
                            label = label,
                            isSelected = selectedBaseType == type,
                            onClick = { selectedBaseType = type }
                        )
                    }
                }
                
                Text(
                    "Color",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2F5233),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    items(colors) { (color, label) ->
                        ModernOptionCard(
                            label = label,
                            isSelected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val randomConfig = AvatarGenerator.generateRandomAvatar()
                        selectedBaseType = randomConfig.baseType
                        selectedColor = randomConfig.color
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = Color(0xFF2F5233)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Text("Randomize", style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(Modifier.height(12.dp))

                Button(
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF52796F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Text("Auto-Generate from Plant Type", style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ModernOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color(0xFF2F5233),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        color = if (isSelected) {
            Color(0xFF52796F)
        } else {
            Color.White.copy(alpha = 0.7f)
        },
        tonalElevation = if (isSelected) 4.dp else 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.White else Color(0xFF2F5233)
        )
    }
}
