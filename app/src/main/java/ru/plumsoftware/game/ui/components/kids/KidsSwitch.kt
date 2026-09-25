package ru.plumsoftware.game.ui.components.kids

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.ui.theme.Kids

/** Переключатель 52×30, вкл — зелёный #2DC78A, выкл — #DDD6E6, анимация 0,2 с (ТЗ §5.11). */
@Composable
fun KidsSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val trackColor by animateColorAsState(
        if (checked) Kids.Success else Color(0xFFDDD6E6),
        tween(200), label = "track"
    )
    val thumbOffset by animateDpAsState(if (checked) 24.dp else 2.dp, tween(200), label = "thumb")
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(trackColor)
            .clickable(interactionSource = interaction, indication = null) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier.padding(start = thumbOffset).size(26.dp).clip(CircleShape)
                .background(Color.White)
        )
    }
}
