package com.example.plantpal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun DashboardScreen(
    onOpenLibrary: () -> Unit,
    onAddPlant: () -> Unit,
    onOpenPlant: (String) -> Unit
) {
    val repo = remember { PlantRepository() }
    var plants by remember { mutableStateOf<List<PlantProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            repo.getAllPlants()
                .onSuccess { plants = it; error = null }
                .onFailure { error = it.message }
            isLoading = false
        }
    }

    fun daysSince(ts: Long): Int {
        if (ts <= 0L) return Int.MAX_VALUE
        val days = (System.currentTimeMillis() - ts) / (1000L * 60 * 60 * 24)
        return max(0, days.toInt())
    }

    val total = plants.size
    val dueWater = plants.count { daysSince(it.lastWatered) >= it.wateringFrequency }
    val dueFert  = plants.count { daysSince(it.lastFertilized) >= it.fertilizerFrequency }
    val dueRotate = plants.count { p ->
        val last = p.careProfile.lastRotated
        val every = p.careProfile.rotationFrequency
        daysSince(last) >= every
    }

    val upcomingWater = plants
        .sortedBy { (it.wateringFrequency - daysSince(it.lastWatered)).coerceAtLeast(0) }
        .take(5)

    val upcomingRotate = plants
        .sortedBy { (it.careProfile.rotationFrequency - daysSince(it.careProfile.lastRotated)).coerceAtLeast(0) }
        .take(5)


    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Couldn’t load your plants.", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    scope.launch {
                        isLoading = true
                        repo.getAllPlants()
                            .onSuccess { plants = it; error = null }
                            .onFailure { error = it.message }
                        isLoading = false
                    }
                }) { Text("Retry") }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Total plants", total.toString(), modifier = Modifier.weight(1f))
                        StatCard("Water due",   dueWater.toString(), modifier = Modifier.weight(1f))
                        StatCard("Fertilize due", dueFert.toString(), modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Rotate due", dueRotate.toString(), modifier = Modifier.weight(1f))
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.weight(1f))
                    }
                }

                if (total == 0) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Let’s add your first plant 🌱")
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = onAddPlant) { Text("Add a plant") }
                            }
                        }
                    }
                }

                else {
                    item {
                        SectionHeader(
                            title = "Upcoming water",
                            action = "Go to Library",
                            onAction = onOpenLibrary
                        )
                    }

                    items(upcomingWater) { plant ->
                        val days = daysSince(plant.lastWatered)
                        val dueIn = (plant.wateringFrequency - days).coerceAtLeast(0)

                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            headlineContent = {
                                Text(plant.commonName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Text(
                                    if (dueIn == 0) "Water today"
                                    else "Water in $dueIn day${if (dueIn == 1) "" else "s"}"
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { onOpenPlant(plant.plantId) }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Open")
                                }
                            }
                        )
                        Divider()
                    }

                    item { SectionHeader(title = "Upcoming rotation") }

                    items(upcomingRotate) { plant ->
                        val remain = (plant.careProfile.rotationFrequency -
                                daysSince(plant.careProfile.lastRotated))
                            .coerceAtLeast(0)

                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            headlineContent = {
                                Text(plant.commonName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Text(
                                    if (remain == 0) "Rotate today"
                                    else "Rotate in $remain day${if (remain == 1) "" else "s"}"
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { onOpenPlant(plant.plantId) }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Open")
                                }
                            }
                        )
                        Divider()
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = onOpenLibrary
                            ) { Text("Open Library") }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = onAddPlant
                            ) { Text("Add Plant") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}
