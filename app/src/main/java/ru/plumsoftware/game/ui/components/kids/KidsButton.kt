package ru.plumsoftware.game.ui.components.kids

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.UnboundedFamily

typealias RowScopeContent = @Composable () -> Unit

/**
 * «Объёмная» кнопка из ТЗ §3.3: снизу жёсткая тень `0 5px 0 <тень>`.
 * При нажатии кнопка смещается вниз на 4 px, тень уменьшается до 1 px.
 *
 * Реализация: внешний бокс резервирует высоту `лицо + depth`. Тень (задний слой)
 * зафиксирована по нижней границе, лицевой слой съезжает вниз при нажатии.
 */
@Composable
fun KidsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Kids.Primary,
    shadow: Color = Kids.PrimaryShadow,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    depth: Dp = 5.dp,
    cornerRadius: Dp = 20.dp,
    contentPadding: PaddingValues = PaddingValues(vertical = 17.dp, horizontal = 20.dp),
    content: RowScopeContent
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val faceOffset by animateDpAsState(
        targetValue = if (pressed && enabled) depth - 1.dp else 0.dp,
        label = "kidsButtonOffset"
    )
    val shape = RoundedCornerShape(cornerRadius)
    val alpha = if (enabled) 1f else 0.45f

    Box(
        modifier = modifier.padding(bottom = depth),
        contentAlignment = Alignment.TopCenter
    ) {
        // Задний слой — тень, зафиксирована по нижней границе (смещена на depth вниз).
        // matchParentSize берёт размер родителя, который задаётся лицевым слоем.
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = depth)
                .clip(shape)
                .background(shadow.copy(alpha = alpha))
        )
        // Лицевой слой — задаёт размер, съезжает вниз при нажатии.
        Box(
            modifier = Modifier
                .offset(y = faceOffset)
                .clip(shape)
                .background(color.copy(alpha = alpha))
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                ProvideTextStyle(
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) { content() }
                }
            }
        }
    }
}

/** Вторичная (белая) кнопка. */
@Composable
fun KidsSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: RowScopeContent
) = KidsButton(
    onClick = onClick,
    modifier = modifier,
    color = Kids.Card,
    shadow = Kids.CardShadow,
    contentColor = Kids.TextPrimary,
    enabled = enabled,
    depth = 4.dp,
    content = content
)

/** Быстрый вариант с текстом. */
@Composable
fun KidsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Kids.Primary,
    shadow: Color = Kids.PrimaryShadow,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) = KidsButton(
    onClick = onClick,
    modifier = modifier,
    color = color,
    shadow = shadow,
    contentColor = contentColor,
    enabled = enabled
) { Text(text) }
