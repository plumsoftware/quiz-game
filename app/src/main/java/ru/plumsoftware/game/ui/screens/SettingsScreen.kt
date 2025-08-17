package ru.plumsoftware.game.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import ru.plumsoftware.game.App
import ru.plumsoftware.game.MainActivity
import ru.plumsoftware.game.R
import ru.plumsoftware.game.ads.AdsBase
import ru.plumsoftware.game.ads.AdsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    addCoins: (Int) -> Unit,
    onBack: () -> Unit,
    notificationScheduler: ru.plumsoftware.game.notifications.NotificationScheduler? = null
) {
    var dailyRemindersEnabled by remember { mutableStateOf(true) }
    var quizRemindersEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    val activity = LocalActivity.current ?: MainActivity()
    val adsManager = AdsManager(App.adsBase, activity)

    val displayAds by remember { mutableStateOf(Firebase.remoteConfig.getBoolean("display_ads")) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

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
            // Rewarded Ads Section
            if (App.adsBase == AdsBase.AdsGooglePlay()) {
                if (displayAds)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                    ) {
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                onClick = {
                                    adsManager.showRewarded { reward ->
                                        addCoins(reward)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.watch_ad),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.width(10.dp))
                                Row(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .rotate(20f)
                                        .graphicsLayer {
                                            scaleX = pulse
                                            scaleY = pulse
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        space = 4.dp,
                                        alignment = Alignment.CenterHorizontally
                                    )
                                ) {
                                    Text("+50", style = MaterialTheme.typography.titleLarge)
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = "плюс 50 монет",
                                        tint = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }
                    }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            onClick = {
                                adsManager.showRewarded { reward ->
                                    addCoins(reward)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Text(
                                stringResource(R.string.watch_ad),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(10.dp))
                            Row(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .rotate(20f)
                                    .graphicsLayer {
                                        scaleX = pulse
                                        scaleY = pulse
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = 4.dp,
                                    alignment = Alignment.CenterHorizontally
                                )
                            ) {
                                Text("+50", style = MaterialTheme.typography.titleLarge)
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "плюс 50 монет",
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            }

            // Notifications Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onBackground
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
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Switch(
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
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
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
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
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onBackground
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
} 