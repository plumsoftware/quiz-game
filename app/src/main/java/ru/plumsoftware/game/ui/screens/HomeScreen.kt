package ru.plumsoftware.game.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.Quiz
import ru.plumsoftware.game.R
import ru.plumsoftware.game.ui.components.RemoteConfigQuizCard

@Composable
fun HomeScreen(
    gameState: GameState,
    availableQuizzes: List<Quiz>,
    onNavigateToQuiz: () -> Unit,
    onNavigateToDailyTasks: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.085f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1.0f)
                .align(Alignment.TopCenter)
                .background(Color.Transparent)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF6F7D7)
                    ),
                    onClick = onNavigateToAchievements
                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .wrapContentSize()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.achievements),
                            contentDescription = "Достижения",
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Достижения",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEFE2D9)
                    ),
                    onClick = onNavigateToProfile
                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .wrapContentSize()
                    ) {
                        Text(
                            text = "Профиль",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Image(
                            painter = painterResource(R.drawable.profile),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header with coins and level
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Уровень ${gameState.level}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${gameState.experience} ОП",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        // Level progress indicator
                        val nextLevelExp = (gameState.level * 100) - gameState.experience
                        if (nextLevelExp > 0) {
                            Text(
                                text = "До следующего уровня: ${nextLevelExp} ОП",
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = Color(0xFFFFD700)
                            )
                            Text(
                                text = "${gameState.coins}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

//                        IconButton(onClick = onNavigateToAchievements) {
//                            Icon(
//                                Icons.Default.Star,
//                                contentDescription = "Achievements",
//                                tint = Color(0xFFFFD700)
//                            )
//                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Streak card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF6B6B)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${gameState.streakDays} дней подряд!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Учись каждый день!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quiz Section (Remote config)
            RemoteConfigQuizCard(
                onNavigateToQuiz = onNavigateToQuiz,
                availableQuizzes = availableQuizzes
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quiz Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    },
                onClick = onNavigateToQuiz
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    MaterialTheme.colorScheme.primary,
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
                        // Animated icon with glow effect
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(ru.plumsoftware.game.R.drawable.quiz_button_icon),
                                    contentDescription = "Quiz",
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "🎮 Играть в викторину",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Доступно ${availableQuizzes.size} викторин",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // Play button with glow
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Other menu buttons
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1.0f)
            ) {
                item {
                    MenuButton(
                        icon = Icons.Default.Assignment,
                        title = "Ежедневные задания",
                        subtitle = "Выполняй задачи",
                        color = Color(0xFFE3F5DF),
                        iconColor = Color(0xFF43C243),
                        onClick = onNavigateToDailyTasks
                    )
                }

                item {
                    MenuButton(
                        icon = Icons.Default.Store,
                        title = "Магазин",
                        subtitle = "Трать свои монеты",
                        color = Color(0xFFE2F1EA),
                        iconColor = Color(0xFF3E99C9),
                        onClick = onNavigateToShop
                    )
                }
            }
        }
    }
}


@Composable
fun MenuButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
} 