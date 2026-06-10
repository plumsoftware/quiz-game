package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.data.ALL_CATEGORIES
import ru.plumsoftware.game.data.Difficulty
import ru.plumsoftware.game.data.QuizCategory
import ru.plumsoftware.game.ui.components.game.CategoryFullCard
import ru.plumsoftware.game.ui.components.game.FilterPill
import ru.plumsoftware.game.ui.components.game.GameScreenTopBar
import ru.plumsoftware.game.ui.theme.GameBackground
import ru.plumsoftware.game.ui.theme.GamePurple

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onCategorySelect: (QuizCategory) -> Unit
) {
    var filter by remember { mutableStateOf<Difficulty?>(null) }
    val shown = remember(filter) {
        if (filter != null) ALL_CATEGORIES.filter { it.difficulty == filter } else ALL_CATEGORIES
    }

    Column(modifier = Modifier.fillMaxSize().background(GameBackground)) {
        GameScreenTopBar(title = "Категории", onBack = onBack)

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterPill(
                    label = "Все",
                    selected = filter == null,
                    color = GamePurple,
                    onClick = { filter = null }
                )
            }
            items(Difficulty.entries) { diff ->
                FilterPill(
                    label = diff.label,
                    selected = filter == diff,
                    color = Color(diff.color),
                    onClick = { filter = if (filter == diff) null else diff }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 14.dp, bottom = 16.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shown) { cat ->
                CategoryFullCard(category = cat, onClick = { onCategorySelect(cat) })
            }
        }
    }
}
