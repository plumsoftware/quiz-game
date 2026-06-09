package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.data.firebase.RemoteConfigQuizModel
import ru.plumsoftware.game.ui.components.RemoteConfigQuizCard
import ru.plumsoftware.game.ui.components.game.*
import ru.plumsoftware.game.ui.isBetween
import ru.plumsoftware.game.ui.theme.*
import ru.plumsoftware.game.ui.util.CategoryStyles

@Composable
fun HomeScreen(
    gameState: GameState,
    availableQuizzes: List<Quiz>,
    completedQuizzes: Set<Int> = emptySet(),
    onNavigateToQuiz: () -> Unit,
    onNavigateToDailyTasks: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    remoteQuiz: RemoteConfigQuizModel,
    onNavigateToRemoteConfigQuiz: (RemoteConfigQuizModel) -> Unit,
    onContinueQuiz: (Int) -> Unit = {},
    onPurchasePowerUp: (PowerUpType) -> Unit = {}
) {
    val categories = remember(availableQuizzes) {
        availableQuizzes.map { it.category }.distinct().take(8)
    }
    val nearAchievement = remember(gameState, completedQuizzes) {
        when {
            completedQuizzes.size < 1 -> "Первые шаги — пройди 1 викторину"
            gameState.streakDays < 7 -> "Мастер серии — 7 дней подряд (${gameState.streakDays}/7)"
            gameState.level < 5 -> "Повышение уровня — достигни 5 ур. (${gameState.level}/5)"
            else -> "Искатель знаний — 10 викторин (${completedQuizzes.size}/10)"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(GameBackground)) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { GameTopBar(coins = gameState.coins, onSettingsClick = onNavigateToSettings) }
            item { StreakHeroBanner(streak = gameState.streakDays) }
            item {
                DailyProgressBar(
                    completed = completedQuizzes.size,
                    total = availableQuizzes.size.coerceAtLeast(1)
                )
            }
            item { QuickPlayButton(onPlay = onNavigateToQuiz) }

            item {
                HomePowerUpShopSection(
                    coins = gameState.coins,
                    inventory = gameState.powerUpInventory,
                    onOpenFullShop = onNavigateToShop,
                    onPurchase = onPurchasePowerUp
                )
            }

            item {
                CategoriesSection(
                    categories = categories,
                    availableQuizzes = availableQuizzes,
                    onCategoryClick = onNavigateToQuiz,
                    onQuizClick = onContinueQuiz
                )
            }

            item {
                NearAchievementTeaser(
                    text = nearAchievement,
                    onClick = onNavigateToAchievements
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

@Composable
private fun CategoriesSection(
    categories: List<String>,
    availableQuizzes: List<Quiz>,
    onCategoryClick: () -> Unit,
    onQuizClick: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Категории", style = MaterialTheme.typography.titleMedium, color = GameTextPrimary, fontWeight = FontWeight.Bold)
            TextButton(onClick = onCategoryClick) {
                Text("Все", color = GamePurpleLight)
                Icon(Icons.Rounded.ChevronRight, null, tint = GamePurpleLight, modifier = Modifier.size(18.dp))
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val style = CategoryStyles.forCategory(category)
                val quiz = availableQuizzes.firstOrNull { it.category == category }
                Surface(
                    onClick = { quiz?.let { onQuizClick(it.id) } ?: onCategoryClick() },
                    modifier = Modifier.width(110.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = style.color.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, style.color.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(style.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            category,
                            style = MaterialTheme.typography.labelSmall,
                            color = GameTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NearAchievementTeaser(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = GameGold.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, GameGold.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎯", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Ближайшее достижение", style = MaterialTheme.typography.labelMedium, color = GameGold)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = GameTextSecondary)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = GameTextMuted)
        }
    }
}
