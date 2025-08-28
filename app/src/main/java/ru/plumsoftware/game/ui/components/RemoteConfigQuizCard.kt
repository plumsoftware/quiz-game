package ru.plumsoftware.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.sweepGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.plumsoftware.game.R
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.data.firebase.RemoteConfigQuizModel
import ru.plumsoftware.game.ui.extendOutsideParent
import ru.plumsoftware.game.ui.formatDuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

@Composable
fun RemoteConfigQuizCard(
    onNavigateToQuiz: (RemoteConfigQuizModel) -> Unit,
    availableQuizzes: List<Quiz>,
    remoteQuiz: RemoteConfigQuizModel
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }

    val endDate by remember(remoteQuiz.dateEnd) {
        mutableStateOf(LocalDateTime.parse(remoteQuiz.dateEnd, formatter))
    }

    val scope = rememberCoroutineScope { Dispatchers.IO }

    var timeLeft by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                val now = LocalDateTime.now()
                timeLeft = if (now >= endDate) {
                    "Время истекло"
                    break
                } else {
                    formatDuration(Duration.between(now, endDate))
                }
                delay(1000)
            }
        }
    }

    // Анимация градиента (остаётся без изменений)
    val transition = rememberInfiniteTransition(label = "borderGradientTransition")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderAngleAnimation"
    )

    val brush by remember(angle) {
        mutableStateOf(
            Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.3f),
                    Color(0xFFFFF200).copy(alpha = 0.5f),
                    Color(0xFFFF656F).copy(alpha = 0.5f),
                    Color(0xFFFF0000).copy(alpha = 0.5f),
                    Color(0xFFFF7B00).copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = Offset(0.5f, 0.5f),
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .height(160.dp)
                .border(
                    width = 4.dp,
                    brush = brush,
                    shape = RoundedCornerShape(12.dp)
                ),
            onClick = { onNavigateToQuiz(remoteQuiz) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF73B9FF),
                                Color(0xFFAF03F9),
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.king),
                                contentDescription = "Quiz",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = remoteQuiz.cardTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Временное событие",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Осталось $timeLeft",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.extra_play_icon),
                                contentDescription = "Play",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
        Image(
            modifier = Modifier
                .size(60.dp, 95.dp)
                .extendOutsideParent(end = 20.dp, bottom = 20.dp)
                .align(Alignment.BottomEnd),
            painter = painterResource(R.drawable.fire),
            contentDescription = null
        )
        Box(
            modifier = Modifier.extendOutsideParent(top = 20.dp, start = 20.dp)
        ) {
            RobloxIcon()
        }
    }
}