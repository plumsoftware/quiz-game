package ru.plumsoftware.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.plumsoftware.game.ui.theme.GameBackground

@Composable
fun MainBack(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        content()
    }
}
