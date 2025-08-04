package ru.plumsoftware.game.ui.screens

import androidx.compose.animation.*
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
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.Question

@Composable
fun QuizScreen(
    onBack: () -> Unit,
    onQuizComplete: (correctAnswers: Int, totalQuestions: Int) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var correctAnswers by remember { mutableStateOf(0) }
    var questions by remember { mutableStateOf(GameData.questions.shuffled().take(5)) }
    
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
            
            Text(
                text = "Question ${currentQuestionIndex + 1}/${questions.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Progress indicator
            LinearProgressIndicator(
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                        text = "Category: ${question.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            if (!showResult) {
                Button(
                    onClick = {
                        if (selectedAnswer != null) {
                            showResult = true
                            if (selectedAnswer == question.correctAnswer) {
                                correctAnswers++
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer != null
                ) {
                    Text("Submit Answer")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedAnswer = null
                                showResult = false
                            } else {
                                onQuizComplete(correctAnswers, questions.size)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (currentQuestionIndex < questions.size - 1) "Next Question" else "Finish Quiz"
                        )
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
    onBackToHome: () -> Unit,
    onPlayAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Result icon
        Icon(
            imageVector = if (correctAnswers == totalQuestions) Icons.Default.Star else Icons.Default.EmojiEvents,
            contentDescription = "Result",
            modifier = Modifier.size(80.dp),
            tint = if (correctAnswers == totalQuestions) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Score
        Text(
            text = "$correctAnswers/$totalQuestions",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Correct Answers",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
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
                    text = "+$coinsEarned coins earned!",
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
                onClick = onBackToHome,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Home")
            }
            
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.weight(1f)
            ) {
                Text("Play Again")
            }
        }
    }
} 