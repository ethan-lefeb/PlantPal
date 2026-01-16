package com.example.plantpal.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plantpal.data.ActivityEvent
import com.example.plantpal.systems.social.ActivityRepository
import com.example.plantpal.systems.social.FriendRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialDashboardScreen(
    onBack: () -> Unit
) {
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
                // Key: ensures the sender side of "accepted" requests becomes a friend doc,
                // making Option B's exists(...) rule pass for both directions.
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
                title = { Text("Social Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            ) { CircularProgressIndicator() }

            error != null -> Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { refresh() }) { Text("Retry") }
            }

            activities.isEmpty() -> Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("No recent activity yet.")
            }

            else -> LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { activity ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            val name = activity.actorName.ifBlank { "Unknown" }
                            val ts = formatRelativeTime(activity.createdAt)

                            Text(
                                if (ts.isBlank()) name else "$name • $ts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(activity.text, style = MaterialTheme.typography.bodyMedium)
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