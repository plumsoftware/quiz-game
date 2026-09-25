package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.components.kids.KidsSecondaryButton
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/** Экран приветствия при первом запуске (ТЗ §5.1). */
@Composable
fun WelcomeScreen(
    onStart: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Kids.BackgroundTop,
                    0.6f to Kids.Background
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically)
        ) {
            // Маскот-лиса в круге с декоративными ❓ и ⭐.
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Kids.Avatar)
                        .border(8.dp, Color.White, CircleShape)
                )
                Text("🦊", fontSize = 110.sp)
                Text("❓", fontSize = 40.sp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp))
                Text("⭐", fontSize = 36.sp, modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 14.dp))
            }
            Text(
                "Викторины\nдля детей",
                fontFamily = UnboundedFamily,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                color = Kids.TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                "Отвечай на вопросы, собирай звёзды и открывай новые миры",
                fontFamily = RubikFamily,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = 15.sp,
                color = Kids.TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        KidsButton(text = "Начать играть", onClick = onStart, modifier = Modifier.fillMaxWidth())
        KidsSecondaryButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
            Text("☁️ Восстановить прогресс")
        }
        Text(
            "Без регистрации и паролей. Прогресс хранится на устройстве, копия — в Google Play Игры / Game Center",
            fontFamily = RubikFamily,
            fontSize = 12.sp,
            color = Kids.TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
