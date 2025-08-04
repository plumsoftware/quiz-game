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
import ru.plumsoftware.game.data.DailyTask
import ru.plumsoftware.game.data.GameData

@Composable
fun DailyTasksScreen(
    tasksProgress: Map<String, Int>,
    onBack: () -> Unit,
    onTaskCompleted: (taskId: Int, reward: Int) -> Unit
) {
    val dailyTasks = remember { GameData.getDailyTasks() }
    
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
                text = "Ежедневные задания",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress summary
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Сегодняшний прогресс",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProgressItem(
                        icon = Icons.Default.Quiz,
                        label = "Викторины",
                        value = tasksProgress["quizzes_completed"] ?: 0,
                        target = 3
                    )
                    
                    ProgressItem(
                        icon = Icons.Default.Check,
                        label = "Правильно",
                        value = tasksProgress["correct_answers"] ?: 0,
                        target = 5
                    )
                    
                    ProgressItem(
                        icon = Icons.Default.Timer,
                        label = "Минуты",
                        value = tasksProgress["play_time_minutes"] ?: 0,
                        target = 10
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tasks list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dailyTasks) { task ->
                TaskCard(
                    task = task,
                    progress = tasksProgress,
                    onTaskCompleted = onTaskCompleted
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TaskCard(
    task: DailyTask,
    progress: Map<String, Int>,
    onTaskCompleted: (taskId: Int, reward: Int) -> Unit
) {
    val isCompleted = when (task.id) {
        1 -> (progress["quizzes_completed"] ?: 0) >= 3
        2 -> (progress["correct_answers"] ?: 0) >= 5
        3 -> (progress["play_time_minutes"] ?: 0) >= 10
        4 -> (progress["categories_played"] ?: 0) >= GameData.getCategories().size
        5 -> false // Perfect score is handled separately
        6 -> (progress["quiz_levels_completed"] ?: 0) >= 1
        7 -> (progress["coins_earned"] ?: 0) >= 100
        else -> false
    }
    
    val progressValue = when (task.id) {
        1 -> (progress["quizzes_completed"] ?: 0).toFloat() / 3f
        2 -> (progress["correct_answers"] ?: 0).toFloat() / 5f
        3 -> (progress["play_time_minutes"] ?: 0).toFloat() / 10f
        4 -> (progress["categories_played"] ?: 0).toFloat() / GameData.getCategories().size.toFloat()
        5 -> 0f
        6 -> (progress["quiz_levels_completed"] ?: 0).toFloat() / 1f
        7 -> (progress["coins_earned"] ?: 0).toFloat() / 100f
        else -> 0f
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                Color(0xFF4CAF50).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = "Reward",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "+${task.reward}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
            
            if (!isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progressValue.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = when (task.id) {
                        1 -> "${progress["quizzes_completed"] ?: 0}/3 викторин завершено"
                        2 -> "${progress["correct_answers"] ?: 0}/5 правильных ответов"
                        3 -> "${progress["play_time_minutes"] ?: 0}/10 минут сыграно"
                        4 -> "${progress["categories_played"] ?: 0}/${GameData.getCategories().size} категорий"
                        5 -> "Получи все ответы правильно в одной викторине"
                        6 -> "${progress["quiz_levels_completed"] ?: 0}/1 новый уровень пройден"
                        7 -> "${progress["coins_earned"] ?: 0}/100 монет собрано"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ProgressItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Int,
    target: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "$value/$target",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
} 