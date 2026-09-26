package ru.plumsoftware.game.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.LevelMap
import ru.plumsoftware.game.data.StreakLogic
import ru.plumsoftware.game.data.WeekDayState
import ru.plumsoftware.game.data.topicById
import ru.plumsoftware.game.ui.components.kids.AvatarCircle
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsChip
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.components.kids.UiIcon
import ru.plumsoftware.game.ui.components.kids.UiIconView
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors
import java.time.LocalDate

private val WEEK_DAYS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

/** Главная — карта уровней (ТЗ §5.3). */
@Composable
fun HomeScreen(
    gameState: GameState,
    onOpenStreak: () -> Unit,
    onOpenTopics: () -> Unit,
    onClaimQuest: () -> Unit,
    onNodeClick: (LevelMap.Node) -> Unit,
    onOpenChest: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val topic = topicById(gameState.currentTopicId)
    val tc = topicColors(gameState.currentTopicId)
    val difficulty = GameDifficulty.fromId(gameState.currentDifficulty)
    val nodes = remember(gameState.currentTopicId, gameState.currentDifficulty, gameState.levelStars, gameState.openedChests) {
        LevelMap.build(gameState.currentTopicId, gameState.currentDifficulty, gameState.levelStars, gameState.openedChests)
    }
    val currentNodeIndex = nodes.indexOfFirst { it.state == LevelMap.NodeState.CURRENT }

    // При открытии экрана карта прокручивается к текущему уровню (§5.3).
    val headerItems = 4
    val listState = rememberLazyListState()
    LaunchedEffect(currentNodeIndex, gameState.currentTopicId, gameState.currentDifficulty) {
        if (currentNodeIndex > 1) listState.animateScrollToItem((currentNodeIndex + headerItems - 1).coerceAtLeast(0))
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HomeHeader(gameState, onOpenStreak) }
        item { StreakCard(gameState, onOpenStreak) }
        item { DailyQuestCard(gameState, onClaimQuest) }
        item {
            CurrentTopicButton(
                iconKey = "topic_${topic.id}", emoji = topic.emoji, name = topic.name,
                difficultyLabel = difficulty.label, color = tc.primary, shadow = tc.shadow, onClick = onOpenTopics
            )
        }
        items(nodes.size) { index ->
            MapNodeRow(
                node = nodes[index],
                topicColor = tc.primary,
                topicShadow = tc.shadow,
                onClick = { node ->
                    if (node.type == LevelMap.NodeType.CHEST) {
                        if (node.state == LevelMap.NodeState.AVAILABLE) onOpenChest(node.chestId)
                        else if (node.state == LevelMap.NodeState.LOCKED) onNodeClick(node)
                    } else onNodeClick(node)
                }
            )
        }
        item { Spacer(Modifier.size(24.dp)) }
    }
}

@Composable
private fun HomeHeader(gameState: GameState, onOpenStreak: () -> Unit) {
    val progress = gameState.levelProgress
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AvatarCircle(gameState.avatarId, 46.dp, goldenFrame = gameState.goldenFrame)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Привет, ${gameState.playerName}!",
                fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = Kids.TextPrimary, maxLines = 1
            )
            Text(
                "Уровень ${progress.level} · ${progress.xpInLevel}/${progress.xpNeeded} XP",
                fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                color = Kids.TextSecondary
            )
        }
        KidsChip(
            "${gameState.streakDays}", Kids.StreakChip, Kids.StreakChipText, leading = "🔥",
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onOpenStreak)
        )
        Spacer(Modifier.width(6.dp))
        KidsChip("${gameState.coins}", Kids.CoinSoft, Kids.CoinText, leading = "🪙")
    }
}

/** Карточка серии: градиент, «N дней подряд!», 7 кружков Пн–Вс (§5.3). */
@Composable
fun StreakCard(gameState: GameState, onClick: () -> Unit) {
    val today = LocalDate.now()
    val week = StreakLogic.week(today, gameState.playedDates, gameState.frozenDates)
    val streak = gameState.streakDays
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Kids.StreakFrom, Kids.StreakTo)))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "$streak ${StreakLogic.daysWord(streak)} подряд!", fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White
                    )
                    Text(
                        if (gameState.lastPlayedDate == today) "Сегодня день засчитан — так держать!"
                        else "Пройди уровень сегодня, чтобы не потерять серию",
                        fontFamily = RubikFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.95f)
                    )
                }
                GameIcon("streak_fire", "🔥", 44.dp)
            }
            WeekRow(week, onGradient = true)
        }
    }
}

/** Ряд Пн–Вс: пройдено ✓, сегодня 🔥 с пунктирной рамкой, будущие пустые, заморозка 🧊. */
@Composable
fun WeekRow(week: List<WeekDayState>, onGradient: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        week.forEachIndexed { i, state ->
            val filled = state == WeekDayState.PLAYED || state == WeekDayState.TODAY_PLAYED
            val isToday = state == WeekDayState.TODAY || state == WeekDayState.TODAY_PLAYED
            val bg = when {
                filled -> if (onGradient) Color.White else Kids.SuccessSoft
                state == WeekDayState.FROZEN -> Color(0xFFE1F2FF)
                else -> if (onGradient) Color.White.copy(alpha = 0.25f) else Kids.TrackBackground
            }
            val dashColor = if (onGradient) Color.White else Kids.StreakChipText
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .then(
                            if (isToday) Modifier.drawBehind {
                                drawRoundRect(
                                    color = dashColor,
                                    cornerRadius = CornerRadius(size.minDimension / 2),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
                                    )
                                )
                            } else Modifier
                        )
                        .padding(if (isToday) 3.dp else 0.dp)
                        .clip(CircleShape)
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isToday -> Text("🔥", fontSize = 13.sp)
                        filled -> UiIconView(UiIcon.CHECK, tint = if (onGradient) Kids.StreakChipText else Kids.Success, size = 16.dp)
                        state == WeekDayState.FROZEN -> Text("🧊", fontSize = 13.sp)
                        else -> {}
                    }
                }
                Text(
                    WEEK_DAYS[i], fontFamily = RubikFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = if (onGradient) Color.White else Kids.TextSecondary
                )
            }
        }
    }
}

/** Задание дня: название, прогресс, награда; забирается нажатием на выполненную карточку (§6.7). */
@Composable
private fun DailyQuestCard(gameState: GameState, onClaim: () -> Unit) {
    val today = LocalDate.now()
    val quest = gameState.todayQuest(today)
    val progress = gameState.questProgressFor(today)
    val claimed = gameState.questClaimedFor(today)
    val done = progress >= quest.target
    val claimable = done && !claimed
    val pulse by rememberInfiniteTransition(label = "quest").animateFloat(
        initialValue = 1f, targetValue = if (claimable) 1.03f else 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "questPulse"
    )
    Box(
        Modifier
            .scale(pulse)
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = claimable, onClick = onClaim)
    ) {
        KidsCard(
            modifier = Modifier.fillMaxWidth(),
            background = if (claimable) Kids.SuccessSoft else Kids.Card
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Kids.CoinSoft),
                    contentAlignment = Alignment.Center
                ) { GameIcon("daily_quest", quest.emoji, 34.dp) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Задание дня", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = Kids.TextPrimary
                        )
                        Text(
                            when {
                                claimed -> "Получено ✓"
                                claimable -> "Забрать +${quest.reward} ${quest.currency.emoji}"
                                else -> "+${quest.reward} ${quest.currency.emoji}"
                            },
                            fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                            color = if (claimed || claimable) Kids.SuccessShadow else Kids.CoinText
                        )
                    }
                    Text(quest.title, fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KidsProgressBar(progress.toFloat() / quest.target, Modifier.weight(1f))
                        Text(
                            "$progress/${quest.target}", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 11.sp, color = Kids.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentTopicButton(
    iconKey: String, emoji: String, name: String, difficultyLabel: String,
    color: Color, shadow: Color, onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
        Box(
            modifier = Modifier.matchParentSize().offset(y = 5.dp)
                .clip(RoundedCornerShape(20.dp)).background(shadow)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(color)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameIcon(iconKey, emoji, 40.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Тема · $difficultyLabel", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    name, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                    fontSize = 17.sp, color = Color.White
                )
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    "Сменить", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MapNodeRow(
    node: LevelMap.Node,
    topicColor: Color,
    topicShadow: Color,
    onClick: (LevelMap.Node) -> Unit
) {
    val isCurrent = node.state == LevelMap.NodeState.CURRENT
    val isBoss = node.type == LevelMap.NodeType.BOSS
    val isChest = node.type == LevelMap.NodeType.CHEST
    val locked = node.state == LevelMap.NodeState.LOCKED
    val size = when {
        isBoss -> 84.dp
        isCurrent -> 78.dp
        else -> 64.dp
    }
    val (bg, shadow) = when {
        isChest && node.state == LevelMap.NodeState.AVAILABLE -> Kids.Coin to Kids.CoinShadow
        isChest && node.state == LevelMap.NodeState.PASSED -> Kids.CoinSoft to Kids.CoinShadow.copy(alpha = 0.4f)
        isCurrent -> Kids.Primary to Kids.PrimaryShadow
        node.state == LevelMap.NodeState.PASSED -> topicColor to topicShadow
        else -> Kids.Locked to Kids.LockedShadow
    }

    // Пульсация текущего узла, подсказки «НАЧАТЬ!» и доступного сундука (§5.3, §10).
    val transition = rememberInfiniteTransition(label = "node")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent || (isChest && node.state == LevelMap.NodeState.AVAILABLE)) 1.08f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "nodePulse"
    )

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Column(
            modifier = Modifier.offset(x = node.xOffset.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .scale(pulse)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(BorderStroke(2.dp, Kids.PrimarySoft), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "НАЧАТЬ!", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp, color = Kids.Primary
                    )
                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(pulse)) {
                // Кольцо-подсветка вокруг текущего уровня.
                if (isCurrent) {
                    Box(
                        Modifier.size(size + 16.dp).clip(CircleShape)
                            .background(Kids.Primary.copy(alpha = 0.16f))
                    )
                }
                Box(modifier = Modifier.size(size).padding(bottom = 6.dp)) {
                    Box(
                        modifier = Modifier.matchParentSize().offset(y = 6.dp)
                            .clip(CircleShape).background(shadow)
                    )
                    Box(
                        modifier = Modifier.matchParentSize()
                            .clip(CircleShape).background(bg)
                            .clickable { onClick(node) },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isChest -> GameIcon(
                                "map_chest", "🎁", 38.dp,
                                modifier = Modifier.alpha(if (node.state == LevelMap.NodeState.PASSED) 0.5f else if (locked) 0.6f else 1f)
                            )
                            isBoss && !locked -> GameIcon("map_boss", "👑", 46.dp)
                            locked -> GameIcon("map_lock", "🔒", 28.dp, modifier = Modifier.alpha(0.8f))
                            else -> Text(
                                "${node.level}", fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isCurrent) 26.sp else 22.sp, color = Color.White
                            )
                        }
                    }
                }
            }
            // Звёзды под пройденным уровнем/боссом.
            if (!isChest && node.state == LevelMap.NodeState.PASSED) {
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    repeat(3) { i ->
                        GameIcon(
                            "star", "⭐", 16.dp,
                            modifier = Modifier.alpha(if (i < node.stars) 1f else 0.25f)
                        )
                    }
                }
            } else if (isChest && node.state == LevelMap.NodeState.PASSED) {
                Text("открыт", fontFamily = RubikFamily, fontSize = 10.sp, color = Kids.TextMuted)
            } else {
                Spacer(Modifier.size(16.dp))
            }
        }
    }
}
