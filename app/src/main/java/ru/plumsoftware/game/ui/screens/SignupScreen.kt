package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.data.ALL_AVATARS
import ru.plumsoftware.game.data.AgeGroup
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/** Создание профиля «Создай героя» (ТЗ §5.2). */
@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onFinish: (name: String, avatarId: String, ageGroup: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var avatarIndex by remember { mutableStateOf(0) }
    var ageIndex by remember { mutableStateOf(0) }

    val avatar = ALL_AVATARS[avatarIndex]
    val canFinish = name.trim().isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KidsBackButton(onClick = onBack)
            Text(
                "Создай героя",
                fontFamily = UnboundedFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Kids.TextPrimary
            )
        }

        // Превью аватара и имени.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Kids.Avatar)
                    .border(5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(avatar.emoji, fontSize = 62.sp) }
            Text(
                name.trim().ifEmpty { " " },
                fontFamily = UnboundedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Kids.TextPrimary
            )
        }

        // Имя.
        FieldLabel("Как тебя зовут?")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Kids.Card)
                .border(3.dp, Kids.InputBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 15.dp)
        ) {
            if (name.isEmpty()) {
                Text(
                    "Имя или прозвище",
                    fontFamily = RubikFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Kids.TextMuted
                )
            }
            BasicTextField(
                value = name,
                onValueChange = { if (it.length <= 14) name = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = RubikFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Kids.TextPrimary
                ),
                cursorBrush = SolidColor(Kids.Primary),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Персонаж.
        FieldLabel("Выбери персонажа")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ALL_AVATARS.forEachIndexed { index, a ->
                val selected = index == avatarIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) Kids.PrimarySoft else Kids.Card)
                        .border(
                            3.dp,
                            if (selected) Kids.Primary else Kids.CardShadow,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { avatarIndex = index },
                    contentAlignment = Alignment.Center
                ) { Text(a.emoji, fontSize = 26.sp) }
            }
        }

        // Возраст.
        FieldLabel("Сколько тебе лет?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AgeGroup.entries.forEachIndexed { index, ag ->
                val selected = index == ageIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) Kids.Primary else Kids.Card)
                        .border(
                            3.dp,
                            if (selected) Kids.Primary else Kids.CardShadow,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { ageIndex = index }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        ag.label,
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (selected) Color.White else Kids.TextPrimary
                    )
                }
            }
        }
        Text(
            "Подберём сложность. Её можно поменять в настройках.",
            fontFamily = RubikFamily,
            fontSize = 12.sp,
            color = Kids.TextMuted
        )

        KidsButton(
            onClick = { onFinish(name.trim(), avatar.id, AgeGroup.entries[ageIndex].id) },
            enabled = canFinish,
            color = Kids.Success,
            shadow = Kids.SuccessShadow,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Поехали! 🚀") }
        Text(
            "Мы не собираем почту и телефон. Всё хранится только на этом устройстве.",
            fontFamily = RubikFamily,
            fontSize = 11.sp,
            color = Kids.TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        fontFamily = RubikFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Kids.TextSecondary
    )
}
