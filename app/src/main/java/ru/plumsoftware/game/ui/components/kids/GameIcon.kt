package ru.plumsoftware.game.ui.components.kids

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.R

/**
 * Иконки из папки icons/emoji (ТЗ §3.4, §12), лежат в res/drawable-nodpi под теми же именами.
 * При замене на Fluent Emoji 3D достаточно положить файлы с теми же именами.
 */
object GameIcons {
    private val map: Map<String, Int> = mapOf(
        "ach_brain" to R.drawable.ach_brain,
        "ach_first_step" to R.drawable.ach_first_step,
        "ach_lightning" to R.drawable.ach_lightning,
        "ach_marathon" to R.drawable.ach_marathon,
        "ach_perfect" to R.drawable.ach_perfect,
        "avatar_fox" to R.drawable.avatar_fox,
        "avatar_frog" to R.drawable.avatar_frog,
        "avatar_octopus" to R.drawable.avatar_octopus,
        "avatar_panda" to R.drawable.avatar_panda,
        "avatar_tiger" to R.drawable.avatar_tiger,
        "avatar_unicorn" to R.drawable.avatar_unicorn,
        "cloud_restore" to R.drawable.cloud_restore,
        "currency_coin" to R.drawable.currency_coin,
        "currency_gem" to R.drawable.currency_gem,
        "daily_quest" to R.drawable.daily_quest,
        "difficulty_easy" to R.drawable.difficulty_easy,
        "difficulty_hard" to R.drawable.difficulty_hard,
        "difficulty_medium" to R.drawable.difficulty_medium,
        "hint_fifty" to R.drawable.hint_fifty,
        "hint_freeze" to R.drawable.hint_freeze,
        "hint_life" to R.drawable.hint_life,
        "hint_skip" to R.drawable.hint_skip,
        "map_boss" to R.drawable.map_boss,
        "map_chest" to R.drawable.map_chest,
        "map_lock" to R.drawable.map_lock,
        "no_ads" to R.drawable.no_ads,
        "pack_gems_large" to R.drawable.pack_gems_large,
        "pack_gems_medium" to R.drawable.pack_gems_medium,
        "question" to R.drawable.question,
        "set_difficulty" to R.drawable.set_difficulty,
        "set_language" to R.drawable.set_language,
        "set_music" to R.drawable.set_music,
        "set_notifications" to R.drawable.set_notifications,
        "set_parent" to R.drawable.set_parent,
        "set_settings" to R.drawable.set_settings,
        "set_sound" to R.drawable.set_sound,
        "set_time_limit" to R.drawable.set_time_limit,
        "set_vibro" to R.drawable.set_vibro,
        "set_voice" to R.drawable.set_voice,
        "star" to R.drawable.star,
        "stat_correct" to R.drawable.stat_correct,
        "stat_games" to R.drawable.stat_games,
        "streak_fire" to R.drawable.streak_fire,
        "streak_freeze" to R.drawable.streak_freeze,
        "tab_home" to R.drawable.tab_home,
        "tab_profile" to R.drawable.tab_profile,
        "tab_shop" to R.drawable.tab_shop,
        "tab_topics" to R.drawable.tab_topics,
        "topic_animals" to R.drawable.topic_animals,
        "topic_countries" to R.drawable.topic_countries,
        "topic_fairytales" to R.drawable.topic_fairytales,
        "topic_food" to R.drawable.topic_food,
        "topic_math" to R.drawable.topic_math,
        "topic_music" to R.drawable.topic_music,
        "topic_nature" to R.drawable.topic_nature,
        "topic_science" to R.drawable.topic_science,
        "topic_space" to R.drawable.topic_space,
        "topic_sport" to R.drawable.topic_sport,
        "trophy" to R.drawable.trophy,
    )

    fun res(key: String): Int? = map[key]
}

/** PNG-иконка по ключу; если файла нет — эмодзи-заглушка. */
@Composable
fun GameIcon(
    key: String,
    fallback: String,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    colorFilter: ColorFilter? = null
) {
    val res = GameIcons.res(key)
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            colorFilter = colorFilter
        )
    } else {
        Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(fallback, fontSize = (size.value * 0.78f).sp)
        }
    }
}

/** Обесцвечивание иконки: 0.6 → «обесцвечены на 60 %» (§3.3). */
fun desaturate(amount: Float): ColorFilter =
    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1f - amount) })

/** Интерфейсные векторные иконки icons/ui (§12). */
enum class UiIcon(val res: Int) {
    BACK(R.drawable.ic_ui_back),
    FORWARD(R.drawable.ic_ui_forward),
    CLOSE(R.drawable.ic_ui_close),
    CHECK(R.drawable.ic_ui_check),
    PLAY(R.drawable.ic_ui_play)
}

@Composable
fun UiIconView(icon: UiIcon, tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Icon(painter = painterResource(icon.res), contentDescription = null, tint = tint, modifier = modifier.size(size))
}
