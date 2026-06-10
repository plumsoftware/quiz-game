package ru.plumsoftware.game.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import ru.plumsoftware.game.App
import ru.plumsoftware.game.MainActivity
import ru.plumsoftware.game.R
import ru.plumsoftware.game.ads.AdsBase
import ru.plumsoftware.game.ui.components.game.GameAdOverlays
import ru.plumsoftware.game.ui.components.game.GameScreenTopBar
import ru.plumsoftware.game.ui.components.game.rememberAdsManager
import ru.plumsoftware.game.ui.components.game.watchRewardedForCoins
import ru.plumsoftware.game.ui.theme.*

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
    val adsManager = rememberAdsManager(activity)
    val displayAds by remember { mutableStateOf(Firebase.remoteConfig.getBoolean("display_ads")) }
    var rewardCoins by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        GameAdOverlays(
            isAdLoading = adsManager.isAdLoading,
            rewardCoins = rewardCoins,
            onDismissReward = { rewardCoins = null }
        )

        Column(modifier = Modifier.fillMaxSize()) {
        GameScreenTopBar(
            title = stringResource(R.string.settings),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (displayAds || App.adsBase != AdsBase.AdsGooglePlay()) {
                GameSettingsCard(title = "🎁 Награда за рекламу") {
                    Text(
                        stringResource(R.string.earn_extra_coins),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GameTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            adsManager.watchRewardedForCoins(
                                onCoinsGranted = addCoins,
                                onRewardDialog = { rewardCoins = it }
                            )
                        },
                        enabled = !adsManager.isAdLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GamePurple),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.watch_ad))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.MonetizationOn, null, tint = GameGold)
                        Text(" +50", color = GameGold)
                    }
                }
            }

            GameSettingsCard(title = "🔔 Уведомления") {
                SettingsToggleRow(
                    label = stringResource(R.string.daily_reminders),
                    checked = dailyRemindersEnabled,
                    onCheckedChange = {
                        dailyRemindersEnabled = it
                        if (it) notificationScheduler?.scheduleDailyNotification()
                        else notificationScheduler?.cancelAllNotifications()
                    }
                )
                HorizontalDivider(color = GameBorder, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleRow(
                    label = stringResource(R.string.quiz_reminders),
                    checked = quizRemindersEnabled,
                    onCheckedChange = {
                        quizRemindersEnabled = it
                        if (it) notificationScheduler?.scheduleQuizReminder()
                        else notificationScheduler?.cancelAllNotifications()
                    }
                )
            }

            GameSettingsCard(title = "🎮 Игровой опыт") {
                SettingsToggleRow(
                    label = stringResource(R.string.sound_enabled),
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it },
                    icon = Icons.Default.VolumeUp
                )
                HorizontalDivider(color = GameBorder, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleRow(
                    label = stringResource(R.string.vibration_enabled),
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}

@Composable
private fun GameSettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GameSurface,
        border = BorderStroke(1.dp, GameBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = GameTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(icon, null, tint = GameTextMuted, modifier = Modifier.size(20.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, color = GameTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GamePurpleLight,
                checkedTrackColor = GamePurple.copy(alpha = 0.4f),
                uncheckedThumbColor = GameTextDisabled,
                uncheckedTrackColor = GameBorder
            )
        )
    }
}
