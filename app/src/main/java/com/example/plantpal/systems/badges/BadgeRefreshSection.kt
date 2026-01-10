package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun BadgeRefreshSection() {
    var isRefreshing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<BadgePreview?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val result = BadgeProgressFixer.previewBadgeChanges()
            if (result.isSuccess) {
                preview = result.getOrNull()
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Badge Progress Fix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                "If you added plants before but badges didn't unlock, use this to refresh your badge progress.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (preview != null) {
                Divider()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Current Status:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Plants: ${preview!!.progressPlantCount} (actual: ${preview!!.currentPlantCount})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Badges: ${preview!!.currentBadgeCount} unlocked (should have: ${preview!!.shouldHaveBadgeCount})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (preview!!.missingBadges.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "🎯 ${preview!!.missingBadges.size} badge(s) ready to unlock:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        preview!!.missingBadges.forEach { badge ->
                            Text(
                                "  ${badge.icon} ${badge.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "✅ All badges up to date!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Refreshing...")
                } else {
                    Text("Refresh Badge Progress")
                }
            }

            if (resultMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        resultMessage!!,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Refresh Badge Progress?") },
            text = {
                Text(
                    "This will update your progress to match your actual plants and unlock any badges you've earned.\n\n" +
                    "This is safe and won't remove any badges you've already unlocked."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        scope.launch {
                            isRefreshing = true
                            resultMessage = null
                            
                            val result = BadgeProgressFixer.refreshBadgeProgress()
                            
                            if (result.isSuccess) {
                                resultMessage = result.getOrNull()
                                val newPreview = BadgeProgressFixer.previewBadgeChanges()
                                if (newPreview.isSuccess) {
                                    preview = newPreview.getOrNull()
                                }
                            } else {
                                resultMessage = "❌ ${result.exceptionOrNull()?.message}"
                            }
                            
                            isRefreshing = false
                        }
                    }
                ) {
                    Text("Refresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}