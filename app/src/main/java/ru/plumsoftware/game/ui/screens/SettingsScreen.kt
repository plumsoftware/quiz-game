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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.GameSettings
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsSwitch
import ru.plumsoftware.game.ui.components.kids.ParentalGate
import ru.plumsoftware.game.ui.components.kids.UiIcon
import ru.plumsoftware.game.ui.components.kids.UiIconView
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/**
 * Настройки (ТЗ §5.11). Все значения сохраняются.
 * Раздел «Для родителей» и внешние ссылки — только после родительского барьера (§9.1).
 */
@Composable
fun SettingsScreen(
    settings: GameSettings,
    appVersion: String,
    onChange: ((GameSettings) -> GameSettings) -> Unit,
    onRemindersEnabled: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onRateApp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var parentUnlocked by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    /** Выполнить действие за родительским барьером. */
    fun gated(action: () -> Unit) {
        if (parentUnlocked) action() else pendingAction = action
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KidsBackButton(onClick = onBack)
                Text("Настройки", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Kids.TextPrimary)
            }

            SettingsGroup("Звук") {
                SwitchRow("set_sound", "🔊", Color(0xFFFF9800), "Звуки", settings.sound) { v -> onChange { it.copy(sound = v) } }
                SwitchRow("set_music", "🎵", Color(0xFF9C27B0), "Музыка", settings.music) { v -> onChange { it.copy(music = v) } }
                SwitchRow("set_vibro", "📳", Color(0xFF2196F3), "Вибрация", settings.vibro) { v -> onChange { it.copy(vibro = v) } }
                SwitchRow("set_voice", "🗣️", Color(0xFF00BCD4), "Озвучка вопросов", settings.voice) { v -> onChange { it.copy(voice = v) } }
            }

            SettingsGroup("Игра") {
                ValueRow("set_language", "🌐", Color(0xFF3F51B5), "Язык", "Русский")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconLabel("set_difficulty", "🎚️", Kids.Primary, "Сложность по умолчанию")
                    Segmented(
                        options = GameDifficulty.entries.map { it.id to it.label },
                        selected = settings.defaultDifficulty,
                        onSelect = { d -> onChange { it.copy(defaultDifficulty = d) } }
                    )
                }
                SwitchRow("set_notifications", "🔔", Color(0xFFE91E63), "Напоминания", settings.notifications) { v ->
                    onChange { it.copy(notifications = v) }
                    if (v) onRemindersEnabled()
                }
            }

            SettingsGroup("Для родителей") {
                SwitchRow("set_parent", "🛡️", Color(0xFF607D8B), "Родительский контроль", settings.parentControl) { v ->
                    gated { onChange { it.copy(parentControl = v) } }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconLabel("set_time_limit", "⏳", Color(0xFFFF5722), "Лимит в день")
                    Segmented(
                        options = listOf(0 to "Без лимита", 15 to "15 мин", 30 to "30 мин", 60 to "60 мин"),
                        selected = settings.dailyLimitMin,
                        onSelect = { m -> gated { onChange { it.copy(dailyLimitMin = m, parentControl = m > 0 || it.parentControl) } } }
                    )
                }
                LinkRow("Политика конфиденциальности") { gated(onOpenPrivacy) }
                LinkRow("Оценить приложение") { gated(onRateApp) }
                if (!parentUnlocked) {
                    Text(
                        "🔒 Изменения здесь доступны только взрослым", fontFamily = RubikFamily,
                        fontSize = 12.sp, color = Kids.TextMuted
                    )
                }
            }

            Text(
                "Версия $appVersion", fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextMuted,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        }

        pendingAction?.let { action ->
            ParentalGate(
                onPass = {
                    parentUnlocked = true
                    pendingAction = null
                    action()
                },
                onDismiss = { pendingAction = null }
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Kids.TextPrimary)
        KidsCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { content() }
        }
    }
}

/** Иконка в цветном квадрате 36×36 (§5.11). */
@Composable
private fun IconSquare(iconKey: String, emoji: String, color: Color) {
    Box(
        Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) { GameIcon(iconKey, emoji, 26.dp) }
}

@Composable
private fun IconLabel(iconKey: String, emoji: String, color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconSquare(iconKey, emoji, color)
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Kids.TextPrimary)
    }
}

@Composable
private fun SwitchRow(
    iconKey: String, emoji: String, color: Color, label: String, checked: Boolean, onChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconSquare(iconKey, emoji, color)
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Kids.TextPrimary, modifier = Modifier.weight(1f))
        KidsSwitch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ValueRow(iconKey: String, emoji: String, color: Color, label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconSquare(iconKey, emoji, color)
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Kids.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Kids.TextSecondary)
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Kids.Primary, modifier = Modifier.weight(1f))
        UiIconView(UiIcon.FORWARD, tint = Kids.Primary, size = 18.dp)
    }
}

@Composable
private fun Segmented(options: List<Pair<Int, String>>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Kids.SegmentTrack).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Kids.Card else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    color = if (isSelected) Kids.Primary else Kids.TextSecondary, textAlign = TextAlign.Center, maxLines = 1
                )
            }
        }
    }
}
