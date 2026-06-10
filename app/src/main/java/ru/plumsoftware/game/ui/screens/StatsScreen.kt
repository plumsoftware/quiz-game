package ru.plumsoftware.game.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.ui.components.AnimatedCounter
import ru.plumsoftware.game.ui.components.game.GameScreenTopBar
import ru.plumsoftware.game.ui.components.game.HomeStatCard
import ru.plumsoftware.game.ui.theme.*
import ru.plumsoftware.game.ui.util.CategoryStyles

@Composable
fun StatsScreen(
    gameState: GameState,
    stats: Map<String, Int>,
    onBack: () -> Unit
) {
    val expInLevel = gameState.experience % 100
    val progressAnim by animateFloatAsState(
        targetValue = expInLevel / 100f,
        animationSpec = tween(1000),
        label = "levelProgress"
    )
    val categoryBests = remember(gameState) {
        gameState.categoriesPlayed.map { category ->
            val style = CategoryStyles.forCategory(category)
            CategoryBest(name = category, emoji = style.emoji)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 14.dp)
    ) {
        GameScreenTopBar(title = "Статистика", onBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LevelProgressCard(
                    level = gameState.level,
                    experience = gameState.experience,
                    expInLevel = expInLevel,
                    progress = progressAnim,
                    quizzesAtNextLevel = GameData.quizzesUnlockingAtPlayerLevel(gameState.level + 1)
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeStatCard(
                            "📝",
                            (stats["quizzes_completed"] ?: gameState.quizzesCompleted).toString(),
                            "Викторин",
                            GamePurple,
                            Modifier.weight(1f)
                        )
                        HomeStatCard(
                            "✅",
                            (stats["correct_answers"] ?: gameState.correctAnswers).toString(),
                            "Правильных",
                            GameCorrect,
                            Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeStatCard(
                            "⏱️",
                            (stats["play_time_minutes"] ?: gameState.playTimeMinutes).toString(),
                            "Минут сыграно",
                            GameGold,
                            Modifier.weight(1f)
                        )
                        HomeStatCard(
                            "🔥",
                            "${gameState.streakDays} дн.",
                            "Макс. серия",
                            GameStreak,
                            Modifier.weight(1f)
                        )
                    }
                }
            }
            if (categoryBests.isNotEmpty()) {
                item {
                    Text(
                        "Лучшие результаты",
                        style = MaterialTheme.typography.titleMedium,
                        color = GameTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(categoryBests) { best ->
                    CategoryBestRow(best = best)
                }
            }
        }
    }
}

@Composable
private fun LevelProgressCard(
    level: Int,
    experience: Int,
    expInLevel: Int,
    progress: Float,
    quizzesAtNextLevel: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GameSurface),
        border = BorderStroke(1.dp, GameBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedCounter(
                target = level,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (quizzesAtNextLevel > 0) {
                    "На уровне ${level + 1}: +${GameData.quizzesUnlockingLabel(quizzesAtNextLevel)}"
                } else {
                    "На следующем уровне новых викторин нет"
                },
                style = MaterialTheme.typography.bodySmall,
                color = GameTextMuted
            )
            Text(
                text = "$experience ОП",
                style = MaterialTheme.typography.bodyMedium,
                color = GameTextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("До уровня ${level + 1}", style = MaterialTheme.typography.bodySmall, color = GameTextMuted)
                Text("$expInLevel/100", style = MaterialTheme.typography.bodySmall, color = GameTextMuted)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GamePurple,
                trackColor = GameBorder
            )
        }
    }
}

private data class CategoryBest(val name: String, val emoji: String)

@Composable
private fun CategoryBestRow(best: CategoryBest) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GameSurface,
        border = BorderStroke(1.dp, GameBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${best.emoji} ${best.name}", style = MaterialTheme.typography.bodyMedium, color = GameTextPrimary)
            Text("Играл", style = MaterialTheme.typography.labelSmall, color = GameCorrectText)
        }
    }
}
