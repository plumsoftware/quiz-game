package ru.plumsoftware.game.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.plumsoftware.game.App
import ru.plumsoftware.game.MainActivity
import ru.plumsoftware.game.ads.AdsBase
import ru.plumsoftware.game.audio.GameAudioManager
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.data.Question
import ru.plumsoftware.game.ui.components.game.*
import ru.plumsoftware.game.ui.theme.*
import ru.plumsoftware.game.ui.util.CategoryStyles
import kotlin.math.max

private const val QUESTION_TIME_SECONDS = 15
private const val MAX_HEARTS = 3

@Composable
fun QuizScreen(
    currentLevel: Int,
    questions: List<Question>,
    coins: Int = 0,
    powerUpInventory: Map<String, Int> = emptyMap(),
    onBack: () -> Unit,
    onQuizComplete: (correctAnswers: Int, totalQuestions: Int) -> Unit,
    onPurchasePowerUp: (PowerUpType) -> Unit,
    onConsumePowerUp: (PowerUpType, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { GameAudioManager(context) }
    DisposableEffect(Unit) { onDispose { audioManager.release() } }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var hearts by remember { mutableIntStateOf(MAX_HEARTS) }
    var timeLeft by remember { mutableIntStateOf(QUESTION_TIME_SECONDS) }
    var answerStates by remember { mutableStateOf<Map<Int, AnswerState>>(emptyMap()) }
    var showExplanation by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showShop by remember { mutableStateOf(false) }
    var hiddenAnswers by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var highlightedAnswer by remember { mutableStateOf<Int?>(null) }
    var shieldActive by remember { mutableStateOf(false) }
    var secondChanceActive by remember { mutableStateOf(false) }
    var slowTimer by remember { mutableStateOf(false) }
    var freezeTicks by remember { mutableIntStateOf(0) }
    var showHintBanner by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val hasAnswered = answerStates.isNotEmpty()
    val isPaused = showShop || freezeTicks > 0

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    fun resetQuestionPowerUps() {
        hiddenAnswers = emptySet()
        highlightedAnswer = null
        slowTimer = false
        freezeTicks = 0
        showHintBanner = false
        secondChanceActive = false
    }

    fun goToNextQuestion() {
        resetQuestionPowerUps()
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            answerStates = emptyMap()
            showExplanation = false
        } else {
            audioManager.playComplete()
            onQuizComplete(correctAnswers, questions.size)
        }
    }

    fun applyPowerUp(type: PowerUpType) {
        val question = currentQuestion ?: return
        if (hasAnswered && type != PowerUpType.SECOND_CHANCE) return

        onConsumePowerUp(type) { consumed ->
            if (!consumed) {
                snackbarMessage = "Нет улучшения «${type.title}»"
                return@onConsumePowerUp
            }
            when (type) {
                PowerUpType.HINT -> showHintBanner = true
                PowerUpType.FIFTY_FIFTY -> {
                    val wrongIds = question.options.indices
                        .filter { it != question.correctAnswer }
                        .shuffled()
                        .take(2)
                        .toSet()
                    hiddenAnswers = wrongIds
                }
                PowerUpType.EXTRA_LIFE -> hearts = minOf(hearts + 1, MAX_HEARTS + 2)
                PowerUpType.EXTRA_TIME -> timeLeft = minOf(timeLeft + 10, QUESTION_TIME_SECONDS + 15)
                PowerUpType.SKIP_QUESTION -> goToNextQuestion()
                PowerUpType.REVEAL_ANSWER, PowerUpType.LUCKY_HINT -> highlightedAnswer = question.correctAnswer
                PowerUpType.SHIELD -> shieldActive = true
                PowerUpType.FREEZE_TIME -> freezeTicks = 8
                PowerUpType.SECOND_CHANCE -> secondChanceActive = true
                PowerUpType.SLOW_TIMER -> slowTimer = true
                PowerUpType.DOUBLE_COINS -> snackbarMessage = "×2 монеты за следующий правильный ответ!"
            }
            snackbarMessage = "Использовано: ${type.emoji} ${type.title}"
        }
    }

    LaunchedEffect(currentQuestionIndex, hasAnswered, showShop, freezeTicks, slowTimer) {
        if (!hasAnswered && !isGameOver && !showShop) {
            timeLeft = QUESTION_TIME_SECONDS
            while (timeLeft > 0 && !hasAnswered && !isGameOver && !showShop) {
                if (freezeTicks > 0) {
                    delay(1000L)
                    freezeTicks--
                    continue
                }
                delay(if (slowTimer) 2000L else 1000L)
                timeLeft--
            }
            if (!hasAnswered && !isGameOver && timeLeft <= 0) {
                val correctId = currentQuestion?.correctAnswer ?: 0
                answerStates = currentQuestion?.options?.indices?.associate { id ->
                    id to when (id) {
                        correctId -> AnswerState.MISSED
                        else -> AnswerState.NONE
                    }
                } ?: emptyMap()
                if (shieldActive) {
                    shieldActive = false
                    snackbarMessage = "🛡️ Щит защитил от таймаута!"
                } else {
                    hearts = max(0, hearts - 1)
                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                    audioManager.playWrong()
                    if (hearts == 0) delay(1200L).also { isGameOver = true }
                }
                lastAnswerCorrect = false
                showExplanation = true
            }
        }
    }

    if (isGameOver) {
        GameOverScreen(
            correctAnswers = correctAnswers,
            totalQuestions = questions.size,
            onFinish = { onQuizComplete(correctAnswers, questions.size) },
            onBack = onBack
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(GameBackground)) {
        GameQuizTopBar(
            lives = hearts,
            timeLeft = timeLeft,
            totalTime = QUESTION_TIME_SECONDS,
            coins = coins,
            onExit = onBack
        )

        QuizPowerUpBar(
            inventory = powerUpInventory,
            enabled = !hasAnswered && !isGameOver,
            onUse = { applyPowerUp(it) },
            onOpenShop = { showShop = true }
        )

        AnimatedGameProgressBar(
            progress = (currentQuestionIndex + if (hasAnswered) 1f else 0f) / questions.size.coerceAtLeast(1)
        )

        Spacer(modifier = Modifier.height(16.dp))

        currentQuestion?.let { question ->
            val style = CategoryStyles.forCategory(question.category)

            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                CategoryTag(name = question.category, emoji = style.emoji)
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = question.question,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally { -it } + fadeOut(tween(200)))
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                label = "question"
            ) { questionText ->
                QuestionCard(
                    text = questionText,
                    questionNumber = currentQuestionIndex + 1,
                    total = questions.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp)
                )
            }

            if (showHintBanner) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = GameGold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GameGold.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "💡 Подсказка: категория «${question.category}», сложность ${"★".repeat(question.difficulty)}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = GameGold
                    )
                }
            }

            if (shieldActive) {
                Text(
                    "🛡️ Щит активен",
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = GamePurpleLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val answers = question.options.mapIndexed { index, text ->
                AnswerOption(id = index, text = text, isCorrect = index == question.correctAnswer)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(answers.filter { it.id !in hiddenAnswers }) { answer ->
                    val highlight = highlightedAnswer == answer.id
                    GameAnswerCard(
                        answer = answer,
                        answerState = when {
                            highlight && answerStates.isEmpty() -> AnswerState.MISSED
                            else -> answerStates[answer.id] ?: AnswerState.NONE
                        },
                        onClick = {
                            if (answerStates.isEmpty() || (secondChanceActive && !lastAnswerCorrect)) {
                                val isCorrect = answer.isCorrect
                                lastAnswerCorrect = isCorrect
                                answerStates = answers.associate { a ->
                                    a.id to when {
                                        a.isCorrect -> AnswerState.CORRECT
                                        a.id == answer.id && !isCorrect -> AnswerState.WRONG
                                        else -> AnswerState.NONE
                                    }
                                }
                                if (isCorrect) {
                                    correctAnswers++
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                    audioManager.playCorrect()
                                    showExplanation = true
                                    secondChanceActive = false
                                } else if (secondChanceActive) {
                                    secondChanceActive = false
                                    answerStates = emptyMap()
                                    snackbarMessage = "Второй шанс — попробуй ещё раз!"
                                } else if (shieldActive) {
                                    shieldActive = false
                                    showExplanation = true
                                    snackbarMessage = "🛡️ Щит спас от ошибки!"
                                } else {
                                    hearts = max(0, hearts - 1)
                                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                    audioManager.playWrong()
                                    showExplanation = true
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = showExplanation,
                enter = slideInVertically { it } + fadeIn()
            ) {
                AnswerExplanationCard(
                    isCorrect = lastAnswerCorrect,
                    explanation = if (lastAnswerCorrect) "Отличный ответ!"
                    else "Правильно: ${question.options[question.correctAnswer]}",
                    onNext = {
                        if (hearts == 0) {
                            isGameOver = true
                            return@AnswerExplanationCard
                        }
                        goToNextQuestion()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showShop) {
            PowerUpShopOverlay(
                coins = coins,
                inventory = powerUpInventory,
                onClose = { showShop = false },
                onPurchase = onPurchasePowerUp
            )
        }
    }
}

@Composable
fun GameQuizTopBar(
    lives: Int,
    timeLeft: Int,
    totalTime: Int,
    coins: Int,
    onExit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GameSurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onExit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.Close, "Выйти", tint = GameTextMuted, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        LivesRow(lives = lives, modifier = Modifier.weight(1f))
        GameTimer(timeLeft = timeLeft, totalTime = totalTime)
        Spacer(modifier = Modifier.width(8.dp))
        CoinBadge(coins = coins)
    }
}

@Composable
fun LivesRow(lives: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        repeat(3) { index ->
            val isAlive = index < lives
            val scale by animateFloatAsState(
                targetValue = if (isAlive) 1f else 0.75f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "heart"
            )
            Text(
                text = if (isAlive) "❤️" else "🖤",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale).padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun GameTimer(timeLeft: Int, totalTime: Int) {
    val progress = timeLeft.toFloat() / totalTime
    val timerColor by animateColorAsState(
        targetValue = when {
            progress > 0.5f -> GamePurple
            progress > 0.25f -> GameMissed
            else -> GameWrong
        },
        animationSpec = tween(300),
        label = "timerColor"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = timerColor,
            strokeWidth = 3.dp,
            trackColor = GameBorder,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = timeLeft.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = timerColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QuestionCard(
    text: String,
    questionNumber: Int,
    total: Int,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = GameSurface,
        border = BorderStroke(1.dp, GameBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "$questionNumber / $total",
                style = MaterialTheme.typography.labelSmall,
                color = GameTextDisabled
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = GameTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
fun GameAnswerCard(answer: AnswerOption, answerState: AnswerState, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = when (answerState) {
            AnswerState.CORRECT -> GameCorrectBg
            AnswerState.WRONG -> GameWrongBg
            AnswerState.NONE -> GameSurface
            AnswerState.MISSED -> Color(0xFF2E1A00)
        },
        animationSpec = tween(250),
        label = "answerBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when (answerState) {
            AnswerState.CORRECT -> GameCorrect
            AnswerState.WRONG -> GameWrong
            AnswerState.NONE -> GameBorder
            AnswerState.MISSED -> GameMissed
        },
        animationSpec = tween(250),
        label = "answerBorder"
    )
    val textColor by animateColorAsState(
        targetValue = when (answerState) {
            AnswerState.CORRECT -> GameCorrectText
            AnswerState.WRONG -> GameWrongText
            AnswerState.NONE -> GameTextSecondary
            AnswerState.MISSED -> Color(0xFFFFB74D)
        },
        animationSpec = tween(250),
        label = "answerText"
    )

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(answerState) {
        if (answerState == AnswerState.WRONG) {
            repeat(3) {
                shakeOffset.animateTo(8f, tween(50))
                shakeOffset.animateTo(-8f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    Surface(
        onClick = onClick,
        enabled = answerState == AnswerState.NONE,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.8f)
            .graphicsLayer { translationX = shakeOffset.value },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (answerState == AnswerState.CORRECT) {
                    Text("✓ ", color = GameCorrectText, fontWeight = FontWeight.Bold)
                } else if (answerState == AnswerState.WRONG) {
                    Text("✗ ", color = GameWrongText, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = answer.text,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AnswerExplanationCard(isCorrect: Boolean, explanation: String, onNext: () -> Unit) {
    val bgColor = if (isCorrect) GameCorrectBg else GameWrongBg
    val accentColor = if (isCorrect) GameCorrect else GameWrong
    val textColor = if (isCorrect) GameCorrectText else GameWrongText
    val title = if (isCorrect) "Правильно! ✓" else "Неверно ✗"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = textColor, fontWeight = FontWeight.Bold)
            if (explanation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Далее →", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun GameOverScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(GameBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💔", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Жизни закончились!", style = MaterialTheme.typography.headlineMedium, color = GameTextPrimary, fontWeight = FontWeight.Bold)
        Text("$correctAnswers из $totalQuestions правильных", style = MaterialTheme.typography.bodyLarge, color = GameTextMuted)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GamePurple)) {
            Text("Посмотреть результат")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, GameBorder)) {
            Text("Выйти", color = GameTextSecondary)
        }
    }
}

// ─── Result Screen ───────────────────────────────────────────────────────────

@Composable
fun QuizResultScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    coinsEarned: Int,
    currentLevel: Int,
    onBackToHome: () -> Unit,
    onPlayAgain: () -> Unit,
    displayAds: Boolean
) {
    val activity = LocalActivity.current ?: MainActivity()
    val adsManager = rememberAdsManager(activity)
    val xpEarned = correctAnswers * 5 * currentLevel.coerceAtLeast(1)

    fun runWithInterstitial(action: () -> Unit) {
        if (App.adsBase == AdsBase.AdsGooglePlay() && !displayAds) {
            action()
        } else {
            adsManager.showInterstitial(action)
        }
    }

    val percentage = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f
    val stars = when {
        percentage == 1f -> 3
        percentage >= 0.7f -> 2
        else -> 1
    }
    val (emoji, title) = when (stars) {
        3 -> "🏆" to "Идеально!"
        2 -> "⭐" to "Отлично!"
        else -> "💪" to "Продолжай!"
    }
    val isNewRecord = percentage >= 0.9f

    Box(modifier = Modifier.fillMaxSize().background(GameBackground)) {
        GameAdOverlays(
            isAdLoading = adsManager.isAdLoading,
            rewardCoins = null,
            onDismissReward = {}
        )

        if (stars == 3) GameConfetti()

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            PulsingEmoji(emoji = emoji)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge, color = GameTextPrimary)
            Text("$correctAnswers из $totalQuestions правильных", style = MaterialTheme.typography.bodyLarge, color = GameTextMuted)
            Spacer(modifier = Modifier.height(24.dp))
            StarsRow(count = stars, total = 3)
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedScoreCircle(score = correctAnswers, total = totalQuestions)
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RewardChip(label = "+$coinsEarned монет", emoji = "🪙", color = GameGold)
                RewardChip(label = "+$xpEarned ОП", emoji = "⚡", color = GameXP)
            }
            if (isNewRecord) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GameGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, GameGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        "🎉 Новый рекорд!",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = GameGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { runWithInterstitial(onPlayAgain) },
                enabled = !adsManager.isAdLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GamePurple)
            ) { Text("Играть ещё →", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { runWithInterstitial(onBackToHome) },
                enabled = !adsManager.isAdLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GameBorder)
            ) { Text("На главную", color = GameTextSecondary) }
        }
    }
}

@Composable
private fun StarsRow(count: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { i ->
            val lit = i < count
            val scale by animateFloatAsState(
                targetValue = if (lit) 1f else 0.7f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "star"
            )
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(i * 200L); visible = true }
            AnimatedVisibility(visible = visible, enter = scaleIn(spring(Spring.DampingRatioLowBouncy))) {
                Text(
                    text = if (lit) "⭐" else "☆",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                    color = if (lit) GameGold else GameTextDisabled
                )
            }
        }
    }
}

@Composable
private fun AnimatedScoreCircle(score: Int, total: Int) {
    val percentage = if (total > 0) score.toFloat() / total else 0f
    val animatedPct by animateFloatAsState(targetValue = percentage, animationSpec = tween(800), label = "score")

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        CircularProgressIndicator(
            progress = { animatedPct },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 8.dp,
            color = GamePurple,
            trackColor = GameBorder
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(animatedPct * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = GameTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GameConfetti() {
    val colors = listOf(GamePurple, GameGold, GameCorrect, GameWrong, Color(0xFF2196F3))
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "confettiTime"
    )
    val particles = remember {
        List(40) { i ->
            Triple(
                kotlin.random.Random.nextFloat(),
                kotlin.random.Random.nextFloat(),
                colors[i % colors.size]
            )
        }
    }
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (x, y, color) ->
            val py = ((y + time * 0.5f) % 1.2f) * size.height
            drawCircle(color = color, radius = 6f, center = androidx.compose.ui.geometry.Offset(x * size.width, py))
        }
    }
}
