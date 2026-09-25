package ru.plumsoftware.game.ui.components.kids

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/** Белая карточка с нижней тенью `0 4px 0 #EDE4D6` (ТЗ §3.3). */
@Composable
fun KidsCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    depth: Dp = 4.dp,
    background: Color = Kids.Card,
    shadow: Color = Kids.CardShadow,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(modifier = modifier.padding(bottom = depth), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = depth)
                .clip(shape)
                .background(shadow)
        )
        Box(
            modifier = Modifier
                .clip(shape)
                .background(background)
                .padding(contentPadding)
        ) { content() }
    }
}

/**
 * Кнопка «Назад»: шеврон `<` в белом квадрате 40×40, скругление 13 (ТЗ §3.3).
 * Одинакова на всех экранах.
 */
@Composable
fun KidsBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    KidsButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        color = Kids.Card,
        shadow = Kids.CardShadow,
        contentColor = Kids.TextPrimary,
        depth = 3.dp,
        cornerRadius = 13.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text("‹", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

/** Плашка-чип (монеты/серия) в шапке. */
@Composable
fun KidsChip(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    leading: String? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) Text(leading, fontSize = 13.sp)
        Text(
            text,
            fontFamily = UnboundedFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = contentColor
        )
    }
}

/** Прогресс-бар со скруглением и цветной заливкой (ТЗ §3). */
@Composable
fun KidsProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    track: Color = Kids.TrackBackground,
    fill: Color = Kids.Success
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(fill)
        )
    }
}
