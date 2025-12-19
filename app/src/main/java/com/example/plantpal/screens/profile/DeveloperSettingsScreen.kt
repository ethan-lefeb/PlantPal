package com.example.plantpal.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.plantpal.PlantRepository
import com.example.plantpal.AvatarDebugUtils
import com.example.plantpal.CareReminderWorker
import com.example.plantpal.BadgeRefreshSection
import com.example.plantpal.ui.theme.ForestGradientBalanced


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettingsScreen(
    workManager: WorkManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { PlantRepository() }

    var isRegenerating by remember { mutableStateOf(false) }
    var regenerationResult by remember { mutableStateOf<String?>(null) }
    var forceRegenerate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(ForestGradientBalanced)
                )
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Avatar Tools",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Regenerate Avatars",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "This will regenerate avatars for plants based on their family and genus. By default, only plants with missing or generic avatars are updated.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = forceRegenerate,
                                onCheckedChange = { forceRegenerate = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Force regenerate ALL plants",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "This will overwrite customized avatars",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isRegenerating = true
                                    regenerationResult = null

                                    val result = AvatarDebugUtils.regenerateAllAvatars(repository, forceRegenerate)

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
            }

            item {
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
                            • Plant genus (most specific)
                            • Plant family (broader category)
                            • Common and scientific names
                            • Plant characteristics
                            
                            Avatar types include:
                            Cacti, Succulents, Ferns, Pothos, Philodendron, 
                            Monstera, Snake Plants, Palms, Orchids, and more!
                            
                            Colors match plant characteristics.
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
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
                            If avatars aren't displaying correctly:
                            
                            1. Try regenerating without force first
                            2. Check that plants have family/genus data
                            3. Use force regenerate if needed
                            4. Manually customize via plant detail screen
                            
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

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Badge Tools",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                BadgeRefreshSection()
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Dev Tools",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val request = OneTimeWorkRequestBuilder<CareReminderWorker>().build()
                        workManager.enqueue(request)
                    }
                ) {
                    Text("Send Test Reminder NOW")
                }
            }
        }
    }
}