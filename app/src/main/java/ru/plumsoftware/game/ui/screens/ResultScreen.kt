package ru.plumsoftware.game.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.plumsoftware.game.audio.LocalFeedback
import ru.plumsoftware.game.audio.Sfx
import ru.plumsoftware.game.audio.Vibe
import ru.plumsoftware.game.data.topicById
import ru.plumsoftware.game.ui.LevelResultUi
import ru.plumsoftware.game.ui.components.kids.AvatarCircle
import ru.plumsoftware.game.ui.components.kids.ConfettiOverlay
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsSecondaryButton
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/** Экран результата уровня (ТЗ §5.6). */
@Composable
fun QuizResultScreen(
    result: LevelResultUi,
    avatarId: String,
    goldenFrame: Boolean,
    rewardedReady: Boolean,
    onDoubleReward: () -> Unit,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onToMap: () -> Unit
) {
    val feedback = LocalFeedback.current
    val topic = topicById(result.topicId)
    val title = when (result.stars) {
        3 -> "Потрясающе!"
        2 -> "Отлично!"
        1 -> "Неплохо!"
        else -> "Попробуй ещё!"
    }

    // Звёзды появляются по очереди, «пружина» (§10).
    val starScales = remember(result) { List(3) { Animatable(0f) } }
    LaunchedEffect(result.topicId, result.level, result.correct) {
        delay(250)
        for (i in 0 until 3) {
            if (i < result.stars) {
                launch { starScales[i].animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
                feedback?.play(Sfx.STAR)
            } else {
                starScales[i].snapTo(1f)
            }
            delay(300)
        }
        if (result.stars == 3) feedback?.vibrate(Vibe.SUCCESS)
        if (result.coins > 0) feedback?.play(Sfx.COIN)
    }

    // Счётчики монет и XP «набегают».
    val shownCoins = remember(result) { Animatable(0f) }
    val shownXp = remember(result) { Animatable(0f) }
    LaunchedEffect(result.coins, result.doubled, result.xp) {
        val target = if (result.doubled) result.coins * 2 else result.coins
        launch { shownCoins.animateTo(target.toFloat(), tween(900, easing = FastOutSlowInEasing)) }
        shownXp.animateTo(result.xp.toFloat(), tween(900, easing = FastOutSlowInEasing))
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
        ) {
            AvatarCircle(avatarId, 130.dp, goldenFrame = goldenFrame, borderWidth = 6.dp)

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    val earned = i < result.stars
                    GameIcon(
                        "star", "⭐", if (i == 1) 64.dp else 48.dp,
                        modifier = Modifier
                            .scale(starScales[i].value)
                            .alpha(if (earned) 1f else 0.22f)
                    )
                }
            }
            Text(
                title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp,
                color = Kids.TextPrimary, textAlign = TextAlign.Center
            )
            Text(
                "${topic.name} · уровень ${result.level} " + if (result.stars >= 1) "пройден" else "не пройден",
                fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Kids.TextSecondary,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ResultStat("Верно", "${result.correct}/${result.total}", Kids.Success, Modifier.weight(1f))
                ResultStat("Монеты", "+${shownCoins.value.toInt()}", Kids.CoinText, Modifier.weight(1f))
                ResultStat("Опыт", "+${shownXp.value.toInt()}", Kids.Primary, Modifier.weight(1f))
            }
            if (result.gems > 0) {
                Text(
                    "Бонус за босса: +${result.gems} 💎", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                    fontSize = 13.sp, color = Kids.GemShadow
                )
            }

            // Rewarded «Удвоить награду ×2»: только по нажатию, один раз, не при 0 монет (§8).
            if (result.coins > 0) {
                if (result.doubled) {
                    KidsCard(modifier = Modifier.fillMaxWidth(), background = Kids.SuccessSoft) {
                        Text(
                            "Награда удвоена! 🎉", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, color = Kids.SuccessShadow, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    DoubleRewardCard(coins = result.coins, ready = rewardedReady, onClick = onDoubleReward)
                }
            }

            Spacer(Modifier.height(2.dp))
            if (result.hasNextLevel && result.stars >= 1) {
                KidsButton(text = "Следующий уровень", onClick = onNextLevel, modifier = Modifier.fillMaxWidth())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                KidsSecondaryButton(onClick = onReplay, modifier = Modifier.weight(1f)) { Text("Ещё раз") }
                KidsSecondaryButton(onClick = onToMap, modifier = Modifier.weight(1f)) { Text("На карту") }
            }
        }
        if (result.stars == 3) ConfettiOverlay(seed = result.level * 31 + result.correct)
    }
}

@Composable
private fun DoubleRewardCard(coins: Int, ready: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = ready, onClick = onClick)
    ) {
        KidsCard(modifier = Modifier.fillMaxWidth(), background = Kids.CoinSoft, shadow = Kids.CoinShadow.copy(alpha = 0.5f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().alpha(if (ready) 1f else 0.55f)
            ) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Kids.Coin),
                    contentAlignment = Alignment.Center
                ) { Text("▶", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Удвоить награду ×2", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, color = Kids.TextPrimary
                    )
                    Text(
                        if (ready) "Посмотри видео и получи ещё +$coins 🪙" else "Видео пока нет",
                        fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    KidsCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp, horizontal = 6.dp)) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(label, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Kids.TextSecondary)
        }
    }
}

/** Окно «Новый уровень!» с наградой 50 🪙 × N (§6.5). */
@Composable
fun LevelUpDialog(level: Int, onDismiss: () -> Unit) {
    val feedback = LocalFeedback.current
    LaunchedEffect(level) { feedback?.play(Sfx.LEVEL_UP) }
    ru.plumsoftware.game.ui.components.kids.KidsModal {
        Text("🎉", fontSize = 56.sp)
        Text(
            "Новый уровень!", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
            color = Kids.TextPrimary
        )
        Text(
            "Ты достиг уровня $level", fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp,
            color = Kids.TextSecondary
        )
        Text(
            "+${ru.plumsoftware.game.data.Economy.levelUpReward(level)} 🪙", fontFamily = UnboundedFamily,
            fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Kids.CoinText
        )
        KidsButton(text = "Ура!", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

/** Выдана награда за серию (§5.10). */
@Composable
fun MilestoneDialog(milestone: ru.plumsoftware.game.data.StreakMilestone, onDismiss: () -> Unit) {
    val feedback = LocalFeedback.current
    LaunchedEffect(milestone) { feedback?.play(Sfx.ACHIEVEMENT) }
    ru.plumsoftware.game.ui.components.kids.KidsModal {
        GameIcon("streak_fire", "🔥", 64.dp)
        Text(
            "${milestone.days} дней подряд!", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp, color = Kids.TextPrimary, textAlign = TextAlign.Center
        )
        Text(
            "Награда за серию: ${milestone.title}", fontFamily = RubikFamily, fontWeight = FontWeight.Medium,
            fontSize = 15.sp, color = Kids.TextSecondary, textAlign = TextAlign.Center
        )
        KidsButton(text = "Забрать", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

/** Что выпало из сундука (§6.4). */
@Composable
fun ChestRewardDialog(reward: ru.plumsoftware.game.data.ChestReward, onDismiss: () -> Unit) {
    val feedback = LocalFeedback.current
    LaunchedEffect(reward) { feedback?.play(Sfx.CHEST) }
    val scale = remember(reward) { Animatable(0.4f) }
    LaunchedEffect(reward) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
    val (iconKey, emoji, text) = when (reward) {
        is ru.plumsoftware.game.data.ChestReward.Coins -> Triple("currency_coin", "🪙", "+${reward.amount} монет")
        is ru.plumsoftware.game.data.ChestReward.Gems -> Triple("currency_gem", "💎", "+${reward.amount} кристаллов")
        is ru.plumsoftware.game.data.ChestReward.Hint -> {
            val type = ru.plumsoftware.game.data.PowerUpType.fromId(reward.hintId)
            Triple(type?.iconKey ?: "question", type?.emoji ?: "✨", "Подсказка «${type?.title ?: "?"}»")
        }
    }
    ru.plumsoftware.game.ui.components.kids.KidsModal(onDismissRequest = onDismiss) {
        Text(
            "Сундук открыт!", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
            color = Kids.TextPrimary
        )
        GameIcon(iconKey, emoji, 88.dp, modifier = Modifier.scale(scale.value))
        Text(text, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Kids.CoinText)
        KidsButton(text = "Супер!", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

/** Экран лимита времени (§9.2). */
@Composable
fun TimeLimitScreen(onExtend: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Kids.Background).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        GameIcon("set_time_limit", "⏳", 110.dp)
        Text(
            "На сегодня всё!", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp,
            color = Kids.TextPrimary, textAlign = TextAlign.Center
        )
        Text(
            "Возвращайся завтра — тебя ждут новые вопросы и награды.", fontFamily = RubikFamily,
            fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Kids.TextSecondary, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        KidsSecondaryButton(onClick = onExtend, modifier = Modifier.fillMaxWidth()) { Text("Продлить (для взрослых)") }
    }
}
