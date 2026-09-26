package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

/**
 * Политика конфиденциальности внутри приложения (§9.3). Текст — assets/privacy_policy.md,
 * тот же файл публикуется на странице в магазине. Открывается только через родительский барьер.
 */
@Composable
fun PrivacyScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lines = remember {
        try {
            context.assets.open("privacy_policy.md").bufferedReader(Charsets.UTF_8).use { it.readLines() }
        } catch (e: Exception) {
            listOf("Не удалось открыть текст политики.")
        }.filter { it.isNotBlank() }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KidsBackButton(onClick = onBack)
                Text(
                    "Конфиденциальность", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp, color = Kids.TextPrimary
                )
            }
        }
        item {
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lines.forEach { line -> PolicyLine(line) }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PolicyLine(line: String) {
    when {
        line.startsWith("# ") -> Text(
            line.removePrefix("# "), fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
            fontSize = 17.sp, lineHeight = 22.sp, color = Kids.TextPrimary
        )
        line.startsWith("## ") -> Text(
            line.removePrefix("## "), fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
            fontSize = 14.sp, color = Kids.Primary, modifier = Modifier.padding(top = 8.dp)
        )
        line.startsWith("- ") -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("•", fontFamily = RubikFamily, fontSize = 13.sp, color = Kids.TextSecondary)
            Text(
                line.removePrefix("- "), fontFamily = RubikFamily, fontSize = 13.sp, lineHeight = 18.sp,
                color = Kids.TextSecondary
            )
        }
        else -> Text(
            line, fontFamily = RubikFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Kids.TextSecondary
        )
    }
}
