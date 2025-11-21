@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.plantpal.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.example.plantpal.CareReminderWorker
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    workManager: WorkManager,
    onBack: () -> Unit
) {
    var remindersEnabled by remember { mutableStateOf(true) }
    var frequencyHours by remember { mutableStateOf(12) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Care Reminder Preferences", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Reminders")
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

            Text("Reminder Frequency: $frequencyHours hours")
            Slider(
                value = frequencyHours.toFloat(),
                onValueChange = { frequencyHours = it.toInt().coerceIn(6, 48) },
                valueRange = 6f..48f,
                steps = 6
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scheduleCareWorker(workManager, frequencyHours)
                }
            ) {
                Text("Apply Reminder Settings")
            }
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
        ExistingPeriodicWorkPolicy.REPLACE, // FIXED
        request
    )
}
