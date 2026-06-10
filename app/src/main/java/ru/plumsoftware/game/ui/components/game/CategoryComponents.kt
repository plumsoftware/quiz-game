package ru.plumsoftware.game.ui.components.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.Difficulty
import ru.plumsoftware.game.data.QuizCategory
import ru.plumsoftware.game.ui.theme.*

@Composable
fun CategoriesHorizontalRow(
    categories: List<QuizCategory>,
    onSelect: (QuizCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { category ->
            CategoryChipCard(category = category, onClick = { onSelect(category) })
        }
    }
}

@Composable
fun CategoryChipCard(category: QuizCategory, onClick: () -> Unit) {
    val diffColor = Color(category.difficulty.color)
    Card(
        onClick = onClick,
        modifier = Modifier.width(78.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = category.bgColor.copy(alpha = 0.13f)),
        border = BorderStroke(1.dp, category.bgColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(category.emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.labelSmall,
                color = GameTextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp
            )
            Spacer(Modifier.height(5.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = diffColor.copy(alpha = 0.12f),
                border = BorderStroke(0.5.dp, diffColor.copy(alpha = 0.4f))
            ) {
                Text(
                    category.difficulty.label,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    color = diffColor,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FilterPill(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) color.copy(alpha = 0.2f) else GameSurface,
        border = BorderStroke(1.dp, if (selected) color.copy(alpha = 0.7f) else GameBorder)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else GameTextMuted,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CategoryFullCard(category: QuizCategory, onClick: () -> Unit) {
    val diffColor = Color(category.difficulty.color)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = category.bgColor.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, category.bgColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(category.emoji, style = MaterialTheme.typography.headlineMedium)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = diffColor.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, diffColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        category.difficulty.label,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = diffColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.titleSmall,
                color = GameTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                category.description,
                style = MaterialTheme.typography.bodySmall,
                color = GameTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${category.questionsCount} вопросов",
                style = MaterialTheme.typography.labelSmall,
                color = GameTextDisabled
            )
        }
    }
}
