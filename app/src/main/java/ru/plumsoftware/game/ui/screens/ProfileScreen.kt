package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import ru.plumsoftware.game.data.AchievementProgress
import ru.plumsoftware.game.data.Economy
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.ui.components.kids.AvatarCircle
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors
import java.time.LocalDate

private val DAYS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

/** Профиль и статистика (ТЗ §5.8). */
@Composable
fun ProfileScreen(
    gameState: GameState,
    achievements: List<AchievementProgress>,
    onOpenSettings: () -> Unit,
    onOpenAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = gameState.levelProgress
    val accuracy = if (gameState.answersTotal > 0) gameState.answersCorrect * 100 / gameState.answersTotal else 0
    val unlocked = achievements.filter { it.unlockedAt != null }
    val today = LocalDate.now()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Профиль", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Kids.TextPrimary)
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Kids.Card)
                    .clickable(onClick = onOpenSettings),
                contentAlignment = Alignment.Center
            ) { GameIcon("set_settings", "⚙️", 28.dp) }
        }

        // Фиолетовая карточка профиля.
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Kids.Primary).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvatarCircle(gameState.avatarId, 84.dp, goldenFrame = gameState.goldenFrame, borderWidth = 4.dp)
            Text(gameState.playerName, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
            Text(
                "Уровень ${progress.level} · ${Economy.rank(progress.level)}", fontFamily = RubikFamily,
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f)
            )
            KidsProgressBar(
                progress.fraction, Modifier.fillMaxWidth().padding(top = 4.dp),
                track = Color.White.copy(alpha = 0.25f), fill = Color.White
            )
            Text(
                "${progress.xpInLevel} / ${progress.xpNeeded} XP до уровня ${progress.level + 1}",
                fontFamily = RubikFamily, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f)
            )
        }

        // Статистика 2×2.
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("stat_games", "🎮", "${gameState.gamesPlayed}", "игр сыграно", Modifier.weight(1f))
            StatTile("stat_correct", "🎯", "$accuracy%", "верных ответов", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("streak_fire", "🔥", "${maxOf(gameState.bestStreak, gameState.streakDays)}", "лучшая серия", Modifier.weight(1f))
            StatTile("star", "⭐", "${gameState.starsTotal}", "звёзд собрано", Modifier.weight(1f))
        }

        // Активность за неделю: столбики Пн–Вс, сегодня выделен, справа сумма.
        val week = gameState.weekAnswers(today)
        val todayIdx = today.dayOfWeek.value - 1
        val max = (week.maxOrNull() ?: 0).coerceAtLeast(1)
        KidsCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Активность за неделю", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Kids.TextPrimary)
                    Text("${week.sum()} вопр.", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Kids.Primary)
                }
                Row(
                    Modifier.fillMaxWidth().height(110.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    week.forEachIndexed { i, v ->
                        Column(
                            Modifier.weight(1f).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (v > 0) Text("$v", fontFamily = RubikFamily, fontSize = 10.sp, color = Kids.TextMuted)
                            Box(
                                Modifier
                                    .padding(top = 2.dp)
                                    .width(18.dp)
                                    .height((70f * v / max).coerceAtLeast(4f).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            i == todayIdx -> Kids.Primary
                                            v > 0 -> Kids.PrimarySoftShadow
                                            else -> Kids.TrackBackground
                                        }
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                DAYS[i], fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
                                color = if (i == todayIdx) Kids.Primary else Kids.TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Точность по темам: до 5 тем с наибольшей точностью.
        val accuracyByTopic = ALL_TOPICS.mapNotNull { t ->
            val total = gameState.topicTotal[t.id] ?: 0
            if (total <= 0) null else t to ((gameState.topicCorrect[t.id] ?: 0) * 100 / total)
        }.sortedByDescending { it.second }.take(5)
        if (accuracyByTopic.isNotEmpty()) {
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Точность по темам", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Kids.TextPrimary)
                    accuracyByTopic.forEach { (topic, pct) ->
                        val tc = topicColors(topic.id)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            GameIcon("topic_${topic.id}", topic.emoji, 24.dp)
                            Text(
                                topic.name, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                color = Kids.TextPrimary, modifier = Modifier.width(92.dp), maxLines = 1
                            )
                            KidsProgressBar(pct / 100f, Modifier.weight(1f), height = 8.dp, fill = tc.primary)
                            Text("$pct%", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = tc.shadow)
                        }
                    }
                }
            }
        }

        // Достижения: 4 последних значка + «Все X/Y».
        Box(Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onOpenAchievements)) {
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Достижения", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Kids.TextPrimary)
                        Text(
                            "Все ${unlocked.size}/${achievements.size} ›", fontFamily = RubikFamily,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Kids.Primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        unlocked.sortedByDescending { it.unlockedAt }.take(4).forEach { a ->
                            Box(
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Kids.PrimarySoft),
                                contentAlignment = Alignment.Center
                            ) { GameIcon(a.def.iconKey, a.def.emoji, 36.dp) }
                        }
                        if (unlocked.isEmpty()) {
                            Text("Пока пусто — вперёд за наградами!", fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextMuted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatTile(iconKey: String, emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    KidsCard(modifier = modifier) {
        Column(
            Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GameIcon(iconKey, emoji, 32.dp)
            Text(value, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Kids.TextPrimary)
            Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Kids.TextSecondary)
        }
    }
}
