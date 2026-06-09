package ru.plumsoftware.game.ui.components.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.theme.GamePurple

private data class NavItem(val screen: GameScreen, val label: String, val icon: ImageVector)

@Composable
fun GameBottomNav(
    currentScreen: GameScreen,
    onNavigate: (GameScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem(GameScreen.HOME, "Главная", Icons.Rounded.Home),
        NavItem(GameScreen.QUIZ_MENU, "Играть", Icons.Rounded.PlayCircle),
        NavItem(GameScreen.DAILY_TASKS, "Задания", Icons.Rounded.CheckCircle),
        NavItem(GameScreen.PROFILE, "Профиль", Icons.Rounded.Person),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
                val isSelected = currentScreen == item.screen
            val isPlay = item.screen == GameScreen.QUIZ_MENU
            val itemColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.55f)

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isPlay) {
                    FloatingActionButton(
                        onClick = { onNavigate(item.screen) },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(52.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(
                            item.icon,
                            item.label,
                            tint = GamePurple,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                } else {
                    Surface(
                        onClick = { onNavigate(item.screen) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color.White.copy(alpha = 0.12f) else Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = itemColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = itemColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
