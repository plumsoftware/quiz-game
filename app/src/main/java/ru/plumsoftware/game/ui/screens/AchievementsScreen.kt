package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.AchievementProgress
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsModal
import ru.plumsoftware.game.ui.components.kids.KidsProgressBar
import ru.plumsoftware.game.ui.components.kids.desaturate
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Достижения (ТЗ §5.9): сводка + сетка 3 колонки, тап открывает окно с прогрессом и датой. */
@Composable
fun AchievementsScreen(
    achievements: List<AchievementProgress>,
    onBack: () -> Unit
) {
    val unlocked = achievements.count { it.unlockedAt != null }
    var details by remember { mutableStateOf<AchievementProgress?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KidsBackButton(onClick = onBack)
                    Text("Достижения", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Kids.TextPrimary)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                KidsCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Открыто $unlocked из ${achievements.size}", fontFamily = RubikFamily,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Kids.TextPrimary
                        )
                        KidsProgressBar(
                            if (achievements.isEmpty()) 0f else unlocked.toFloat() / achievements.size,
                            Modifier.fillMaxWidth(), fill = Kids.Primary
                        )
                    }
                }
            }
            items(achievements, key = { it.def.id }) { a -> AchievementBadge(a) { details = a } }
        }

        details?.let { a -> AchievementDialog(a) { details = null } }
    }
}

@Composable
private fun AchievementBadge(a: AchievementProgress, onClick: () -> Unit) {
    val open = a.unlockedAt != null
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Kids.Card)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp)
            .alpha(if (open) 1f else 0.45f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GameIcon(a.def.iconKey, a.def.emoji, 48.dp, colorFilter = if (open) null else desaturate(1f))
        Text(
            a.def.title, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
            color = Kids.TextPrimary, textAlign = TextAlign.Center, maxLines = 1
        )
        Text(
            a.def.condition, fontFamily = RubikFamily, fontSize = 10.sp, color = Kids.TextSecondary,
            textAlign = TextAlign.Center, maxLines = 2, minLines = 2, lineHeight = 12.sp
        )
    }
}

@Composable
private fun AchievementDialog(a: AchievementProgress, onDismiss: () -> Unit) {
    KidsModal(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)).background(Kids.PrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            GameIcon(a.def.iconKey, a.def.emoji, 60.dp, colorFilter = if (a.unlockedAt != null) null else desaturate(1f))
        }
        Text(a.def.title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Kids.TextPrimary, textAlign = TextAlign.Center)
        Text(a.def.condition, fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Kids.TextSecondary, textAlign = TextAlign.Center)
        KidsProgressBar(a.fraction, Modifier.fillMaxWidth(), fill = Kids.Primary)
        Text("${a.current}/${a.target}", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Kids.TextMuted)
        Text(
            (if (a.unlockedAt != null) "Награда получена: " else "Награда: ") + "+${a.def.reward} ${a.def.currency.emoji}",
            fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Kids.CoinText
        )
        a.unlockedAt?.let { date ->
            Text("Получено ${formatDate(date)}", fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextMuted)
        }
        KidsButton(text = "Закрыть", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

private fun formatDate(iso: String): String = try {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
} catch (e: Exception) {
    iso
}
