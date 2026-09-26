package ru.plumsoftware.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.plumsoftware.game.audio.LocalFeedback
import ru.plumsoftware.game.audio.Sfx
import ru.plumsoftware.game.audio.Vibe
import ru.plumsoftware.game.data.Economy
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.data.topicById
import ru.plumsoftware.game.ui.QuizRunStats
import ru.plumsoftware.game.ui.QuizSession
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.components.kids.KidsDialog
import ru.plumsoftware.game.ui.components.kids.UiIcon
import ru.plumsoftware.game.ui.components.kids.UiIconView
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors

private enum class AnswerOutcome { NONE, CORRECT, WRONG, TIMEOUT }

/** Уровень викторины (ТЗ §5.5). Реклама здесь не показывается никогда. */
@Composable
fun QuizScreen(
    session: QuizSession,
    coins: Int,
    powerUpInventory: Map<String, Int>,
    onExit: () -> Unit,
    onFinish: (QuizRunStats) -> Unit,
    onConsumeHint: (PowerUpType, (Boolean) -> Unit) -> Unit,
    onBuyHint: (PowerUpType, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = LocalFeedback.current
    val topic = topicById(session.topicId)
    val tc = topicColors(session.topicId)
    val difficulty = session.difficulty
    val qs = session.questions
    val key = session.sessionId

    var qIndex by remember(key) { mutableIntStateOf(0) }
    var correctCount by remember(key) { mutableIntStateOf(0) }
    var skippedCount by remember(key) { mutableIntStateOf(0) }
    var mistakes by remember(key) { mutableIntStateOf(0) }
    var lives by remember(key) { mutableIntStateOf(difficulty.lives) }
    var usedHints by remember(key) { mutableStateOf(false) }
    var fastAnswer by remember(key) { mutableStateOf(false) }
    var finished by remember(key) { mutableStateOf(false) }

    // Состояние текущего вопроса.
    var selected by remember(key, qIndex) { mutableStateOf<Int?>(null) }
    var outcome by remember(key, qIndex) { mutableStateOf(AnswerOutcome.NONE) }
    var timeLeft by remember(key, qIndex) { mutableIntStateOf(difficulty.secondsPerQuestion) }
    var frozen by remember(key, qIndex) { mutableStateOf(false) }
    var hidden by remember(key, qIndex) { mutableStateOf(setOf<Int>()) }
    var usedFifty by remember(key, qIndex) { mutableStateOf(false) }
    var shownAt by remember(key, qIndex) { mutableLongStateOf(System.currentTimeMillis()) }

    var showExitConfirm by remember(key) { mutableStateOf(false) }
    var showLivesDialog by remember(key) { mutableStateOf(false) }
    var buyHintFor by remember(key) { mutableStateOf<PowerUpType?>(null) }

    val question = qs[qIndex.coerceIn(0, qs.lastIndex)]
    val answered = outcome != AnswerOutcome.NONE
    val coinsPerCorrect = Economy.coinsPerCorrect(difficulty)
    val shake = remember(key, qIndex) { Animatable(0f) }

    fun finish() {
        if (finished) return
        finished = true
        onFinish(
            QuizRunStats(
                correct = correctCount,
                total = qs.size,
                skipped = skippedCount,
                mistakes = mistakes,
                usedHints = usedHints,
                fastAnswer = fastAnswer
            )
        )
    }

    fun advance() {
        if (qIndex + 1 >= qs.size) finish() else qIndex++
    }

    fun submit(optionIndex: Int?) {
        if (answered || finished) return
        val correct = optionIndex != null && optionIndex == question.answer
        selected = optionIndex
        outcome = when {
            optionIndex == null -> AnswerOutcome.TIMEOUT
            correct -> AnswerOutcome.CORRECT
            else -> AnswerOutcome.WRONG
        }
        feedback?.stopSpeaking()
        if (correct) {
            correctCount++
            if (System.currentTimeMillis() - shownAt < 3000L) fastAnswer = true
            feedback?.play(Sfx.CORRECT)
        } else {
            lives--
            mistakes++
            feedback?.play(Sfx.WRONG)
            feedback?.vibrate(Vibe.MEDIUM)
        }
    }

    BackHandler(enabled = !finished) { showExitConfirm = true }

    // Озвучка вопроса (§5.11).
    LaunchedEffect(key, qIndex) {
        shownAt = System.currentTimeMillis()
        val letters = "АБВГ"
        val text = buildString {
            append(question.text)
            question.options.forEachIndexed { i, o -> append(". ").append(letters.getOrElse(i) { ' ' }).append(": ").append(o) }
        }
        feedback?.speak(text)
    }

    // Таймер вопроса: идёт, пока не отвечено и не заморожен. Тик в последние 5 секунд (§5.5).
    LaunchedEffect(key, qIndex, answered, frozen, showExitConfirm) {
        while (!answered && !frozen && !showExitConfirm && timeLeft > 0) {
            delay(1000)
            if (answered || frozen || showExitConfirm) break
            timeLeft--
            if (timeLeft in 1..5) feedback?.play(Sfx.TICK)
        }
        if (timeLeft <= 0 && !answered) submit(null)
    }

    // Покачивание неверно выбранного варианта (300 мс, §10).
    LaunchedEffect(outcome) {
        if (outcome == AnswerOutcome.WRONG) {
            listOf(-10f, 10f, -7f, 7f, -3f, 0f).forEach { shake.animateTo(it, tween(50)) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp)
                .padding(bottom = if (answered) 190.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Верхняя панель: выход, прогресс, жизни.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                        .background(Kids.Card).clickable { showExitConfirm = true },
                    contentAlignment = Alignment.Center
                ) { UiIconView(UiIcon.CLOSE, tint = Kids.TextSecondary, size = 18.dp) }
                val progress by animateFloatAsState(
                    (qIndex + if (answered) 1 else 0).toFloat() / qs.size, tween(300), label = "levelProgress"
                )
                Box(
                    modifier = Modifier.weight(1f).height(14.dp).clip(RoundedCornerShape(8.dp))
                        .background(Kids.TrackBackground)
                ) {
                    Box(
                        Modifier.fillMaxWidth(progress).height(14.dp).clip(RoundedCornerShape(8.dp))
                            .background(Kids.Success)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(difficulty.lives) { i ->
                        GameIcon("hint_life", "❤️", 22.dp, modifier = Modifier.alpha(if (i < lives) 1f else 0.25f))
                    }
                }
            }

            // Строка: номер вопроса, тема и таймер.
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Вопрос ${qIndex + 1} из ${qs.size} · ${topic.name}" + if (session.boss) " · Босс 👑" else "",
                    fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = Kids.TextSecondary, modifier = Modifier.weight(1f)
                )
                TimerChip(timeLeft, frozen)
            }

            // Карточка вопроса: иллюстрация 130 px на фоне цвета темы + текст.
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
                    .background(Kids.Card).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(18.dp))
                        .background(tc.cardBg),
                    contentAlignment = Alignment.Center
                ) { GameIcon("topic_${topic.id}", topic.emoji, 92.dp) }
                Text(
                    question.text, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                    fontSize = 19.sp, lineHeight = 25.sp, textAlign = TextAlign.Center,
                    color = Kids.TextPrimary
                )
            }

            // 4 варианта ответа А/Б/В/Г.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEachIndexed { index, option ->
                    val removed = index in hidden
                    OptionButton(
                        letter = "АБВГ".getOrElse(index) { '•' }.toString(),
                        text = option,
                        state = optionState(index, question.answer, selected, answered),
                        enabled = !answered && !removed,
                        removed = removed,
                        modifier = if (index == selected && outcome == AnswerOutcome.WRONG)
                            Modifier.offset(x = shake.value.dp) else Modifier,
                        onClick = {
                            feedback?.vibrate(Vibe.LIGHT)
                            submit(index)
                        }
                    )
                }
            }

            // Подсказки. 50/50 недоступна на боссе (§6.1) и один раз за вопрос.
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                val fifty = powerUpInventory[PowerUpType.FIFTY_FIFTY.id] ?: 0
                HintButton(
                    PowerUpType.FIFTY_FIFTY, fifty,
                    active = !answered && !usedFifty && !session.boss,
                    modifier = Modifier.weight(1f)
                ) {
                    if (fifty <= 0) buyHintFor = PowerUpType.FIFTY_FIFTY
                    else onConsumeHint(PowerUpType.FIFTY_FIFTY) { ok ->
                        if (ok) {
                            hidden = question.options.indices.filter { it != question.answer }.shuffled().take(2).toSet()
                            usedFifty = true
                            usedHints = true
                        }
                    }
                }
                val freeze = powerUpInventory[PowerUpType.FREEZE_TIME.id] ?: 0
                HintButton(PowerUpType.FREEZE_TIME, freeze, active = !answered && !frozen, modifier = Modifier.weight(1f)) {
                    if (freeze <= 0) buyHintFor = PowerUpType.FREEZE_TIME
                    else onConsumeHint(PowerUpType.FREEZE_TIME) { ok ->
                        if (ok) {
                            frozen = true
                            usedHints = true
                        }
                    }
                }
                val skip = powerUpInventory[PowerUpType.SKIP_QUESTION.id] ?: 0
                HintButton(PowerUpType.SKIP_QUESTION, skip, active = !answered, modifier = Modifier.weight(1f)) {
                    if (skip <= 0) buyHintFor = PowerUpType.SKIP_QUESTION
                    else onConsumeHint(PowerUpType.SKIP_QUESTION) { ok ->
                        if (ok && !answered) {
                            usedHints = true
                            skippedCount++
                            feedback?.stopSpeaking()
                            advance()
                        }
                    }
                }
            }
        }

        // Нижняя панель ответа выезжает снизу.
        AnimatedVisibility(
            visible = answered,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(250)) { it },
            exit = slideOutVertically(tween(200)) { it }
        ) {
            AnswerPanel(
                outcome = outcome,
                correctText = question.correctText,
                explanation = question.explanation,
                coins = coinsPerCorrect,
                onContinue = {
                    if (lives <= 0 && outcome != AnswerOutcome.CORRECT) {
                        if (qIndex + 1 >= qs.size) finish() else showLivesDialog = true
                    } else advance()
                }
            )
        }
    }

    if (showExitConfirm) {
        KidsDialog(
            title = "Выйти?",
            message = "Прогресс уровня не сохранится.",
            confirmText = "Остаться",
            dismissText = "Выйти",
            onConfirm = { showExitConfirm = false },
            onDismiss = {
                showExitConfirm = false
                feedback?.stopSpeaking()
                onExit()
            }
        )
    }

    if (showLivesDialog) {
        val extraLives = powerUpInventory[PowerUpType.EXTRA_LIFE.id] ?: 0
        KidsDialog(
            title = "Жизни закончились",
            message = if (extraLives > 0) "Можно продолжить за доп. жизнь (есть: $extraLives)."
            else "Уровень засчитаем по твоим ответам.",
            confirmText = if (extraLives > 0) "Продолжить за ❤️ Доп. жизнь" else "Завершить",
            dismissText = if (extraLives > 0) "Завершить" else null,
            iconKey = "hint_life",
            iconFallback = "❤️",
            onConfirm = {
                if (extraLives > 0) {
                    onConsumeHint(PowerUpType.EXTRA_LIFE) { ok ->
                        if (ok) {
                            lives = 1
                            showLivesDialog = false
                            advance()
                        }
                    }
                } else {
                    showLivesDialog = false
                    finish()
                }
            },
            onDismiss = {
                showLivesDialog = false
                finish()
            }
        )
    }

    buyHintFor?.let { type ->
        val canAfford = coins >= type.price
        KidsDialog(
            title = type.title,
            message = if (canAfford) "${type.description}. Купить за ${type.price} 🪙?" else "Не хватает монет: нужно ${type.price} 🪙, у тебя $coins 🪙.",
            confirmText = if (canAfford) "Купить за ${type.price} 🪙" else "Понятно",
            dismissText = if (canAfford) "Отмена" else null,
            iconKey = type.iconKey,
            iconFallback = type.emoji,
            confirmColor = if (canAfford) Kids.Coin else Kids.Primary,
            confirmShadow = if (canAfford) Kids.CoinShadow else Kids.PrimaryShadow,
            onConfirm = {
                if (canAfford) onBuyHint(type) { ok -> if (ok) feedback?.play(Sfx.COIN) }
                buyHintFor = null
            },
            onDismiss = { buyHintFor = null }
        )
    }
}

@Composable
private fun TimerChip(timeLeft: Int, frozen: Boolean) {
    val danger = timeLeft <= 5 && !frozen
    val bg = when {
        frozen -> Color(0xFFE1F2FF); danger -> Color(0xFFFFE3E6); else -> Kids.PrimarySoft
    }
    val fg = when {
        frozen -> Kids.Gem; danger -> Kids.Error; else -> Kids.Primary
    }
    val pulse by rememberInfiniteTransition(label = "timer").animateFloat(
        initialValue = 1f, targetValue = if (danger) 0.55f else 1f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "timerPulse"
    )
    Row(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp).alpha(if (danger) pulse else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(if (frozen) "❄" else "⏱", fontSize = 14.sp, color = fg)
        Text("$timeLeft", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = fg)
    }
}

private enum class OptState { IDLE, CORRECT, WRONG, DIMMED }

private fun optionState(index: Int, correct: Int, selected: Int?, answered: Boolean): OptState = when {
    !answered -> OptState.IDLE
    index == correct -> OptState.CORRECT
    index == selected -> OptState.WRONG
    else -> OptState.DIMMED
}

private data class OptionColors(
    val bg: Color, val border: Color, val badgeBg: Color, val badgeFg: Color, val content: Color
)

@Composable
private fun OptionButton(
    letter: String,
    text: String,
    state: OptState,
    enabled: Boolean,
    removed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (bg, border, badgeBg, badgeFg, contentColor) = when (state) {
        OptState.CORRECT -> OptionColors(Kids.SuccessSoft, Kids.Success, Kids.Success, Color.White, Kids.SuccessShadow)
        OptState.WRONG -> OptionColors(Color(0xFFFFE3E6), Kids.Error, Kids.Error, Color.White, Kids.ErrorShadow)
        OptState.DIMMED -> OptionColors(Kids.Card, Kids.CardShadow, Kids.SegmentTrack, Kids.TextMuted, Kids.TextMuted)
        OptState.IDLE -> OptionColors(Kids.Card, Kids.CardShadow, Kids.PrimarySoft, Kids.Primary, Kids.TextPrimary)
    }
    val alpha = when {
        removed -> 0.35f
        state == OptState.DIMMED -> 0.6f
        else -> 1f
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(3.dp, border, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                OptState.CORRECT -> UiIconView(UiIcon.CHECK, tint = badgeFg, size = 18.dp)
                OptState.WRONG -> UiIconView(UiIcon.CLOSE, tint = badgeFg, size = 16.dp)
                else -> Text(letter, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = badgeFg)
            }
        }
        Text(
            text, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
            color = contentColor, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HintButton(
    type: PowerUpType, count: Int, active: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    // Неактивна (45 %), если ответ уже дан / уже использована или подсказок 0 (§5.5).
    val dim = !active || count <= 0
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp)
                .alpha(if (dim) 0.45f else 1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Kids.PrimarySoft)
                .clickable(enabled = active, onClick = onClick)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            GameIcon(type.iconKey, type.emoji, 26.dp)
            Text(
                type.shortTitle, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                color = Kids.PrimaryShadow
            )
        }
        Box(
            modifier = Modifier.align(Alignment.TopEnd).size(22.dp).clip(CircleShape)
                .background(if (count > 0) Kids.Error else Kids.LockedShadow)
                .border(2.dp, Kids.Background, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White) }
    }
}

@Composable
private fun AnswerPanel(
    outcome: AnswerOutcome,
    correctText: String,
    explanation: String?,
    coins: Int,
    onContinue: () -> Unit
) {
    val title = remember(outcome) {
        when (outcome) {
            AnswerOutcome.CORRECT -> listOf("Верно!", "Молодец!", "Супер!").random()
            AnswerOutcome.WRONG -> "Ой, не то!"
            AnswerOutcome.TIMEOUT -> "Время вышло!"
            AnswerOutcome.NONE -> ""
        }
    }
    val (color, shadow) = when (outcome) {
        AnswerOutcome.CORRECT -> Kids.Success to Kids.SuccessShadow
        else -> Kids.Error to Kids.ErrorShadow
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(color)
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
        if (outcome == AnswerOutcome.CORRECT) {
            Text(
                "+$coins монет и +${Economy.XP_PER_CORRECT} XP", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, color = Color.White.copy(alpha = 0.95f)
            )
        } else {
            Text(
                "Правильный ответ: $correctText", fontFamily = RubikFamily,
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White.copy(alpha = 0.95f)
            )
        }
        if (!explanation.isNullOrBlank()) {
            Text(
                explanation, fontFamily = RubikFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(2.dp))
        KidsButton(
            onClick = onContinue, modifier = Modifier.fillMaxWidth(),
            color = Color.White, shadow = shadow, contentColor = color
        ) { Text("Продолжить") }
    }
}
