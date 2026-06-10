package ru.plumsoftware.game.ui.components.game

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.plumsoftware.game.ui.screens.Achievement
import ru.plumsoftware.game.ui.theme.*

data class AchievementToast(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val reward: Int
)

fun Achievement.toToast(): AchievementToast = AchievementToast(
    id = id,
    title = title,
    description = description,
    emoji = emojiForAchievement(id),
    reward = reward
)

fun emojiForAchievement(id: String): String = when (id) {
    "first_quiz" -> "🎯"
    "quiz_master" -> "⭐"
    "perfect_score" -> "💯"
    "coin_collector" -> "🪙"
    "daily_player" -> "🔥"
    "level_up" -> "📈"
    "time_spent" -> "⏱️"
    "category_explorer" -> "🗺️"
    "quiz_levels" -> "👑"
    "daily_tasks" -> "✅"
    else -> "🏆"
}

@Composable
fun AchievementToastOverlay(
    toast: AchievementToast?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedToast by remember { mutableStateOf<AchievementToast?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(toast?.id) {
        if (toast != null) {
            displayedToast = toast
            visible = true
            delay(3000)
            visible = false
        }
    }

    LaunchedEffect(visible) {
        if (!visible && displayedToast != null) {
            delay(350)
            displayedToast = null
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && displayedToast != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(tween(200)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp)
            .padding(horizontal = 14.dp)
    ) {
        displayedToast?.let { t ->
            AchievementToastBanner(toast = t, onDismiss = { visible = false })
        }
    }
}

@Composable
private fun AchievementToastBanner(toast: AchievementToast, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A2E1A),
        border = BorderStroke(1.5.dp, GameCorrect.copy(alpha = 0.6f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameCorrect.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(toast.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Достижение разблокировано!",
                    style = MaterialTheme.typography.labelSmall,
                    color = GameCorrectText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    toast.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = GameTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    toast.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GameTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (toast.reward > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🪙", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "+${toast.reward}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GameGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Close, null, tint = GameTextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}
