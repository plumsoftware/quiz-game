package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.ALL_TOPICS
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.LevelMap
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors

/** Экран выбора темы и сложности (ТЗ §5.4). */
@Composable
fun TopicsScreen(
    gameState: GameState,
    onSelectTopic: (String) -> Unit,
    onSelectDifficulty: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val difficulty = GameDifficulty.fromId(gameState.currentDifficulty)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Шапка с кнопкой назад и заголовком (на всю ширину сетки).
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KidsBackButton(onClick = onBack)
                Text("Темы", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp, color = Kids.TextPrimary)
            }
        }
        // Переключатель сложности.
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Сложность", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = Kids.TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(Kids.SegmentTrack).padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameDifficulty.entries.forEach { d ->
                        val selected = d.id == difficulty.id
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                .background(if (selected) Kids.Card else Color.Transparent)
                                .clickable { onSelectDifficulty(d.id) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(d.label, fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selected) Kids.Primary else Kids.TextSecondary)
                        }
                    }
                }
                Text(difficultyHint(difficulty), fontFamily = RubikFamily, fontSize = 12.sp,
                    color = Kids.TextSecondary)
            }
        }
        // Сетка тем.
        itemsIndexed(ALL_TOPICS) { _, topic ->
            val tc = topicColors(topic.id)
            val selected = topic.id == gameState.currentTopicId
            val done = LevelMap.countPassed(topic.id, difficulty.id, gameState.levelStars)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(tc.cardBg)
                    .border(3.dp, if (selected) tc.primary else Color.Transparent, RoundedCornerShape(22.dp))
                    .clickable { onSelectTopic(topic.id) }
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {
                    Text(topic.emoji, fontSize = 38.sp)
                    Text("$done/${LevelMap.LEVELS_PER_TOPIC}", fontFamily = RubikFamily,
                        fontWeight = FontWeight.Bold, fontSize = 11.sp, color = tc.shadow)
                }
                Text(topic.name, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = Kids.TextPrimary)
                KidsProgressBar(
                    progress = done.toFloat() / LevelMap.LEVELS_PER_TOPIC,
                    modifier = Modifier.fillMaxWidth(),
                    height = 8.dp,
                    track = Color.White.copy(alpha = 0.8f),
                    fill = tc.primary
                )
            }
        }
    }
}

private fun difficultyHint(d: GameDifficulty): String = when (d) {
    GameDifficulty.EASY -> "Легко: 20 с на вопрос, 3 жизни, для 5–7 лет."
    GameDifficulty.MEDIUM -> "Средне: 15 с, 3 жизни, для 8–10 лет."
    GameDifficulty.HARD -> "Сложно: 10 с, 2 жизни, для 11+ лет."
}
