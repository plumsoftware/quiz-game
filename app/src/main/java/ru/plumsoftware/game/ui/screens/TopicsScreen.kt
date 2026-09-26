package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.ALL_TOPICS
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.LevelMap
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.components.kids.desaturate
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors

/** Экран выбора темы и сложности (ТЗ §5.4). Прогресс хранится отдельно для каждой сложности. */
@Composable
fun TopicsScreen(
    gameState: GameState,
    playableTopics: Set<String>,
    onSelectTopic: (String) -> Unit,
    onSelectDifficulty: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val difficulty = GameDifficulty.fromId(gameState.currentDifficulty)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                "Темы", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp, color = Kids.TextPrimary
            )
        }
        // Переключатель сложности из 3 сегментов + подсказка.
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(Kids.SegmentTrack).padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameDifficulty.entries.forEach { d ->
                        val selected = d.id == difficulty.id
                        Row(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                .background(if (selected) Kids.Card else Color.Transparent)
                                .clickable { onSelectDifficulty(d.id) }
                                .padding(vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GameIcon(difficultyIcon(d), "", 20.dp)
                            Text(
                                " ${d.label}", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                                fontSize = 13.sp, color = if (selected) Kids.Primary else Kids.TextSecondary
                            )
                        }
                    }
                }
                Text(
                    difficultyHint(difficulty), fontFamily = RubikFamily, fontWeight = FontWeight.Medium,
                    fontSize = 12.sp, color = Kids.TextSecondary
                )
            }
        }
        // Сетка тем 2 колонки.
        items(ALL_TOPICS, key = { it.id }) { topic ->
            val tc = topicColors(topic.id)
            val available = topic.id in playableTopics
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
                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    GameIcon(
                        "topic_${topic.id}", topic.emoji, 46.dp,
                        colorFilter = if (available) null else desaturate(0.8f),
                        modifier = Modifier.alpha(if (available) 1f else 0.6f)
                    )
                    if (available) {
                        Text(
                            "$done/${LevelMap.LEVELS_PER_TOPIC}", fontFamily = RubikFamily,
                            fontWeight = FontWeight.Bold, fontSize = 11.sp, color = tc.shadow
                        )
                    } else {
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.8f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Скоро", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Kids.TextMuted)
                        }
                    }
                }
                Text(
                    topic.name, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = if (available) Kids.TextPrimary else Kids.TextMuted
                )
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

private fun difficultyIcon(d: GameDifficulty): String = when (d) {
    GameDifficulty.EASY -> "difficulty_easy"
    GameDifficulty.MEDIUM -> "difficulty_medium"
    GameDifficulty.HARD -> "difficulty_hard"
}

private fun difficultyHint(d: GameDifficulty): String = when (d) {
    GameDifficulty.EASY -> "Легко: 20 с на вопрос, 3 жизни, для 5–7 лет."
    GameDifficulty.MEDIUM -> "Средне: 15 с, 3 жизни, для 8–10 лет."
    GameDifficulty.HARD -> "Сложно: 10 с, 2 жизни, для 11+ лет."
}
