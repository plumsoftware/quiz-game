package ru.plumsoftware.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun RobloxIcon(modifier: Modifier = Modifier) {
    val brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF9AA4AD),
            Color(0xFFF1F8FB)
        )
    )

    Box(
        modifier = Modifier
            .size(50.dp)
            .graphicsLayer {
                rotationZ = -12f
                transformOrigin =
                    androidx.compose.ui.graphics.TransformOrigin.Center
            }
            .border(
                border = BorderStroke(18.dp, brush),
                shape = RoundedCornerShape(0.dp)
            )
            .background(Color.Transparent)
            .then(modifier)
    )
}