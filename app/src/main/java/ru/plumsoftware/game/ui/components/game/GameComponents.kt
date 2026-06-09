package ru.plumsoftware.game.ui.components.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.theme.*

@Composable
fun GameTopBar(coins: Int, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GameSurfaceVariant)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Викторины",
            style = MaterialTheme.typography.titleLarge,
            color = GameTextPrimary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoinBadge(coins = coins)
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Outlined.Settings, "Настройки", tint = GameTextPrimary)
            }
        }
    }
}

@Composable
fun CoinBadge(coins: Int) {
    val animatedCoins by animateIntAsState(targetValue = coins, animationSpec = tween(500), label = "coins")
    Surface(
        shape = RoundedCornerShape(50),
        color = GameGold.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, GameGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🪙", style = MaterialTheme.typography.labelMedium)
            Text(
                text = animatedCoins.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = GameGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StreakHeroBanner(streak: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GamePurple.copy(alpha = 0.12f))
            .border(1.dp, GamePurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (streak > 0) {
                    Text(
                        "🔥 $streak дней подряд",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GameStreak
                    )
                    Text(
                        "Не прерывай серию!",
                        style = MaterialTheme.typography.bodySmall,
                        color = GameTextMuted
                    )
                } else {
                    Text(
                        "Начни серию сегодня!",
                        style = MaterialTheme.typography.titleMedium,
                        color = GameTextPrimary
                    )
                    Text(
                        "Играй каждый день 🎯",
                        style = MaterialTheme.typography.bodySmall,
                        color = GameTextMuted
                    )
                }
            }
            Text(
                text = when {
                    streak >= 7 -> "🏆"
                    streak >= 3 -> "🔥"
                    else -> "🧠"
                },
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
fun DailyProgressBar(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(600), label = "daily")

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Прогресс дня", style = MaterialTheme.typography.labelMedium, color = GameTextMuted)
            Text("$completed / $total", style = MaterialTheme.typography.labelMedium, color = GamePurpleLight)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(GameBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(GamePurple, GamePurpleLight)))
            )
        }
    }
}

@Composable
fun QuickPlayButton(onPlay: () -> Unit) {
    Button(
        onClick = onPlay,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GamePurple),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Играть", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryTag(name: String, emoji: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = GamePurple.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, GamePurple.copy(alpha = 0.3f))
    ) {
        Text(
            text = "$emoji $name",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = GamePurpleLight
        )
    }
}

@Composable
fun AnimatedGameProgressBar(progress: Float, color: Color = GamePurple) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(400), label = "progress")
    Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(GameBorder)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.7f))))
        )
    }
}

@Composable
fun RewardChip(label: String, emoji: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Text(label, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PulsingEmoji(emoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "emojiScale"
    )
    Text(
        text = emoji,
        style = MaterialTheme.typography.displayLarge,
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    )
}

enum class AnswerState { NONE, CORRECT, WRONG, MISSED }

data class AnswerOption(val id: Int, val text: String, val isCorrect: Boolean)
