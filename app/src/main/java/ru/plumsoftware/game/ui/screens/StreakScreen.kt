package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

private data class StreakReward(val days: Int, val title: String)

private val STREAK_REWARDS = listOf(
    StreakReward(3, "100 🪙"),
    StreakReward(7, "Сундук с подсказками"),
    StreakReward(14, "Персонаж 🦄 Единорог"),
    StreakReward(30, "Золотая рамка + 20 💎"),
)

/** Экран серии (ТЗ §5.10). */
@Composable
fun StreakScreen(
    gameState: GameState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streak = gameState.streakDays
    val best = gameState.streakDays // отдельного «лучшего» пока нет — берём текущий
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val today = ((java.time.LocalDate.now().dayOfWeek.value) - 1).coerceIn(0, 6)
    val nextReward = STREAK_REWARDS.firstOrNull { it.days > streak }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFE7D6), Kids.Background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KidsBackButton(onClick = onBack)
            Text("Серия", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp, color = Kids.TextPrimary)
        }

        Column(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🔥", fontSize = 72.sp)
            Text("$streak", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp, color = Kids.StreakChipText)
            Text("дней подряд", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp, color = Kids.TextSecondary)
            Text(
                buildString {
                    append("Лучшая серия — $best дней.")
                    if (nextReward != null) append(" Ещё ${nextReward.days - streak} до награды.")
                },
                fontFamily = RubikFamily, fontSize = 13.sp, color = Kids.TextMuted,
                textAlign = TextAlign.Center
            )
        }

        // Календарь недели.
        KidsCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { i, d ->
                    val passed = i < today && (today - i) <= streak
                    val isToday = i == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                .background(when {
                                    isToday -> Kids.StreakChip
                                    passed -> Kids.SuccessSoft
                                    else -> Kids.TrackBackground
                                }),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isToday) "🔥" else if (passed) "✓" else "",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Kids.Success)
                        }
                        Text(d, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Kids.TextSecondary)
                    }
                }
            }
        }

        // Награды за серию.
        Text("Награды за серию", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
            fontSize = 18.sp, color = Kids.TextPrimary)
        STREAK_REWARDS.forEach { reward ->
            val achieved = streak >= reward.days
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.alpha(if (achieved || streak >= reward.days - 7) 1f else 0.55f)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Kids.StreakChip),
                        contentAlignment = Alignment.Center) {
                        Text("${reward.days}", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, color = Kids.StreakChipText)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("${reward.days} дней", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 13.sp, color = Kids.TextPrimary)
                        Text(reward.title, fontFamily = RubikFamily, fontSize = 12.sp,
                            color = Kids.TextSecondary)
                    }
                    Text(
                        if (achieved) "Получено" else "через ${reward.days - streak} дн.",
                        fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        color = if (achieved) Kids.Success else Kids.TextMuted
                    )
                }
            }
        }

        // Заморозка серии.
        KidsCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE1F2FF)),
                    contentAlignment = Alignment.Center) { Text("🧊", fontSize = 22.sp) }
                Column(Modifier.weight(1f)) {
                    Text("Заморозка серии", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, color = Kids.TextPrimary)
                    Text("Сохранит серию, если пропустишь день", fontFamily = RubikFamily,
                        fontSize = 12.sp, color = Kids.TextSecondary)
                }
                Text("×0", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = Kids.Gem)
            }
        }
        Box(Modifier.size(12.dp))
    }
}
