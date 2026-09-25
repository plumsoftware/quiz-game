package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.theme.*

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val target: Int,
    val current: Int,
    val reward: Int = 0
)

/** Достижения (ТЗ §5.9): сводка + сетка 3 колонки, тап открывает окно с прогрессом. */
@Composable
fun AchievementsScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val achievements = getAchievements(gameState)
    val unlocked = achievements.count { it.current >= it.target }
    var details by remember { mutableStateOf<Achievement?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KidsBackButton(onClick = onBack)
                    Text("Достижения", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp, color = Kids.TextPrimary)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Открыто $unlocked из ${achievements.size}", fontFamily = RubikFamily,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Kids.TextSecondary)
                    KidsProgressBar(
                        if (achievements.isEmpty()) 0f else unlocked.toFloat() / achievements.size,
                        Modifier.fillMaxWidth(), fill = Kids.Primary
                    )
                }
            }
            items(achievements) { a -> AchievementBadge(a) { details = a } }
        }
    }

    details?.let { a ->
        AchievementDialog(a) { details = null }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement, onClick: () -> Unit) {
    val unlocked = achievement.current >= achievement.target
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Kids.Card)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp)
            .alpha(if (unlocked) 1f else 0.45f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp))
                .background(achievement.color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(achievement.icon, contentDescription = achievement.title, tint = achievement.color,
                modifier = Modifier.size(28.dp))
        }
        Text(achievement.title, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp,
            color = Kids.TextPrimary, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun AchievementDialog(achievement: Achievement, onDismiss: () -> Unit) {
    val unlocked = achievement.current >= achievement.target
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Kids.Card).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(achievement.color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) { Icon(achievement.icon, null, tint = achievement.color, modifier = Modifier.size(38.dp)) }
            Text(achievement.title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp, color = Kids.TextPrimary, textAlign = TextAlign.Center)
            Text(achievement.description, fontFamily = RubikFamily, fontSize = 14.sp,
                color = Kids.TextSecondary, textAlign = TextAlign.Center)
            KidsProgressBar(
                (achievement.current.toFloat() / achievement.target).coerceIn(0f, 1f),
                Modifier.fillMaxWidth(), fill = achievement.color
            )
            Text("${achievement.current}/${achievement.target}", fontFamily = RubikFamily,
                fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Kids.TextMuted)
            if (achievement.reward > 0) {
                Text(if (unlocked) "Награда получена: +${achievement.reward} 🪙"
                    else "Награда: +${achievement.reward} 🪙",
                    fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = Kids.CoinText)
            }
            KidsButton(text = "Закрыть", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

fun getAchievements(gameState: GameState): List<Achievement> {
    return listOf(
        Achievement(
            id = "first_quiz",
            title = "Первый шаг",
            description = "Пройди свою первую викторину",
            icon = Icons.Default.Quiz,
            color = Color(0xFF4CAF50),
            target = 1,
            current = gameState.quizzesCompleted,
            reward = 50
        ),
        Achievement(
            id = "quiz_master",
            title = "Мастер викторин",
            description = "Пройди 10 викторин",
            icon = Icons.Default.Star,
            color = Color(0xFFFF9800),
            target = 10,
            current = gameState.quizzesCompleted,
            reward = 200
        ),
        Achievement(
            id = "perfect_score",
            title = "Отличник",
            description = "Получи 100% правильных ответов",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFFE91E63),
            target = 1,
            current = if (gameState.correctAnswers > 0 && gameState.totalAnswers > 0 && 
                         gameState.correctAnswers == gameState.totalAnswers) 1 else 0,
            reward = 100
        ),
        Achievement(
            id = "coin_collector",
            title = "Коллекционер монет",
            description = "Накопи 1000 монет",
            icon = Icons.Default.MonetizationOn,
            color = Color(0xFFFFD700),
            target = 1000,
            current = gameState.coins,
            reward = 500
        ),
        Achievement(
            id = "daily_player",
            title = "Ежедневный игрок",
            description = "Играй 7 дней подряд",
            icon = Icons.Default.CalendarToday,
            color = Color(0xFF9C27B0),
            target = 7,
            current = gameState.streak,
            reward = 300
        ),
        Achievement(
            id = "level_up",
            title = "Повышение уровня",
            description = "Достигни 5 уровня",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF2196F3),
            target = 5,
            current = gameState.level,
            reward = 400
        ),
        Achievement(
            id = "time_spent",
            title = "Время знаний",
            description = "Проведи в игре 60 минут",
            icon = Icons.Default.Schedule,
            color = Color(0xFF607D8B),
            target = 60,
            current = gameState.playTimeMinutes,
            reward = 250
        ),
        Achievement(
            id = "category_explorer",
            title = "Исследователь категорий",
            description = "Играй в 5 разных категориях",
            icon = Icons.Default.Category,
            color = Color(0xFF795548),
            target = 5,
            current = gameState.categoriesPlayed.size,
            reward = 350
        ),
        Achievement(
            id = "quiz_levels",
            title = "Покоритель уровней",
            description = "Пройди все доступные уровни викторины",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFFFF6B35),
            target = gameState.unlockedQuizLevels,
            current = gameState.unlockedQuizLevels,
            reward = 600
        ),
        Achievement(
            id = "daily_tasks",
            title = "Выполнитель задач",
            description = "Выполни 5 ежедневных задач",
            icon = Icons.Default.Assignment,
            color = Color(0xFF4CAF50),
            target = 5,
            current = gameState.dailyTasksCompleted,
            reward = 400
        )
    )
} 