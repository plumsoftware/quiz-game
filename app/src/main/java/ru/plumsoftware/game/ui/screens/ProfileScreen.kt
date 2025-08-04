package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import ru.plumsoftware.game.data.GameState

@Composable
fun ProfileScreen(
    gameState: GameState,
    stats: Map<String, Int>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player info card
            item {
                PlayerInfoCard(gameState)
            }
            
            // Statistics card
            item {
                StatisticsCard(stats)
            }
            
            // Achievements card
            item {
                AchievementsCard(gameState, stats)
            }
        }
    }
}

@Composable
fun PlayerInfoCard(gameState: GameState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Level ${gameState.level}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${gameState.experience} XP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress to next level
            val progressToNextLevel = (gameState.experience % 100).toFloat() / 100f
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress to Level ${gameState.level + 1}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${gameState.experience % 100}/100",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                LinearProgressIndicator(
                    progress = progressToNextLevel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StatisticsCard(stats: Map<String, Int>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val statItems = listOf(
                Triple(Icons.Default.Quiz, "Quizzes Completed", stats["quizzes_completed"] ?: 0),
                Triple(Icons.Default.Check, "Correct Answers", stats["correct_answers"] ?: 0),
                Triple(Icons.Default.Timer, "Minutes Played", stats["play_time_minutes"] ?: 0),
                Triple(Icons.Default.Category, "Categories Played", stats["categories_played"] ?: 0)
            )
            
            statItems.forEach { (icon, label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Text(
                        text = "$value",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsCard(gameState: GameState, stats: Map<String, Int>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val achievements = listOf(
                Achievement(
                    title = "First Steps",
                    description = "Complete your first quiz",
                    icon = Icons.Default.Star,
                    isUnlocked = (stats["quizzes_completed"] ?: 0) >= 1,
                    color = Color(0xFFFFD700)
                ),
                Achievement(
                    title = "Knowledge Seeker",
                    description = "Complete 10 quizzes",
                    icon = Icons.Default.School,
                    isUnlocked = (stats["quizzes_completed"] ?: 0) >= 10,
                    color = Color(0xFF4CAF50)
                ),
                Achievement(
                    title = "Perfect Score",
                    description = "Get all answers correct in a quiz",
                    icon = Icons.Default.EmojiEvents,
                    isUnlocked = false, // This would need to be tracked separately
                    color = Color(0xFFE91E63)
                ),
                Achievement(
                    title = "Streak Master",
                    description = "Maintain a 7-day streak",
                    icon = Icons.Default.LocalFireDepartment,
                    isUnlocked = gameState.streakDays >= 7,
                    color = Color(0xFFFF6B6B)
                ),
                Achievement(
                    title = "Level Up",
                    description = "Reach level 5",
                    icon = Icons.Default.TrendingUp,
                    isUnlocked = gameState.level >= 5,
                    color = Color(0xFF2196F3)
                ),
                Achievement(
                    title = "Coin Collector",
                    description = "Earn 1000 coins",
                    icon = Icons.Default.MonetizationOn,
                    isUnlocked = gameState.coins >= 1000,
                    color = Color(0xFFFF9800)
                )
            )
            
            achievements.forEach { achievement ->
                AchievementItem(achievement = achievement)
            }
        }
    }
}

data class Achievement(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean,
    val color: Color
)

@Composable
fun AchievementItem(achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = achievement.icon,
            contentDescription = achievement.title,
            tint = if (achievement.isUnlocked) achievement.color else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (achievement.isUnlocked) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                else 
                    Color.Gray
            )
        }
        
        if (achievement.isUnlocked) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Unlocked",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 