package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.firebase.RemoteConfigQuizModel
import ru.plumsoftware.game.ui.components.RemoteConfigQuizCard
import ru.plumsoftware.game.ui.components.game.*
import ru.plumsoftware.game.ui.isBetween
import ru.plumsoftware.game.ui.theme.*
import ru.plumsoftware.game.ui.util.countCompletedDailyTasks

@Composable
fun HomeScreen(
    gameState: GameState,
    availableQuizzes: List<ru.plumsoftware.game.data.Quiz>,
    currentTierQuizTotal: Int = 0,
    tasksProgress: Map<String, Int> = emptyMap(),
    onNavigateToQuiz: () -> Unit,
    onNavigateToDailyTasks: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    remoteQuiz: RemoteConfigQuizModel,
    onNavigateToRemoteConfigQuiz: (RemoteConfigQuizModel) -> Unit,
) {
    val dailyTasksTotal = remember { GameData.getDailyTasks().size }
    val dailyTasksDone = remember(tasksProgress) { countCompletedDailyTasks(tasksProgress) }
    val nearAchievement = remember(gameState) {
        getAchievements(gameState).firstOrNull { it.current < it.target }
    }

    val stats = PlayerStats(
        quizzesPlayed = gameState.quizzesCompleted,
        correctAnswers = gameState.correctAnswers,
        minutesPlayed = gameState.playTimeMinutes,
        categoriesPlayed = gameState.categoriesPlayed.size
    )

    val navItems = listOf(
        HomeNavItem(
            emoji = "✅",
            title = "Ежедневные задания",
            subtitle = "$dailyTasksDone из $dailyTasksTotal выполнено",
            accentColor = GameCorrect
        ),
        HomeNavItem(
            emoji = "🏆",
            title = "Достижения",
            subtitle = nearAchievement?.let { "${it.current} / ${it.target} до следующего" }
                ?: "Все достижения открыты",
            accentColor = GameGold
        ),
        HomeNavItem(
            emoji = "🛒",
            title = "Магазин",
            subtitle = "Подсказки, жизни и бонусы",
            accentColor = GamePurple
        ),
        HomeNavItem(
            emoji = "📊",
            title = "Статистика",
            subtitle = "Уровень ${gameState.level} • ${gameState.quizzesCompleted} викторин",
            accentColor = GameStreak
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(GameBackground)) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { GameTopBar(coins = gameState.coins, onSettingsClick = onNavigateToSettings) }
            item { StreakHeroBanner(streak = gameState.streakDays) }
            item {
                val tierTotal = currentTierQuizTotal.coerceAtLeast(1)
                val tierCompleted = (tierTotal - availableQuizzes.size).coerceAtLeast(0)
                DailyProgressBar(
                    completed = tierCompleted,
                    total = tierTotal
                )
            }
            item { QuickPlayButton(onPlay = onNavigateToQuiz) }
            item { HomeStatsGrid(stats = stats, modifier = Modifier.padding(horizontal = 14.dp)) }
            item {
                HomeNavButtons(
                    items = navItems,
                    onItemClick = { index ->
                        when (index) {
                            0 -> onNavigateToDailyTasks()
                            1 -> onNavigateToAchievements()
                            2 -> onNavigateToShop()
                            3 -> onNavigateToStats()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
            if (isBetween(remoteQuiz.dateStart, remoteQuiz.dateEnd)) {
                item {
                    RemoteConfigQuizCard(
                        remoteQuiz = remoteQuiz,
                        onNavigateToQuiz = onNavigateToRemoteConfigQuiz,
                        availableQuizzes = availableQuizzes
                    )
                }
            }
        }
    }
}
