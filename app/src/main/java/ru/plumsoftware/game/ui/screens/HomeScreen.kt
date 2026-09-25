package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameData
import ru.plumsoftware.game.data.GameDifficulty
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.LevelMap
import ru.plumsoftware.game.data.avatarById
import ru.plumsoftware.game.data.topicById
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsChip
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import ru.plumsoftware.game.ui.theme.topicColors
import ru.plumsoftware.game.ui.util.isDailyTaskCompleted

/** Главная — карта уровней (ТЗ §5.3). */
@Composable
fun HomeScreen(
    gameState: GameState,
    tasksProgress: Map<String, Int>,
    onOpenStreak: () -> Unit,
    onOpenTopics: () -> Unit,
    onOpenDailyTasks: () -> Unit,
    onPlayLevel: (Int) -> Unit,
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

    val listState = rememberLazyListState()
    LaunchedEffect(currentNodeIndex) {
        if (currentNodeIndex >= 0) listState.animateScrollToItem(currentNodeIndex + 4)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HomeHeader(gameState, onOpenStreak) }
        item { StreakCard(gameState.streakDays, onOpenStreak) }
        item { DailyTaskCard(tasksProgress, onOpenDailyTasks) }
        item { CurrentTopicButton(topic.emoji, topic.name, difficulty.label, tc.primary, tc.shadow, onOpenTopics) }
        item { Spacer(Modifier.size(4.dp)) }

        items(nodes.size) { index ->
            MapNodeRow(
                node = nodes[index],
                topicColor = tc.primary,
                topicShadow = tc.shadow,
                onClick = { node ->
                    when (node.type) {
                        LevelMap.NodeType.CHEST ->
                            if (node.state == LevelMap.NodeState.AVAILABLE) onOpenChest(node.chestId)
                        else ->
                            if (node.state == LevelMap.NodeState.CURRENT || node.state == LevelMap.NodeState.PASSED)
                                onPlayLevel(node.level)
                    }
                }
            )
        }
        item { Spacer(Modifier.size(24.dp)) }
    }
}

@Composable
private fun HomeHeader(gameState: GameState, onOpenStreak: () -> Unit) {
    val avatar = avatarById(gameState.avatarId)
    val expInLevel = gameState.experience % 100
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Kids.Avatar)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(avatar.emoji, fontSize = 24.sp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Привет, ${gameState.playerName}!",
                fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = Kids.TextPrimary
            )
            Text(
                "Уровень ${gameState.level} · $expInLevel/100 XP",
                fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                color = Kids.TextSecondary
            )
        }
        KidsChip("${gameState.streakDays}", Kids.StreakChip, Kids.StreakChipText, leading = "🔥",
            modifier = Modifier.clickable(onClick = onOpenStreak))
        Spacer(Modifier.width(6.dp))
        KidsChip("${gameState.coins}", Kids.CoinSoft, Kids.CoinText, leading = "🪙")
    }
}

@Composable
private fun StreakCard(streakDays: Int, onClick: () -> Unit) {
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val today = ((java.time.LocalDate.now().dayOfWeek.value) - 1).coerceIn(0, 6)
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
                    Text("$streakDays дней подряд!", fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    Text("Сыграй сегодня, чтобы не потерять серию", fontFamily = RubikFamily,
                        fontSize = 13.sp, color = Color.White.copy(alpha = 0.95f))
                }
                Text("🔥", fontSize = 40.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { i, d ->
                    val passed = i < today && (today - i) <= streakDays
                    val isToday = i == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isToday || passed) Color.White else Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isToday) "🔥" else if (passed) "✓" else "",
                                fontSize = if (isToday) 14.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Kids.StreakChipText
                            )
                        }
                        Text(d, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTaskCard(tasksProgress: Map<String, Int>, onClick: () -> Unit) {
    // Детерминированный выбор задания дня из пула по дате (ТЗ §6.7).
    val tasks = remember { GameData.getDailyTasks() }
    val task = remember(tasks) {
        val idx = (java.time.LocalDate.now().toEpochDay() % tasks.size).toInt()
        tasks[idx]
    }
    val done = isDailyTaskCompleted(task.id, tasksProgress)
    Box(Modifier.clickable(onClick = onClick)) {
        KidsCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(Kids.SuccessSoft),
                    contentAlignment = Alignment.Center
                ) { Text("🎯", fontSize = 26.sp) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Задание дня", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = Kids.TextPrimary)
                        Text("+${task.reward} 🪙", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 12.sp, color = Kids.CoinText)
                    }
                    Text(task.title, fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary)
                    KidsProgressBar(if (done) 1f else 0f, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun CurrentTopicButton(
    emoji: String, name: String, difficultyLabel: String,
    color: Color, shadow: Color, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
    ) {
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("ТЕМА · $difficultyLabel", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                Text(name, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                    fontSize = 17.sp, color = Color.White)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) { Text("Сменить", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                fontSize = 12.sp, color = Color.White) }
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
    val size = when {
        isBoss -> 84.dp
        isCurrent -> 78.dp
        else -> 64.dp
    }
    val (bg, shadow, contentColor) = when {
        isChest -> Triple(
            if (node.state == LevelMap.NodeState.LOCKED) Kids.Locked else Kids.Coin,
            if (node.state == LevelMap.NodeState.LOCKED) Kids.LockedShadow else Kids.CoinShadow,
            Color.White
        )
        isCurrent -> Triple(Kids.Primary, Kids.PrimaryShadow, Color.White)
        node.state == LevelMap.NodeState.PASSED -> Triple(topicColor, topicShadow, Color.White)
        else -> Triple(Kids.Locked, Kids.LockedShadow, Kids.TextMuted)
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Column(
            modifier = Modifier.offset(x = node.xOffset.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isCurrent) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        .background(Color.White).padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("НАЧАТЬ!", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp, color = Kids.Primary)
                }
            }
            Box(
                modifier = Modifier.size(size).padding(bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.matchParentSize().offset(y = 6.dp)
                        .clip(CircleShape).background(shadow)
                )
                Box(
                    modifier = Modifier.matchParentSize().offset(y = 0.dp)
                        .clip(CircleShape).background(bg)
                        .clickable { onClick(node) },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isChest -> Text("🎁", fontSize = 26.sp)
                        isBoss -> Text("👑", fontSize = 32.sp)
                        node.state == LevelMap.NodeState.LOCKED -> Text("🔒", fontSize = 22.sp)
                        else -> Text(
                            "${node.level}", fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isCurrent) 26.sp else 22.sp, color = contentColor
                        )
                    }
                }
            }
            // Звёзды под пройденным уровнем/боссом.
            if (!isChest && node.state == LevelMap.NodeState.PASSED) {
                Text("⭐".repeat(node.stars.coerceIn(0, 3)), fontSize = 12.sp)
            } else {
                Spacer(Modifier.size(14.dp))
            }
        }
    }
}
