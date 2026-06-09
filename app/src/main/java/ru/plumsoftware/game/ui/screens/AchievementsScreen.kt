package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.ui.components.game.GameScreenTopBar
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

@Composable
fun AchievementsScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 16.dp)
    ) {
        GameScreenTopBar(
            title = "Достижения",
            onBack = onBack,
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Настройки", tint = GameTextPrimary)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Achievements list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(getAchievements(gameState)) { achievement ->
                AchievementCard(achievement = achievement)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    val progress = (achievement.current.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f)
    val isCompleted = achievement.current >= achievement.target
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                achievement.color.copy(alpha = 0.1f)
            else
                GameSurface,
            contentColor = GameTextPrimary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted) achievement.color else Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = achievement.icon,
                            contentDescription = achievement.title,
                            tint = if (isCompleted) Color.White else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) achievement.color else GameTextPrimary
                )

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GameTextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isCompleted) achievement.color else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${achievement.current}/${achievement.target}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GameTextMuted
                )
                
                if (achievement.reward > 0 && isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Reward",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "+${achievement.reward}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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