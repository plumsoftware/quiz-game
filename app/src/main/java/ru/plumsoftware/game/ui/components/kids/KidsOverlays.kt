package ru.plumsoftware.game.ui.components.kids

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import kotlinx.coroutines.delay
import ru.plumsoftware.game.data.avatarById
import ru.plumsoftware.game.ui.AchievementToast
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Затемнённый фон + карточка по центру. Нажатие мимо карточки ничего не делает. */
@Composable
fun KidsModal(
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(interactionSource = interaction, indication = null) { onDismissRequest?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Kids.Card)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    }
}

/** Диалог в стиле игры: заголовок, текст, основная кнопка и (необязательная) вторичная ссылка. */
@Composable
fun KidsDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    iconKey: String? = null,
    iconFallback: String = "",
    confirmEnabled: Boolean = true,
    confirmColor: Color = Kids.Primary,
    confirmShadow: Color = Kids.PrimaryShadow
) {
    KidsModal(onDismissRequest = null) {
        if (iconKey != null) GameIcon(iconKey, iconFallback, 64.dp)
        Text(
            title, fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
            color = Kids.TextPrimary, textAlign = TextAlign.Center
        )
        if (message.isNotEmpty()) {
            Text(
                message, fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                color = Kids.TextSecondary, textAlign = TextAlign.Center
            )
        }
        KidsButton(
            text = confirmText, onClick = onConfirm, modifier = Modifier.fillMaxWidth(),
            enabled = confirmEnabled, color = confirmColor, shadow = confirmShadow
        )
        if (dismissText != null) {
            Text(
                dismissText, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = Kids.TextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

/** Всплывающее сообщение внизу экрана. */
@Composable
fun KidsToast(message: String?, modifier: Modifier = Modifier) {
    // Последний текст держим вне state, чтобы он не пропадал во время исчезновения.
    val last = remember { arrayOf("") }
    if (message != null) last[0] = message
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(tween(150)) + slideInVertically(tween(250)) { it / 2 },
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Kids.TextPrimary)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                last[0], fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                color = Color.White, textAlign = TextAlign.Center
            )
        }
    }
}

/** Круглый аватар игрока. Золотая рамка — награда за серию 30 дней (§5.10). */
@Composable
fun AvatarCircle(
    avatarId: String,
    size: Dp,
    modifier: Modifier = Modifier,
    goldenFrame: Boolean = false,
    borderWidth: Dp = 3.dp
) {
    val avatar = avatarById(avatarId)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Kids.Avatar)
            .border(if (goldenFrame) borderWidth + 1.dp else borderWidth, if (goldenFrame) Kids.Coin else Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        GameIcon("avatar_${avatar.id}", avatar.emoji, size * 0.72f)
    }
}

/** Конфетти поверх экрана (результат с 3 звёздами, §5.6). */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier, pieces: Int = 90, seed: Int = 1) {
    val progress = remember(seed) { Animatable(0f) }
    LaunchedEffect(seed) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(2600, easing = LinearEasing))
    }
    val colors = remember {
        listOf(Kids.Primary, Kids.Success, Kids.Coin, Kids.Error, Kids.Gem, Kids.StreakFrom, Color(0xFFE46BE0))
    }
    val particles = remember(seed) {
        val rnd = Random(seed)
        List(pieces) {
            val angle = rnd.nextDouble(-PI * 0.9, -PI * 0.1)
            val speed = rnd.nextDouble(0.55, 1.25)
            Particle(
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                color = colors[rnd.nextInt(colors.size)],
                size = rnd.nextDouble(6.0, 12.0).toFloat(),
                spin = rnd.nextDouble(-720.0, 720.0).toFloat()
            )
        }
    }
    if (progress.value >= 1f) return
    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val origin = Offset(size.width / 2f, size.height * 0.32f)
        val scale = size.minDimension * 0.9f
        particles.forEach { p ->
            val x = origin.x + p.vx * scale * t
            val y = origin.y + p.vy * scale * t + 1.4f * scale * t * t
            val alpha = (1f - t).coerceIn(0f, 1f)
            rotate(p.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.size / 2, y - p.size / 4),
                    size = Size(p.size, p.size / 2)
                )
            }
        }
    }
}

private data class Particle(val vx: Float, val vy: Float, val color: Color, val size: Float, val spin: Float)

/** Тост о новом достижении сверху (§5.9). */
@Composable
fun AchievementToastOverlay(
    toast: AchievementToast?,
    onShown: (AchievementToast) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayed by remember { mutableStateOf<AchievementToast?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(toast?.id) {
        if (toast != null) {
            displayed = toast
            visible = true
            onShown(toast)
            delay(3000)
            visible = false
            delay(350)
            displayed = null
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && displayed != null,
        enter = slideInVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) { -it } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(300)) { -it } + fadeOut(tween(200)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 14.dp, end = 14.dp)
    ) {
        val t = displayed ?: return@AnimatedVisibility
        KidsCard(modifier = Modifier.fillMaxWidth(), shadow = Kids.PrimarySoftShadow) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Kids.PrimarySoft),
                    contentAlignment = Alignment.Center
                ) { GameIcon(t.iconKey, t.emoji, 34.dp) }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Новое достижение!", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                        fontSize = 11.sp, color = Kids.Primary
                    )
                    Text(
                        t.title, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = Kids.TextPrimary
                    )
                    Text(
                        t.description, fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                if (t.reward > 0) {
                    Text(
                        "+${t.reward} ${t.rewardEmoji}", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, color = Kids.CoinText
                    )
                }
            }
        }
    }
}

/**
 * Баннер 320×50 над нижним меню (§8, место №1). Подпись «Реклама».
 * Показывается только на главной и скрывается после покупки «Отключить рекламу».
 */
@Composable
fun AdBanner(adUnitId: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Реклама", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 10.sp,
            color = Kids.TextMuted
        )
        AndroidView(
            modifier = Modifier.width(320.dp).height(50.dp),
            factory = { ctx ->
                BannerAdView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // SDK 8: ID блока передаётся в AdRequest.Builder, размер — BannerAdSize.fixed.
                    setAdSize(BannerAdSize.fixed(ctx, 320, 50))
                    loadAd(AdRequest.Builder(adUnitId).build())
                }
            },
            onRelease = { it.destroy() }
        )
    }
}
