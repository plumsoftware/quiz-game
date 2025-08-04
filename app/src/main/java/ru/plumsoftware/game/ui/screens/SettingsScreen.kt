package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    notificationScheduler: ru.plumsoftware.game.notifications.NotificationScheduler? = null
) {
    var dailyRemindersEnabled by remember { mutableStateOf(true) }
    var quizRemindersEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notifications Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Switch(
                        checked = dailyRemindersEnabled,
                        onCheckedChange = {
                            dailyRemindersEnabled = it
                            if (it) {
                                notificationScheduler?.scheduleDailyNotification()
                            } else {
                                notificationScheduler?.cancelAllNotifications()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.daily_reminders),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Switch(
                        checked = quizRemindersEnabled,
                        onCheckedChange = {
                            quizRemindersEnabled = it
                            if (it) {
                                notificationScheduler?.scheduleQuizReminder()
                            } else {
                                notificationScheduler?.cancelAllNotifications()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.quiz_reminders),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Sound & Vibration Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.sound_enabled),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.vibration_enabled),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Rewarded Ads Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.watch_ad_for_rewards),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = stringResource(R.string.earn_extra_coins),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = { /* Handle ad watching */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.watch_ad))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
} 