package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsSwitch
import ru.plumsoftware.game.ui.components.kids.ParentalGate
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/** Настройки (ТЗ §5.11). Раздел «Для родителей» доступен после родительского барьера. */
@Composable
fun SettingsScreen(
    currentDifficulty: Int,
    onSetDifficulty: (Int) -> Unit,
    onBack: () -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
    appVersion: String = "1.3.1",
    modifier: Modifier = Modifier
) {
    var sound by remember { mutableStateOf(true) }
    var music by remember { mutableStateOf(true) }
    var vibro by remember { mutableStateOf(true) }
    var voice by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf(true) }
    var parentalControl by remember { mutableStateOf(false) }
    var dailyLimit by remember { mutableStateOf(0) } // 0=без лимита,15,30,60
    var parentUnlocked by remember { mutableStateOf(false) }
    var showGate by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KidsBackButton(onClick = onBack)
                Text("Настройки", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp, color = Kids.TextPrimary)
            }

            // Звук.
            SettingsGroup("Звук") {
                SwitchRow("🔊", Color(0xFFFF9800), "Звуки", sound) { sound = it }
                SwitchRow("🎵", Color(0xFF9C27B0), "Музыка", music) { music = it }
                SwitchRow("📳", Color(0xFF2196F3), "Вибрация", vibro) { vibro = it }
                SwitchRow("🗣️", Color(0xFF00BCD4), "Озвучка вопросов", voice) { voice = it }
            }

            // Игра.
            SettingsGroup("Игра") {
                InfoRow("🌐", Color(0xFF3F51B5), "Язык", "Русский")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RowIconLabel("🎚️", Color(0xFF6C4DF6), "Сложность по умолчанию")
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Kids.SegmentTrack).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GameDifficulty.entries.forEach { d ->
                            val selected = d.id == currentDifficulty
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Kids.Card else Color.Transparent)
                                    .clickable { onSetDifficulty(d.id) }.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(d.label, fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp, color = if (selected) Kids.Primary else Kids.TextSecondary)
                            }
                        }
                    }
                }
                SwitchRow("🔔", Color(0xFFE91E63), "Напоминания", reminders) {
                    reminders = it; onRemindersChanged(it)
                }
            }

            // Для родителей (за барьером).
            SettingsGroup("Для родителей") {
                if (!parentUnlocked) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(Kids.PrimarySoft).clickable { showGate = true }.padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒 Разблокировать (родительский барьер)", fontFamily = RubikFamily,
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Kids.Primary)
                    }
                } else {
                    SwitchRow("🛡️", Color(0xFF607D8B), "Родительский контроль", parentalControl) {
                        parentalControl = it
                    }
                    RowIconLabel("⏳", Color(0xFFFF5722), "Лимит в день")
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Kids.SegmentTrack).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0 to "Без", 15 to "15", 30 to "30", 60 to "60").forEach { (value, label) ->
                            val selected = value == dailyLimit
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Kids.Card else Color.Transparent)
                                    .clickable { dailyLimit = value }.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp, color = if (selected) Kids.Primary else Kids.TextSecondary)
                            }
                        }
                    }
                }
            }

            Text("Версия $appVersion", fontFamily = RubikFamily, fontSize = 12.sp,
                color = Kids.TextMuted, modifier = Modifier.fillMaxWidth().padding(8.dp))
            Box(Modifier.size(8.dp))
        }

        if (showGate) {
            ParentalGate(
                onPass = { parentUnlocked = true; showGate = false },
                onDismiss = { showGate = false }
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            color = Kids.TextPrimary)
        KidsCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
        }
    }
}

@Composable
private fun RowIconLabel(emoji: String, color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center) { Text(emoji, fontSize = 18.sp) }
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = Kids.TextPrimary)
    }
}

@Composable
private fun SwitchRow(emoji: String, color: Color, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center) { Text(emoji, fontSize = 18.sp) }
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = Kids.TextPrimary, modifier = Modifier.weight(1f))
        KidsSwitch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun InfoRow(emoji: String, color: Color, label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center) { Text(emoji, fontSize = 18.sp) }
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = Kids.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            color = Kids.TextSecondary)
    }
}
