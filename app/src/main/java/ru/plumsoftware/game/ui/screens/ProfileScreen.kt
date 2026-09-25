package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.LevelMap
import ru.plumsoftware.game.data.avatarById
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors

/** Звания игрока (ТЗ §5.8). */
private fun rankFor(level: Int): String = when {
    level >= 15 -> "Профессор"
    level >= 10 -> "Мудрец"
    level >= 6 -> "Знаток"
    level >= 3 -> "Ученик"
    else -> "Новичок"
}

/** Профиль и статистика (ТЗ §5.8). */
@Composable
fun ProfileScreen(
    gameState: GameState,
    achievements: List<Achievement>,
    onOpenSettings: () -> Unit,
    onOpenAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatar = avatarById(gameState.avatarId)
    val expInLevel = gameState.experience % 100
    val accuracy = if (gameState.totalAnswers > 0)
        gameState.correctAnswers * 100 / gameState.totalAnswers else 0
    val starsTotal = gameState.levelStars.values.sum()
    val unlocked = achievements.count { it.current >= it.target }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Профиль", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp, color = Kids.TextPrimary)
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Kids.Card)
                    .clickable(onClick = onOpenSettings),
                contentAlignment = Alignment.Center
            ) { Text("⚙️", fontSize = 20.sp) }
        }

        // Фиолетовая карточка профиля.
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Kids.Primary).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(84.dp).clip(CircleShape).background(Kids.Avatar),
                contentAlignment = Alignment.Center
            ) { Text(avatar.emoji, fontSize = 46.sp) }
            Text(gameState.playerName, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp, color = Color.White)
            Text("Уровень ${gameState.level} · ${rankFor(gameState.level)}", fontFamily = RubikFamily,
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
            KidsProgressBar(expInLevel / 100f, Modifier.fillMaxWidth().padding(top = 4.dp),
                track = Color.White.copy(alpha = 0.25f), fill = Color.White)
            Text("$expInLevel / 100 XP до уровня ${gameState.level + 1}", fontFamily = RubikFamily,
                fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
        }

        // Статистика 2×2.
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("🎮", "${gameState.quizzesCompleted}", "игр сыграно", Modifier.weight(1f))
            StatTile("🎯", "$accuracy%", "верных ответов", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("🔥", "${gameState.streakDays}", "лучшая серия", Modifier.weight(1f))
            StatTile("⭐", "$starsTotal", "звёзд собрано", Modifier.weight(1f))
        }

        // Прогресс по темам (до 5 с наибольшим прогрессом).
        val topicProgress = ALL_TOPICS.map { t ->
            val passed = (0..2).sumOf { d -> LevelMap.countPassed(t.id, d, gameState.levelStars) }
            Triple(t, passed, passed / (LevelMap.LEVELS_PER_TOPIC * 3f))
        }.filter { it.second > 0 }.sortedByDescending { it.second }.take(5)

        if (topicProgress.isNotEmpty()) {
            Text("Прогресс по темам", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = Kids.TextPrimary)
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    topicProgress.forEach { (topic, _, fraction) ->
                        val tc = topicColors(topic.id)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(topic.emoji, fontSize = 20.sp)
                            Text(topic.name, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp, color = Kids.TextPrimary, modifier = Modifier.width(96.dp))
                            KidsProgressBar(fraction, Modifier.weight(1f), height = 8.dp, fill = tc.primary)
                            Text("${(fraction * 100).toInt()}%", fontFamily = RubikFamily,
                                fontWeight = FontWeight.Bold, fontSize = 12.sp, color = tc.shadow)
                        }
                    }
                }
            }
        }

        // Достижения — сводка + последние.
        Text("Достижения", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
            fontSize = 18.sp, color = Kids.TextPrimary)
        Box(Modifier.clickable(onClick = onOpenAchievements)) {
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Открыто $unlocked из ${achievements.size}", fontFamily = RubikFamily,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Kids.TextPrimary)
                        Text("Все ›", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 13.sp, color = Kids.Primary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        achievements.filter { it.current >= it.target }.take(4).forEach { a ->
                            Box(
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                                    .background(a.color.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) { Icon(a.icon, contentDescription = a.title, tint = a.color,
                                modifier = Modifier.size(26.dp)) }
                        }
                        if (unlocked == 0) {
                            Text("Пока пусто — вперёд за наградами!", fontFamily = RubikFamily,
                                fontSize = 12.sp, color = Kids.TextMuted)
                        }
                    }
                }
            }
        }
        Box(Modifier.height(12.dp))
    }
}

@Composable
private fun StatTile(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    KidsCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 24.sp)
            Text(value, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
                color = Kids.TextPrimary)
            Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                color = Kids.TextSecondary)
        }
    }
}
