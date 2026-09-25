package ru.plumsoftware.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Заголовки, цифры, крупные кнопки (ТЗ §3.2)
private val unboundedFont = GoogleFont("Unbounded")
val UnboundedFamily = FontFamily(
    Font(googleFont = unboundedFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = unboundedFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// Основной текст и подписи (ТЗ §3.2)
private val rubikFont = GoogleFont("Rubik")
val RubikFamily = FontFamily(
    Font(googleFont = rubikFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = rubikFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = rubikFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = rubikFont, fontProvider = provider, weight = FontWeight.Bold),
)

// Сохранён старый псевдоним, чтобы существующие экраны компилировались
val nunitoFontFamily = RubikFamily

val Typography = Typography(
    // H1 — заголовок экрана (Unbounded 800 / 26)
    headlineLarge = TextStyle(fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 30.sp),
    // H2 (Unbounded 700 / 16–20)
    headlineSmall = TextStyle(fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 22.sp),
    // Вопрос (Unbounded 700 / 19)
    titleMedium = TextStyle(fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
    // Текст (Rubik 500–600 / 14–16)
    bodyLarge = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    // Подпись (Rubik 600–700 / 11–13)
    labelLarge = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
)
