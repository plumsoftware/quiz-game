package ru.plumsoftware.game.ui.components.kids

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily

/** Вкладки нижнего меню (ТЗ §3.3). */
enum class BottomTab(val emoji: String, val label: String) {
    HOME("🏠", "Главная"),
    TOPICS("📚", "Темы"),
    SHOP("🛒", "Магазин"),
    PROFILE("👤", "Профиль"),
}

/**
 * Нижнее меню: 4 вкладки. Активная — фиолетовый текст и светлая подложка `#EFEAFF`,
 * неактивные иконки обесцвечены на 60 % (ТЗ §3.3).
 */
@Composable
fun KidsBottomNav(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Kids.Card)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomTab.entries.forEach { tab ->
            val active = tab == selected
            val interaction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) Kids.PrimarySoft else Color.Transparent)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(tab.emoji, fontSize = 22.sp, modifier = Modifier.alpha(if (active) 1f else 0.6f))
                Text(
                    tab.label,
                    fontFamily = RubikFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = if (active) Kids.Primary else Kids.TextMuted
                )
            }
        }
    }
}
