package ru.plumsoftware.game.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.R
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.ui.components.AnimatedCounter
import ru.plumsoftware.game.ui.components.game.GameScreenTopBar
import ru.plumsoftware.game.ui.theme.*

@Composable
fun ProfileScreen(
    gameState: GameState,
    stats: Map<String, Int>,
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onUpdatePlayerName: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 16.dp)
    ) {
        GameScreenTopBar(
            title = "Профиль",
            onBack = onBack,
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Настройки", tint = GameTextPrimary)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PlayerInfoCard(gameState, onUpdatePlayerName) }
            item { StatsGrid(stats) }
            item { AchievementsCarousel(gameState, stats) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PlayerInfoCard(
    gameState: GameState,
    onUpdatePlayerName: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember(gameState.playerName) { mutableStateOf(gameState.playerName) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Изменить имя") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { if (it.length <= 24) editedName = it },
                    singleLine = true,
                    placeholder = { Text("Введите имя") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editedName.trim()
                        if (trimmed.isNotEmpty()) {
                            onUpdatePlayerName(trimmed)
                        }
                        showEditDialog = false
                    },
                    enabled = editedName.trim().isNotEmpty()
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    val expInLevel = gameState.experience % 100
    val quizzesAtNextLevel = GameData.quizzesUnlockingAtPlayerLevel(gameState.level + 1)
    val progressTarget = expInLevel / 100f
    val progressAnim by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(1000),
        label = "levelProgress"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
                SmallFloatingActionButton(
                    onClick = {
                        editedName = gameState.playerName
                        showEditDialog = true
                    },
                    modifier = Modifier.size(32.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Изменить", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = gameState.playerName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedCounter(
                target = gameState.level,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (quizzesAtNextLevel > 0) {
                    "На уровне ${gameState.level + 1}: +${GameData.quizzesUnlockingLabel(quizzesAtNextLevel)}"
                } else {
                    "На следующем уровне новых викторин нет"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${gameState.experience} ОП",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "До уровня ${gameState.level + 1}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$expInLevel/100",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun StatsGrid(stats: Map<String, Int>) {
    val items = listOf(
        StatItem("Викторины", stats["quizzes_completed"] ?: 0, Icons.Default.Quiz),
        StatItem("Ответов", stats["correct_answers"] ?: 0, Icons.Default.Check),
        StatItem("Сыграно", stats["play_time_minutes"] ?: 0, Icons.Default.Timer, suffix = " мин"),
        StatItem("Категорий", stats["categories_played"] ?: 0, Icons.Default.Category)
    )

    Column {
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(items[0], modifier = Modifier.weight(1f))
            StatCard(items[1], modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(items[2], modifier = Modifier.weight(1f))
            StatCard(items[3], modifier = Modifier.weight(1f))
        }
    }
}

private data class StatItem(
    val label: String,
    val value: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val suffix: String = ""
)

@Composable
private fun StatCard(item: StatItem, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedCounter(
                target = item.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.label + item.suffix,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AchievementsCarousel(gameState: GameState, stats: Map<String, Int>) {
    val achievements = listOf(
        AchievementProfile("Первые шаги", "1 викторина", Icons.Default.Star, (stats["quizzes_completed"] ?: 0) >= 1, Color(0xFFFFD700)),
        AchievementProfile("Искатель", "10 викторин", Icons.Default.School, (stats["quizzes_completed"] ?: 0) >= 10, Color(0xFF4CAF50)),
        AchievementProfile("Серия", "7 дней", Icons.Default.LocalFireDepartment, gameState.streakDays >= 7, Color(0xFFFF6B6B)),
        AchievementProfile("Уровень 5", "Достигни 5 ур.", Icons.Default.TrendingUp, gameState.level >= 5, Color(0xFF2196F3)),
        AchievementProfile("Богач", "1000 монет", Icons.Default.MonetizationOn, gameState.coins >= 1000, Color(0xFFFF9800)),
        AchievementProfile("Мастер", "50 викторин", Icons.Default.EmojiEvents, (stats["quizzes_completed"] ?: 0) >= 50, Color(0xFF9C27B0))
    )

    Column {
        Text(
            text = "Достижения",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(achievements) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: AchievementProfile) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                achievement.color.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                achievement.icon,
                contentDescription = achievement.title,
                tint = if (achievement.isUnlocked) achievement.color else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (achievement.isUnlocked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

data class AchievementProfile(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean,
    val color: Color
)
