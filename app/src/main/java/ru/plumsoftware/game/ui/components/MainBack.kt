package ru.plumsoftware.game.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ru.plumsoftware.game.R

@Composable
fun MainBack(content: @Composable () -> Unit){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(R.drawable.quiz_back),
            contentScale = ContentScale.FillHeight,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = 0.2f))
        )
        Image(
            modifier = Modifier.align(Alignment.BottomCenter),
            painter = painterResource(R.drawable.quiz_back),
            contentScale = ContentScale.FillHeight,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = 0.2f))
        )
        content()
    }
}