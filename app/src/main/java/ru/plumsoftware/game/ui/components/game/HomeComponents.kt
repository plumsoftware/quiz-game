package ru.plumsoftware.game.ui.components.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.ui.theme.*

data class PlayerStats(
    val quizzesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val minutesPlayed: Int = 0,
    val categoriesPlayed: Int = 0
)

data class HomeNavItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val accentColor: Color
)

@Composable
fun HomeStatsGrid(stats: PlayerStats, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeStatCard("📝", stats.quizzesPlayed.toString(), "Викторин", GamePurple, Modifier.weight(1f))
            HomeStatCard("✅", stats.correctAnswers.toString(), "Правильных", GameCorrect, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeStatCard("⏱️", stats.minutesPlayed.toString(), "Минут", GameGold, Modifier.weight(1f))
            HomeStatCard("🎯", stats.categoriesPlayed.toString(), "Категорий", GameStreak, Modifier.weight(1f))
        }
    }
}

@Composable
fun HomeStatCard(
    emoji: String,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = GameSurface,
        border = BorderStroke(1.dp, GameBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                fontWeight = FontWeight.ExtraBold
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = GameTextMuted)
        }
    }
}

@Composable
fun HomeNavButtons(
    items: List<HomeNavItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            HomeNavButton(item = item, onClick = { onItemClick(index) })
        }
    }
}

@Composable
fun HomeNavButton(item: HomeNavItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = item.accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, item.accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                tint = item.accentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
