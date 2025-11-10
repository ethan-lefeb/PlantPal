package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettingsScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { PlantRepository() }
    
    var isRegenerating by remember { mutableStateOf(false) }
    var regenerationResult by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Settings") },
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
                .padding(16.dp)
        ) {
            Text(
                "Avatar Tools",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Regenerate All Avatars",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "This will regenerate avatars for all plants based on their plant family and characteristics. Use this if avatars aren't displaying correctly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isRegenerating = true
                                regenerationResult = null
                                
                                val result = AvatarDebugUtils.regenerateAllAvatars(repository)
                                
                                isRegenerating = false
                                regenerationResult = if (result.isSuccess) {
                                    "✅ Successfully updated ${result.getOrNull()} plants!"
                                } else {
                                    "❌ Error: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        enabled = !isRegenerating,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRegenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isRegenerating) "Regenerating..." else "Regenerate Avatars")
                    }
                    
                    regenerationResult?.let { result ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (result.startsWith("✅")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "About Avatars",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        """
                        Avatars are automatically generated based on:
                        • Plant family (e.g., Cactaceae → cactus avatar)
                        • Common and scientific names
                        • Plant characteristics
                        
                        Avatar types:
                        Cactus, Succulent, Flower
                        Fern, Tree, Herb, Generic
                        
                        Colors match plant characteristics
                        Accessories appear based on health and care
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Troubleshooting",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        """
                        If all avatars look the same:
                        
                        1. Tap "Regenerate Avatars" above
                        2. Check that plants have family data
                        3. Try customizing avatars manually
                        
                        If avatars don't save:
                        • Check Firestore permissions
                        • Verify network connection
                        • Check logs for errors
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
