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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.data.GameData

@Composable
fun QuizMenuScreen(
    gameState: GameState,
    availableQuizzes: List<Quiz>,
    completedQuizzes: Set<Int>,
    onNavigateToQuiz: (quizId: Int) -> Unit,
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
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "Викторины",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Progress indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE0E1FE)
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${completedQuizzes.size}/${availableQuizzes.size}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Прогресс",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = if (availableQuizzes.isNotEmpty()) {
                        completedQuizzes.size.toFloat() / availableQuizzes.size
                    } else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Пройдено ${completedQuizzes.size} из ${availableQuizzes.size} викторин",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                // Level requirement indicator
                val lockedQuizzes = GameData.getAllQuizzes().filter { quiz ->
                    quiz.requiredLevel > gameState.unlockedQuizLevels
                }
                
                if (lockedQuizzes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🔒 Заблокированные викторины",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                                Text(
                                    text = "Требуется уровень ${gameState.unlockedQuizLevels + 1} для разблокировки",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF9800).copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Доступно еще ${lockedQuizzes.size} викторин",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF9800).copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quiz list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableQuizzes) { quiz ->
                QuizMenuItem(
                    quiz = quiz,
                    isCompleted = completedQuizzes.contains(quiz.id),
                    isUnlocked = quiz.requiredLevel <= gameState.unlockedQuizLevels,
                    canPlay = canPlayQuiz(quiz.id, completedQuizzes, gameState.unlockedQuizLevels),
                    onClick = { onNavigateToQuiz(quiz.id) }
                )
            }
            
            // Show locked quizzes preview
            val lockedQuizzes = GameData.getAllQuizzes().filter { quiz ->
                quiz.requiredLevel > gameState.unlockedQuizLevels
            }.take(3) // Show only first 3 locked quizzes
            
            if (lockedQuizzes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "🔒 Заблокированные викторины",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(lockedQuizzes) { quiz ->
                    LockedQuizItem(
                        quiz = quiz,
                        currentLevel = gameState.unlockedQuizLevels
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun QuizMenuItem(
    quiz: Quiz,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    canPlay: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                canPlay -> Color(0xFFE5E6FA)
                else -> Color.Gray.copy(alpha = 0.1f)
            }
        ),
        onClick = if (canPlay) onClick else { {} }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quiz icon
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCompleted -> Color(0xFF4CAF50)
                        canPlay -> when (quiz.difficulty) {
                            1 -> Color(0xFF4CAF50)
                            2 -> Color(0xFFFF9800)
                            3 -> Color(0xFFE91E63)
                            4 -> Color(0xFF9C27B0)
                            5 -> Color(0xFF2196F3)
                            6 -> Color(0xFF795548)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        else -> Color.Gray
                    }
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isCompleted -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        canPlay -> {
                            Text(
                                text = "${quiz.difficulty}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Level info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCompleted -> Color(0xFF4CAF50)
                            canPlay -> when (quiz.difficulty) {
                                1 -> Color(0xFF4CAF50)
                                2 -> Color(0xFFFF9800)
                                3 -> Color(0xFFE91E63)
                                4 -> Color(0xFF9C27B0)
                                5 -> Color(0xFF2196F3)
                                6 -> Color(0xFF795548)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            else -> Color.Gray
                        }
                    )
                    
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Completed",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = quiz.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.7f)
                        canPlay -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else -> Color.Gray
                    }
                )
                
                Text(
                    text = "Категория: ${quiz.category} • ${quiz.questions.size} вопросов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                if (!isUnlocked) {
                    Text(
                        text = "Требуется уровень ${quiz.requiredLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else if (!canPlay && !isCompleted) {
                    Text(
                        text = "Сначала пройдите предыдущую викторину",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Action button
            if (canPlay) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LockedQuizItem(
    quiz: Quiz,
    currentLevel: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Locked icon
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Gray
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Quiz info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                Text(
                    text = quiz.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Text(
                    text = "Категория: ${quiz.category} • ${quiz.questions.size} вопросов",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
                
                Text(
                    text = "🔒 Требуется уровень ${quiz.requiredLevel} (у вас ${currentLevel})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun canPlayQuiz(
    quizId: Int,
    completedQuizzes: Set<Int>,
    unlockedLevels: Int
): Boolean {
    val quiz = GameData.getQuiz(quizId)
    if (quiz == null) return false
    
    // Quiz must be unlocked
    if (quiz.requiredLevel > unlockedLevels) return false
    
    // For difficulty 1, can always be played if unlocked
    if (quiz.difficulty == 1) return true
    
    // For other difficulties, need to complete at least one quiz of previous difficulty
    val previousDifficultyQuizzes = GameData.getQuizzesForDifficulty(quiz.difficulty - 1)
    return previousDifficultyQuizzes.any { it.id in completedQuizzes }
} 