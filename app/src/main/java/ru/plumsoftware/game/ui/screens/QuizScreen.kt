package ru.plumsoftware.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.data.Question
import ru.plumsoftware.game.data.topicById
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors

private enum class AnswerOutcome { NONE, CORRECT, WRONG, TIMEOUT }

/** Экран прохождения викторины (ТЗ §5.5). */
@Composable
fun QuizScreen(
    topicId: String,
    difficulty: GameDifficulty,
    isBoss: Boolean,
    questions: List<Question>,
    coins: Int,
    powerUpInventory: Map<String, Int>,
    onExit: () -> Unit,
    onComplete: (correct: Int, total: Int) -> Unit,
    onConsumePowerUp: (PowerUpType, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val topic = topicById(topicId)
    val tc = topicColors(topicId)
    val neededCount = if (isBoss) 10 else 5
    val qs = remember(questions) { questions.take(neededCount).ifEmpty { questions } }

    if (qs.isEmpty()) {
        // Нет вопросов — выходим (side-effect вне композиции).
        androidx.compose.runtime.LaunchedEffect(Unit) { onComplete(0, 0) }
        return
    }

    var qIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(difficulty.lives) }

    // Состояние текущего вопроса.
    var selected by remember(qIndex) { mutableStateOf<Int?>(null) }
    var outcome by remember(qIndex) { mutableStateOf(AnswerOutcome.NONE) }
    var timeLeft by remember(qIndex) { mutableIntStateOf(difficulty.secondsPerQuestion) }
    var frozen by remember(qIndex) { mutableStateOf(false) }
    var hidden by remember(qIndex) { mutableStateOf(setOf<Int>()) }
    var usedFifty by remember(qIndex) { mutableStateOf(false) }

    var showExitConfirm by remember { mutableStateOf(false) }
    var showLivesDialog by remember { mutableStateOf(false) }
    var buyHintFor by remember { mutableStateOf<PowerUpType?>(null) }

    val question = qs[qIndex]
    val answered = outcome != AnswerOutcome.NONE

    fun finish() = onComplete(correctCount, qs.size)

    fun advance() {
        if (qIndex + 1 >= qs.size) finish() else qIndex++
    }

    fun submit(optionIndex: Int?) {
        if (answered) return
        val correct = optionIndex != null && optionIndex == question.correctAnswer
        selected = optionIndex
        outcome = when {
            optionIndex == null -> AnswerOutcome.TIMEOUT
            correct -> AnswerOutcome.CORRECT
            else -> AnswerOutcome.WRONG
        }
        if (correct) correctCount++ else lives--
    }

    // Таймер вопроса (ТЗ §5.5): идёт, пока не отвечено и не заморожен.
    androidx.compose.runtime.LaunchedEffect(qIndex, answered, frozen) {
        while (!answered && !frozen && timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0 && !answered) submit(null)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Верхняя панель: выход, прогресс, жизни.
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                        .background(Kids.SegmentTrack).clickable { showExitConfirm = true },
                    contentAlignment = Alignment.Center
                ) { Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Kids.TextSecondary) }
                Box(
                    modifier = Modifier.weight(1f).height(14.dp).clip(RoundedCornerShape(8.dp))
                        .background(Kids.TrackBackground)
                ) {
                    Box(
                        Modifier.fillMaxWidth((qIndex + if (answered) 1 else 0).toFloat() / qs.size)
                            .height(14.dp).clip(RoundedCornerShape(8.dp)).background(Kids.Success)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(difficulty.lives) { i ->
                        Text("❤️", fontSize = 18.sp, modifier = Modifier.alpha(if (i < lives) 1f else 0.25f))
                    }
                }
            }

            // Строка: номер вопроса и таймер.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Вопрос ${qIndex + 1} из ${qs.size} · ${topic.name}",
                    fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = Kids.TextSecondary)
                TimerChip(timeLeft, frozen)
            }

            // Карточка вопроса.
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
                    .background(Kids.Card).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(18.dp))
                        .background(tc.cardBg),
                    contentAlignment = Alignment.Center
                ) { Text(topic.emoji, fontSize = 66.sp) }
                Text(question.question, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                    fontSize = 19.sp, lineHeight = 25.sp, textAlign = TextAlign.Center,
                    color = Kids.TextPrimary)
            }

            // Варианты ответа.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEachIndexed { index, option ->
                    if (index !in hidden) {
                        OptionButton(
                            letter = "АБВГ".getOrElse(index) { '•' }.toString(),
                            text = option,
                            state = optionState(index, question.correctAnswer, selected, answered),
                            enabled = !answered,
                            onClick = { submit(index) }
                        )
                    } else {
                        Spacer(Modifier.height(0.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Подсказки (недоступны после ответа; 50/50 недоступна на боссе — §6.1).
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HintButton("✂️", "50/50", powerUpInventory[PowerUpType.FIFTY_FIFTY.id] ?: 0,
                    enabled = !answered && !usedFifty && !isBoss, modifier = Modifier.weight(1f)) {
                    val count = powerUpInventory[PowerUpType.FIFTY_FIFTY.id] ?: 0
                    if (count <= 0) buyHintFor = PowerUpType.FIFTY_FIFTY
                    else onConsumePowerUp(PowerUpType.FIFTY_FIFTY) { ok ->
                        if (ok) {
                            val wrong = question.options.indices
                                .filter { it != question.correctAnswer }.shuffled().take(2)
                            hidden = wrong.toSet()
                            usedFifty = true
                        }
                    }
                }
                HintButton("⏸️", "Стоп", powerUpInventory[PowerUpType.FREEZE_TIME.id] ?: 0,
                    enabled = !answered && !frozen, modifier = Modifier.weight(1f)) {
                    val count = powerUpInventory[PowerUpType.FREEZE_TIME.id] ?: 0
                    if (count <= 0) buyHintFor = PowerUpType.FREEZE_TIME
                    else onConsumePowerUp(PowerUpType.FREEZE_TIME) { ok -> if (ok) frozen = true }
                }
                HintButton("⏭️", "Пропуск", powerUpInventory[PowerUpType.SKIP_QUESTION.id] ?: 0,
                    enabled = !answered, modifier = Modifier.weight(1f)) {
                    val count = powerUpInventory[PowerUpType.SKIP_QUESTION.id] ?: 0
                    if (count <= 0) buyHintFor = PowerUpType.SKIP_QUESTION
                    else onConsumePowerUp(PowerUpType.SKIP_QUESTION) { ok -> if (ok) advance() }
                }
            }
        }

        // Нижняя панель ответа (выезжает снизу).
        AnimatedVisibility(
            visible = answered,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            AnswerPanel(
                outcome = outcome,
                correctText = question.options.getOrNull(question.correctAnswer) ?: "",
                onContinue = { if (lives <= 0 && outcome != AnswerOutcome.CORRECT) showLivesDialog = true else advance() }
            )
        }
    }

    if (showExitConfirm) {
        KidsDialog(
            title = "Выйти?",
            message = "Прогресс уровня не сохранится.",
            confirmText = "Выйти",
            dismissText = "Остаться",
            onConfirm = onExit,
            onDismiss = { showExitConfirm = false }
        )
    }

    if (showLivesDialog) {
        val hasExtraLife = (powerUpInventory[PowerUpType.EXTRA_LIFE.id] ?: 0) > 0
        KidsDialog(
            title = "Жизни закончились",
            message = if (hasExtraLife) "Продолжить за доп. жизнь?" else "Уровень будет засчитан по ответам.",
            confirmText = if (hasExtraLife) "❤️ Доп. жизнь" else "Завершить",
            dismissText = if (hasExtraLife) "Завершить" else null,
            onConfirm = {
                if (hasExtraLife) {
                    onConsumePowerUp(PowerUpType.EXTRA_LIFE) { ok ->
                        if (ok) { lives = 1; showLivesDialog = false; advance() }
                    }
                } else finish()
            },
            onDismiss = { showLivesDialog = false; finish() }
        )
    }

    buyHintFor?.let { type ->
        KidsDialog(
            title = "Подсказки закончились",
            message = "Купить «${type.title}» можно в магазине за ${type.price} 🪙.",
            confirmText = "Понятно",
            dismissText = null,
            onConfirm = { buyHintFor = null },
            onDismiss = { buyHintFor = null }
        )
    }
}

@Composable
private fun TimerChip(timeLeft: Int, frozen: Boolean) {
    val danger = timeLeft <= 5 && !frozen
    val bg = when { frozen -> Color(0xFFE1F2FF); danger -> Color(0xFFFFE3E6); else -> Kids.PrimarySoft }
    val fg = when { frozen -> Kids.Gem; danger -> Kids.Error; else -> Kids.Primary }
    // лёгкая пульсация в опасной зоне
    val pulse by rememberInfiniteTransition(label = "timer").animateFloat(
        initialValue = 1f, targetValue = if (danger) 0.55f else 1f,
        animationSpec = infiniteRepeatable(tween(500)), label = "timerPulse"
    )
    Row(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp).alpha(if (danger) pulse else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(if (frozen) "❄" else "⏱", fontSize = 14.sp)
        Text("$timeLeft", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
            fontSize = 14.sp, color = fg)
    }
}

private enum class OptState { IDLE, CORRECT, WRONG, DIMMED }

private fun optionState(index: Int, correct: Int, selected: Int?, answered: Boolean): OptState = when {
    !answered -> OptState.IDLE
    index == correct -> OptState.CORRECT
    index == selected -> OptState.WRONG
    else -> OptState.DIMMED
}

@Composable
private fun OptionButton(
    letter: String,
    text: String,
    state: OptState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val (bg, border, badgeBg, badgeFg, contentColor) = when (state) {
        OptState.CORRECT -> OptionColors(Kids.SuccessSoft, Kids.Success, Kids.Success, Color.White, Kids.SuccessShadow)
        OptState.WRONG -> OptionColors(Color(0xFFFFE3E6), Kids.Error, Kids.Error, Color.White, Kids.ErrorShadow)
        OptState.DIMMED -> OptionColors(Kids.Card, Kids.CardShadow, Kids.SegmentTrack, Kids.TextMuted, Kids.TextMuted)
        OptState.IDLE -> OptionColors(Kids.Card, Kids.CardShadow, Kids.PrimarySoft, Kids.Primary, Kids.TextPrimary)
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(3.dp, border, RoundedCornerShape(18.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (state == OptState.DIMMED) 0.6f else 1f)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(badgeBg),
            contentAlignment = Alignment.Center
        ) { Text(letter, fontFamily = UnboundedFamily, fontSize = 13.sp, color = badgeFg) }
        Text(text, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
            color = contentColor)
    }
}

private data class OptionColors(
    val bg: Color, val border: Color, val badgeBg: Color, val badgeFg: Color, val content: Color
)

@Composable
private fun HintButton(
    icon: String, label: String, count: Int, enabled: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Kids.PrimarySoft)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .alpha(if (enabled) 1f else 0.45f)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                color = Kids.PrimaryShadow)
        }
        Box(
            modifier = Modifier.align(Alignment.TopEnd).size(20.dp).clip(CircleShape)
                .background(Kids.Error).border(2.dp, Kids.Background, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White) }
    }
}

@Composable
private fun AnswerPanel(outcome: AnswerOutcome, correctText: String, onContinue: () -> Unit) {
    val (color, shadow, title) = when (outcome) {
        AnswerOutcome.CORRECT -> Triple(Kids.Success, Kids.SuccessShadow,
            listOf("Верно!", "Молодец!", "Супер!").random())
        AnswerOutcome.WRONG -> Triple(Kids.Error, Kids.ErrorShadow, "Ой, не то!")
        AnswerOutcome.TIMEOUT -> Triple(Kids.Error, Kids.ErrorShadow, "Время вышло!")
        AnswerOutcome.NONE -> Triple(Kids.Success, Kids.SuccessShadow, "")
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(color).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
            color = Color.White)
        if (outcome == AnswerOutcome.CORRECT) {
            Text("+10 монет и +8 XP", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, color = Color.White.copy(alpha = 0.95f))
        } else {
            Text("Правильный ответ: $correctText", fontFamily = RubikFamily,
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White.copy(alpha = 0.95f))
        }
        KidsButton(onClick = onContinue, modifier = Modifier.fillMaxWidth(),
            color = Color.White, shadow = Color.White.copy(alpha = 0.5f), contentColor = color) {
            Text("Продолжить")
        }
    }
}

@Composable
private fun KidsDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0x99000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Kids.Card).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
                color = Kids.TextPrimary, textAlign = TextAlign.Center)
            Text(message, fontFamily = RubikFamily, fontSize = 14.sp, color = Kids.TextSecondary,
                textAlign = TextAlign.Center)
            KidsButton(text = confirmText, onClick = onConfirm, modifier = Modifier.fillMaxWidth())
            if (dismissText != null) {
                Text(dismissText, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = Kids.TextSecondary,
                    modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
            }
        }
    }
}

/** Экран результата уровня (ТЗ §5.6). */
@Composable
fun QuizResultScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    coinsEarned: Int,
    currentLevel: Int,
    onBackToHome: () -> Unit,
    onPlayAgain: () -> Unit,
    displayAds: Boolean = true,
    topicId: String = "animals"
) {
    val topic = topicById(topicId)
    val stars = when {
        totalQuestions <= 0 -> 0
        correctAnswers >= totalQuestions -> 3
        correctAnswers.toFloat() / totalQuestions >= 0.6f -> 2
        correctAnswers >= 1 -> 1
        else -> 0
    }
    val title = when (stars) {
        3 -> "Потрясающе!"
        2 -> "Отлично!"
        1 -> "Неплохо!"
        else -> "Попробуй ещё!"
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier.size(130.dp).clip(CircleShape).background(Kids.Avatar)
                .border(6.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(topic.emoji, fontSize = 72.sp) }

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                val earned = i < stars
                Text("⭐", fontSize = if (i == 1) 52.sp else 40.sp,
                    modifier = Modifier.alpha(if (earned) 1f else 0.25f))
            }
        }
        Text(title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp,
            color = Kids.TextPrimary)
        Text("${topic.name} · уровень $currentLevel пройден", fontFamily = RubikFamily,
            fontSize = 14.sp, color = Kids.TextSecondary)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ResultStat("Верно", "$correctAnswers/$totalQuestions", Kids.Success, Modifier.weight(1f))
            ResultStat("Монеты", "+$coinsEarned", Kids.CoinText, Modifier.weight(1f))
            ResultStat("Опыт", "+${correctAnswers * 8}", Kids.Primary, Modifier.weight(1f))
        }

        Spacer(Modifier.height(4.dp))
        KidsButton(text = "Следующий уровень", onClick = onBackToHome, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            KidsButton(onClick = onPlayAgain, modifier = Modifier.weight(1f),
                color = Kids.Card, shadow = Kids.CardShadow, contentColor = Kids.TextPrimary) {
                Text("Ещё раз")
            }
            KidsButton(onClick = onBackToHome, modifier = Modifier.weight(1f),
                color = Kids.Card, shadow = Kids.CardShadow, contentColor = Kids.TextPrimary) {
                Text("На карту")
            }
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Kids.Card).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
            color = color)
        Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
            color = Kids.TextSecondary)
    }
}
