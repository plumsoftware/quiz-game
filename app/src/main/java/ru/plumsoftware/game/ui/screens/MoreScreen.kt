package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.theme.*

private data class MoreItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val screen: GameScreen
)

@Composable
fun MoreScreen(onNavigate: (GameScreen) -> Unit) {
    val items = listOf(
        MoreItem("✅", "Ежедневные задания", "Выполняй и зарабатывай монеты", GameScreen.DAILY_TASKS),
        MoreItem("🏆", "Достижения", "Открывай новые значки", GameScreen.ACHIEVEMENTS),
        MoreItem("🛒", "Магазин", "Подсказки, жизни и бонусы", GameScreen.SHOP),
        MoreItem("📊", "Статистика", "Твои рекорды и прогресс", GameScreen.STATS),
        MoreItem("⚙️", "Настройки", "Звук, уведомления, реклама", GameScreen.SETTINGS),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Меню",
            style = MaterialTheme.typography.headlineMedium,
            color = GameTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        items.forEach { item ->
            Surface(
                onClick = { onNavigate(item.screen) },
                shape = RoundedCornerShape(14.dp),
                color = GameSurface,
                border = BorderStroke(1.dp, GameBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(item.emoji, style = MaterialTheme.typography.titleLarge)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = GameTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = GameTextMuted)
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = GameTextDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
