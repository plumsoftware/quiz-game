package ru.plumsoftware.game.ui.components.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.ui.GameScreen
import ru.plumsoftware.game.ui.theme.GamePurple
import ru.plumsoftware.game.ui.theme.GamePurpleLight
import ru.plumsoftware.game.ui.theme.GameTextDisabled

@Composable
fun GameBottomNav(
    currentScreen: GameScreen,
    onNavigate: (GameScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Rounded.Home,
            label = "Главная",
            selected = currentScreen == GameScreen.HOME,
            onClick = { onNavigate(GameScreen.HOME) },
            modifier = Modifier.weight(1f)
        )

        FloatingActionButton(
            onClick = { onNavigate(GameScreen.QUIZ_MENU) },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(52.dp),
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(Icons.Rounded.PlayArrow, "Играть", tint = GamePurple, modifier = Modifier.size(26.dp))
        }

        BottomNavItem(
            icon = Icons.Rounded.Menu,
            label = "Ещё",
            selected = currentScreen == GameScreen.MORE,
            onClick = { onNavigate(GameScreen.MORE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) GamePurpleLight else GameTextDisabled,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) GamePurpleLight else GameTextDisabled
            )
        }
    }
}
