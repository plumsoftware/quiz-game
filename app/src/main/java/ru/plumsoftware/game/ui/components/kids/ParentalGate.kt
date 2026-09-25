package ru.plumsoftware.game.ui.components.kids

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/**
 * Родительский барьер (ТЗ §9.1): «Попроси взрослого», пример на умножение,
 * ввод числа. После 3 ошибок барьер блокируется на 1 минуту.
 */
@Composable
fun ParentalGate(
    onPass: () -> Unit,
    onDismiss: () -> Unit
) {
    var a by remember { mutableIntStateOf((2..9).random()) }
    var b by remember { mutableIntStateOf((2..9).random()) }
    var input by remember { mutableStateOf("") }
    var errors by remember { mutableIntStateOf(0) }
    var lockedSeconds by remember { mutableIntStateOf(0) }

    val locked = lockedSeconds > 0
    androidx.compose.runtime.LaunchedEffect(lockedSeconds) {
        if (lockedSeconds > 0) {
            delay(1000)
            lockedSeconds--
            if (lockedSeconds == 0) errors = 0
        }
    }

    fun check() {
        if (input.toIntOrNull() == a * b) {
            onPass()
        } else {
            errors++
            input = ""
            a = (2..9).random(); b = (2..9).random()
            if (errors >= 3) lockedSeconds = 60
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Kids.Card).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Попроси взрослого", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp, color = Kids.TextPrimary, textAlign = TextAlign.Center)
            Text("Чтобы продолжить, реши пример:", fontFamily = RubikFamily, fontSize = 14.sp,
                color = Kids.TextSecondary, textAlign = TextAlign.Center)
            Text("$a × $b = ?", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                fontSize = 26.sp, color = Kids.Primary)

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Kids.Background)
                    .border(3.dp, Kids.InputBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (input.isEmpty()) {
                    Text("Ответ", fontFamily = RubikFamily, fontSize = 18.sp, color = Kids.TextMuted)
                }
                BasicTextField(
                    value = input,
                    onValueChange = { if (!locked && it.length <= 4 && it.all(Char::isDigit)) input = it },
                    singleLine = true,
                    enabled = !locked,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, color = Kids.TextPrimary, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(Kids.Primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (locked) {
                Text("Слишком много ошибок. Подожди $lockedSeconds с.", fontFamily = RubikFamily,
                    fontSize = 12.sp, color = Kids.Error, textAlign = TextAlign.Center)
            } else if (errors > 0) {
                Text("Неверно. Осталось попыток: ${3 - errors}", fontFamily = RubikFamily,
                    fontSize = 12.sp, color = Kids.Error, textAlign = TextAlign.Center)
            }

            KidsButton(text = "Подтвердить", onClick = { check() }, enabled = !locked,
                modifier = Modifier.fillMaxWidth())
            Text("Отмена", fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = Kids.TextSecondary, modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
        }
    }
}
