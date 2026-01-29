package com.example.plantpal.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.plantpal.data.ActivityEvent
import com.example.plantpal.systems.social.ActivityRepository
import com.example.plantpal.systems.social.FriendRepository
import com.example.plantpal.ui.theme.LocalUIScale
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialDashboardScreen(
    onBack: () -> Unit
) {
    val scaled = LocalUIScale.current
    val activityRepo = remember { ActivityRepository() }
    val friendRepo = remember { FriendRepository() }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activities by remember { mutableStateOf<List<ActivityEvent>>(emptyList()) }

    fun refresh() {
        scope.launch {
            loading = true
            error = null

            try {
                friendRepo.syncAcceptedOutgoingToFriends()

                val friendUids = friendRepo.getMyFriendUids().getOrElse { emptyList() }

                val results = buildList {
                    add(async { activityRepo.getMyRecent(25).getOrElse { emptyList() } })
                    friendUids.forEach { uid ->
                        add(async { activityRepo.getRecentForUser(uid, 10).getOrElse { emptyList() } })
                    }
                }.awaitAll()

                activities = results
                    .flatten()
                    .sortedByDescending { it.createdAt }
                    .take(50)
            } catch (e: Exception) {
                error = e.message ?: "Failed to load activity"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Social Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = scaled.titleLarge
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(scaled.iconSizeMedium)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error != null -> Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(scaled.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(scaled.spacingMedium)
            ) {
                Text(
                    error!!,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = scaled.bodyLarge
                    ),
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { refresh() },
                    modifier = Modifier.height(scaled.buttonHeight)
                ) {
                    Text("Retry", fontSize = scaled.labelLarge)
                }
            }

            activities.isEmpty() -> Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(scaled.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(scaled.spacingMedium)
            ) {
                Text(
                    "No recent activity yet.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = scaled.bodyLarge
                    )
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(scaled.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(scaled.spacingSmall)
            ) {
                items(activities) { activity ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(scaled.paddingMedium)) {
                            val name = activity.actorName.ifBlank { "Unknown" }
                            val ts = formatRelativeTime(activity.createdAt)

                            Text(
                                if (ts.isBlank()) name else "$name • $ts",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = scaled.bodySmall
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(scaled.spacingXSmall))

                            Text(
                                activity.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = scaled.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatRelativeTime(ts: Long): String {
    if (ts <= 0L) return ""
    val diff = System.currentTimeMillis() - ts
    val minutes = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}