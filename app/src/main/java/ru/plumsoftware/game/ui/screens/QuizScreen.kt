package ru.plumsoftware.game.ui.screens

import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.App
import ru.plumsoftware.game.MainActivity
import ru.plumsoftware.game.ads.AdsManager
import ru.plumsoftware.game.data.Question

@Composable
fun QuizScreen(
    currentLevel: Int,
    questions: List<Question>,
    onBack: () -> Unit,
    onQuizComplete: (correctAnswers: Int, totalQuestions: Int) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var correctAnswers by remember { mutableStateOf(0) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Уровень $currentLevel",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Вопрос ${currentQuestionIndex + 1}/${questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Progress indicator
            LinearProgressIndicator(
                trackColor = MaterialTheme.colorScheme.primary,
                progress = (currentQuestionIndex + 1).toFloat() / questions.size,
                modifier = Modifier.width(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Question
        currentQuestion?.let { question ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Категория: ${question.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Сложность: ${"★".repeat(question.difficulty)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Answer options
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(question.options.indices.toList()) { index ->
                    val option = question.options[index]
                    val isSelected = selectedAnswer == index
                    val isCorrect = index == question.correctAnswer
                    val showCorrectAnswer = showResult

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                showCorrectAnswer && isCorrect -> Color(0xFF4CAF50)
                                showCorrectAnswer && isSelected && !isCorrect -> Color(0xFFF44336)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        onClick = {
                            if (!showResult) {
                                selectedAnswer = index
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${('A' + index)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(40.dp)
                            )

                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (showCorrectAnswer && isCorrect) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Correct",
                                    tint = Color.White
                                )
                            } else if (showCorrectAnswer && isSelected && !isCorrect) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Incorrect",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    if (!showResult) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = {
                                if (selectedAnswer != null) {
                                    showResult = true
                                    if (selectedAnswer == question.correctAnswer) {
                                        correctAnswers++
                                    }
                                }
                            },
                            contentPadding = PaddingValues(vertical = 16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedAnswer != null
                        ) {
                            Text("Ответить", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                onClick = {
                                    if (currentQuestionIndex < questions.size - 1) {
                                        currentQuestionIndex++
                                        selectedAnswer = null
                                        showResult = false
                                    } else {
                                        onQuizComplete(correctAnswers, questions.size)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 16.dp),
                            ) {
                                Text(
                                    text = if (currentQuestionIndex < questions.size - 1) "Следующий" else "Завершить",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    coinsEarned: Int,
    currentLevel: Int,
    onBackToHome: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current ?: MainActivity()
    val adsManager = AdsManager(App.adsBase, activity)

    val isPerfectScore = correctAnswers == totalQuestions
    val levelColor = when (currentLevel) {
        1 -> Color(0xFF4CAF50)
        2 -> Color(0xFFFF9800)
        3 -> Color(0xFFE91E63)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Level indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = levelColor.copy(alpha = 0.1f)
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Уровень $currentLevel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = levelColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Result icon
        Icon(
            imageVector = if (isPerfectScore) Icons.Default.Star else Icons.Default.EmojiEvents,
            contentDescription = "Result",
            modifier = Modifier.size(80.dp),
            tint = if (isPerfectScore) Color(0xFFFFD700) else levelColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Score
        Text(
            text = "$correctAnswers/$totalQuestions",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = levelColor
        )

        Text(
            text = "Правильных ответов",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        if (isPerfectScore) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "Отличный результат! 🎉",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Coins earned
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+$coinsEarned монет заработано!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    adsManager.showInterstitial {
                        onBackToHome()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("На главную")
            }

            Button(
                onClick = {
                    adsManager.showInterstitial {
                        onPlayAgain()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Играть снова")
            }
        }
    }
} 