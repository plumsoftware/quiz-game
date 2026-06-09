package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.ui.components.game.GameScreenTopBar
import ru.plumsoftware.game.ui.theme.*
import ru.plumsoftware.game.ui.util.CategoryStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizMenuScreen(
    gameState: GameState,
    availableQuizzes: List<Quiz>,
    completedQuizzes: Set<Int>,
    onNavigateToQuiz: (quizId: Int) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var lockedQuizSheet by remember { mutableStateOf<Quiz?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val categories = remember(availableQuizzes) {
        availableQuizzes.map { it.category }.distinct().sorted()
    }

    val filteredQuizzes = remember(availableQuizzes, searchQuery, selectedCategory) {
        availableQuizzes.filter { quiz ->
            val matchesSearch = searchQuery.isBlank() ||
                quiz.title.contains(searchQuery, ignoreCase = true) ||
                quiz.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || quiz.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 16.dp)
    ) {
        GameScreenTopBar(
            title = "Викторины",
            onBack = onBack,
            actions = {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "${completedQuizzes.size}/${availableQuizzes.size}",
                            color = GameTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = GamePurpleLight)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = GameSurface,
                        labelColor = GameTextPrimary
                    )
                )
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск викторины...", color = GameTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GameTextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GameTextPrimary,
                unfocusedTextColor = GameTextPrimary,
                cursorColor = GamePurple,
                focusedBorderColor = GamePurple,
                unfocusedBorderColor = GameBorder,
                focusedContainerColor = GameSurface,
                unfocusedContainerColor = GameSurface
            ),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Все") }
                )
            }
            items(categories) { category ->
                val style = CategoryStyles.forCategory(category)
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                    label = { Text("${style.emoji} $category") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = style.color.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = {
                if (availableQuizzes.isNotEmpty())
                    completedQuizzes.size.toFloat() / availableQuizzes.size
                else 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredQuizzes) { quiz ->
                val canPlay = canPlayQuiz(quiz.id, completedQuizzes, gameState.unlockedQuizLevels)
                val isUnlocked = quiz.requiredLevel <= gameState.unlockedQuizLevels
                val isCompleted = completedQuizzes.contains(quiz.id)

                QuizMenuItem(
                    quiz = quiz,
                    isCompleted = isCompleted,
                    isUnlocked = isUnlocked,
                    canPlay = canPlay,
                    onClick = {
                        if (canPlay) onNavigateToQuiz(quiz.id)
                        else lockedQuizSheet = quiz
                    }
                )
            }

            if (filteredQuizzes.isEmpty()) {
                item {
                    Text(
                        text = "Ничего не найдено",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = GameTextMuted
                    )
                }
            }
        }
    }

    if (lockedQuizSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { lockedQuizSheet = null },
            sheetState = sheetState
        ) {
            val quiz = lockedQuizSheet!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Пройди уровень ${quiz.requiredLevel}, чтобы открыть!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GameTextPrimary
                )
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GameTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        lockedQuizSheet = null
                        val playable = availableQuizzes.firstOrNull {
                            canPlayQuiz(it.id, completedQuizzes, gameState.unlockedQuizLevels) &&
                                !completedQuizzes.contains(it.id)
                        }
                        playable?.let { onNavigateToQuiz(it.id) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Играть сейчас")
                }
                Spacer(modifier = Modifier.height(24.dp))
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
    val style = CategoryStyles.forCategory(quiz.category)
    val isLocked = !canPlay && !isCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.5f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) style.color.copy(alpha = 0.08f) else GameSurface,
            contentColor = GameTextPrimary
        ),
        onClick = onClick
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(style.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = style.emoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GameTextPrimary
                    )
                    Text(
                        text = "${quiz.questions.size} вопросов • ${quiz.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GameTextMuted
                    )
                    DifficultyStars(difficulty = quiz.difficulty)
                }

                when {
                    isCompleted -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    canPlay -> Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = style.color
                    )
                    else -> Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            if (isLocked) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyStars(difficulty: Int) {
    Text(
        text = "★".repeat(difficulty.coerceIn(1, 6)),
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFFFD700)
    )
}

private fun canPlayQuiz(
    quizId: Int,
    completedQuizzes: Set<Int>,
    unlockedLevels: Int
): Boolean {
    val quiz = GameData.getQuiz(quizId) ?: return false
    if (quiz.requiredLevel > unlockedLevels) return false
    if (quiz.difficulty == 1) return true
    val previousDifficultyQuizzes = GameData.getQuizzesForDifficulty(quiz.difficulty - 1)
    return previousDifficultyQuizzes.any { it.id in completedQuizzes }
}
