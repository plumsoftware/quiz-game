package ru.plumsoftware.game.ui.screens

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
import ru.plumsoftware.game.data.StreakLogic
import ru.plumsoftware.game.data.StreakMilestone
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import java.time.LocalDate

/** Экран серии (ТЗ §5.10). */
@Composable
fun StreakScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onOpenShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streak = gameState.streakDays
    val best = maxOf(gameState.bestStreak, streak)
    val today = LocalDate.now()
    val week = StreakLogic.week(today, gameState.playedDates, gameState.frozenDates)
    val next = StreakLogic.nextMilestone(streak, gameState.streakRewardsClaimed)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFE2CC), Color(0xFFFFEFE4), Kids.Background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KidsBackButton(onClick = onBack)
            Text("Серия", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Kids.TextPrimary)
        }

        Column(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            GameIcon("streak_fire", "🔥", 96.dp)
            Text("$streak", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, color = Kids.StreakChipText)
            Text(
                "${StreakLogic.daysWord(streak)} подряд", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp, color = Kids.TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                buildString {
                    append("Лучшая серия — $best ${StreakLogic.daysWord(best)}.")
                    if (next != null) {
                        val left = next.days - streak
                        append(" Ещё $left ${StreakLogic.daysWord(left)} до награды «${next.title}».")
                    }
                },
                fontFamily = RubikFamily, fontSize = 13.sp, color = Kids.TextSecondary, textAlign = TextAlign.Center
            )
        }

        KidsCard(modifier = Modifier.fillMaxWidth()) { WeekRow(week, onGradient = false) }

        Text("Награды за серию", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Kids.TextPrimary)
        StreakMilestone.entries.forEach { m ->
            val claimed = m.key in gameState.streakRewardsClaimed
            val left = (m.days - streak).coerceAtLeast(0)
            // Недоступные (следующие после ближайшей) — прозрачность 55 %.
            val reachable = claimed || m == next
            KidsCard(modifier = Modifier.fillMaxWidth().alpha(if (reachable) 1f else 0.55f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Kids.StreakChip),
                        contentAlignment = Alignment.Center
                    ) {
                        when (m) {
                            StreakMilestone.D3 -> GameIcon("currency_coin", "🪙", 30.dp)
                            StreakMilestone.D7 -> GameIcon("map_chest", "🎁", 30.dp)
                            StreakMilestone.D14 -> GameIcon("avatar_unicorn", "🦄", 30.dp)
                            StreakMilestone.D30 -> GameIcon("trophy", "👑", 30.dp)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("${m.days} ${StreakLogic.daysWord(m.days)}", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Kids.TextPrimary)
                        Text(m.title, fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary)
                    }
                    Text(
                        if (claimed) "Получено" else "через $left ${StreakLogic.daysWord(left)}",
                        fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        color = if (claimed) Kids.Success else Kids.TextMuted
                    )
                }
            }
        }

        // Заморозка серии.
        Box(Modifier.clip(RoundedCornerShape(22.dp))) {
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE1F2FF)),
                        contentAlignment = Alignment.Center
                    ) { GameIcon("streak_freeze", "🧊", 30.dp) }
                    Column(Modifier.weight(1f)) {
                        Text("Заморозка серии", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Kids.TextPrimary)
                        Text(
                            "Сама сработает, если пропустишь день", fontFamily = RubikFamily, fontSize = 12.sp,
                            color = Kids.TextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("×${gameState.streakFreezes}", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Kids.GemShadow)
                        Text(
                            "Купить", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Kids.Primary,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenShop)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
