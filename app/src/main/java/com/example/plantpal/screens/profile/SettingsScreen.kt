@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.plantpal.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Notifications
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.example.plantpal.CareReminderWorker
import com.example.plantpal.ui.theme.*
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    workManager: WorkManager,
    onBack: () -> Unit
) {
    var remindersEnabled by remember { mutableStateOf(true) }
    var frequencyHours by remember { mutableStateOf(12) }

    val currentScaledSizes = LocalUIScale.current
    val scaleUpdater = LocalScaleUpdater.current
    val currentUIScale = remember(currentScaledSizes.scaleFactor) {
        UIScale.fromScaleFactor(currentScaledSizes.scaleFactor)
    }
    var selectedScale by remember { mutableStateOf(currentUIScale) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        fontSize = currentScaledSizes.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(currentScaledSizes.iconSizeMedium)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(currentScaledSizes.paddingLarge)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(currentScaledSizes.spacingLarge)
        ) {

            SettingsSection(
                title = "Display & Accessibility",
                icon = Icons.Default.TextFields
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(currentScaledSizes.spacingSmall)
                ) {
                    Text(
                        "Text & UI Size",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = currentScaledSizes.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    Text(
                        "Adjust the size of text and UI elements throughout the app",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = currentScaledSizes.bodyMedium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(currentScaledSizes.spacingSmall))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(currentScaledSizes.spacingSmall)
                    ) {
                        UIScale.entries.forEach { scale ->
                            FilterChip(
                                selected = selectedScale == scale,
                                onClick = {
                                    selectedScale = scale
                                    scaleUpdater(scale.scaleFactor)
                                },
                                label = { 
                                    Text(
                                        scale.label,
                                        fontSize = currentScaledSizes.labelMedium
                                    ) 
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = currentScaledSizes.spacingMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(currentScaledSizes.paddingMedium)
                        ) {
                            Text(
                                "Preview",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = currentScaledSizes.labelSmall
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(currentScaledSizes.spacingXSmall))
                            Text(
                                "This is how text will appear",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = currentScaledSizes.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                "Body text example with current scale: ${selectedScale.label}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = currentScaledSizes.bodyMedium
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(currentScaledSizes.spacingMedium))

            SettingsSection(
                title = "Care Reminder Preferences",
                icon = Icons.Default.Notifications
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Enable Reminders",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = currentScaledSizes.titleMedium
                            )
                        )
                        Text(
                            "Get notifications for plant care",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = currentScaledSizes.bodySmall
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = {
                            remindersEnabled = it
                            if (it) {
                                scheduleCareWorker(workManager, frequencyHours)
                            } else {
                                workManager.cancelUniqueWork("CareReminderWork")
                            }
                        }
                    )
                }
                
                Spacer(Modifier.height(currentScaledSizes.spacingMedium))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Reminder Frequency",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = currentScaledSizes.titleMedium
                        )
                    )
                    Text(
                        "$frequencyHours hours between reminders",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = currentScaledSizes.bodyMedium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(Modifier.height(currentScaledSizes.spacingSmall))
                    
                    Slider(
                        value = frequencyHours.toFloat(),
                        onValueChange = { frequencyHours = it.toInt().coerceIn(6, 48) },
                        valueRange = 6f..48f,
                        steps = 6,
                        enabled = remindersEnabled
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "6h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = currentScaledSizes.labelSmall
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "48h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = currentScaledSizes.labelSmall
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(Modifier.height(currentScaledSizes.spacingMedium))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentScaledSizes.buttonHeight),
                    onClick = {
                        scheduleCareWorker(workManager, frequencyHours)
                    },
                    enabled = remindersEnabled
                ) {
                    Text(
                        "Apply Reminder Settings",
                        fontSize = currentScaledSizes.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val scaledSizes = LocalUIScale.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(scaledSizes.paddingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = scaledSizes.spacingMedium)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(scaledSizes.iconSizeMedium)
                        .padding(end = scaledSizes.paddingSmall),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = scaledSizes.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Divider(modifier = Modifier.padding(bottom = scaledSizes.spacingMedium))

            content()
        }
    }
}

fun scheduleCareWorker(workManager: WorkManager, frequencyHours: Int) {
    val request = PeriodicWorkRequestBuilder<CareReminderWorker>(
        frequencyHours.toLong(),
        TimeUnit.HOURS
    ).build()

    workManager.enqueueUniquePeriodicWork(
        "CareReminderWork",
        ExistingPeriodicWorkPolicy.REPLACE,
        request
    )
}
