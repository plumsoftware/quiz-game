package ru.plumsoftware.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.game.audio.LocalFeedback
import ru.plumsoftware.game.audio.Sfx
import ru.plumsoftware.game.data.ALL_AVATARS
import ru.plumsoftware.game.data.Avatar
import ru.plumsoftware.game.data.Economy
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.GemShopItem
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.ui.components.kids.GameIcon
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsChip
import ru.plumsoftware.game.ui.components.kids.desaturate
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

private val SHOP_HINTS = listOf(
    PowerUpType.FIFTY_FIFTY,
    PowerUpType.FREEZE_TIME,
    PowerUpType.EXTRA_LIFE,
    PowerUpType.SKIP_QUESTION,
)

/** Магазин (ТЗ §5.7). Покупок за реальные деньги нет — только монеты и кристаллы, заработанные в игре. */
@Composable
fun ShopScreen(
    gameState: GameState,
    rewardedReady: Boolean,
    onFreeCoins: () -> Unit,
    onBuyHint: (PowerUpType, (Boolean) -> Unit) -> Unit,
    onBuyGemItem: (GemShopItem, (Boolean) -> Unit) -> Unit,
    onAvatar: (Avatar, (Boolean) -> Unit) -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = LocalFeedback.current
    val freeCoinsLeft = (Economy.FREE_COINS_DAILY_LIMIT - gameState.freeCoinsClaimedToday).coerceAtLeast(0)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок и балансы.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Магазин", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp, color = Kids.TextPrimary, modifier = Modifier.weight(1f)
                )
                KidsChip("${gameState.coins}", Kids.CoinSoft, Kids.CoinText, leading = "🪙")
                Spacer(Modifier.width(6.dp))
                KidsChip("${gameState.gems}", Color(0xFFE1F2FF), Kids.GemShadow, leading = "💎")
            }

            // Бесплатные монеты (rewarded, до 5 раз в день).
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Kids.CoinSoft),
                        contentAlignment = Alignment.Center
                    ) { GameIcon("currency_coin", "🪙", 38.dp) }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Бесплатные монеты", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = Kids.TextPrimary
                        )
                        Text(
                            when {
                                freeCoinsLeft <= 0 -> "На сегодня всё — приходи завтра"
                                !rewardedReady -> "Видео пока нет · осталось $freeCoinsLeft из ${Economy.FREE_COINS_DAILY_LIMIT}"
                                else -> "Посмотри видео · осталось $freeCoinsLeft из ${Economy.FREE_COINS_DAILY_LIMIT}"
                            },
                            fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary
                        )
                    }
                    KidsButton(
                        onClick = onFreeCoins,
                        enabled = freeCoinsLeft > 0 && rewardedReady,
                        color = Kids.Coin, shadow = Kids.CoinShadow, contentColor = Kids.CoinText,
                        depth = 4.dp, cornerRadius = 14.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) { Text("+${Economy.FREE_COINS_REWARD}", fontSize = 15.sp) }
                }
            }

            // Подсказки 2×2.
            SectionTitle("Подсказки")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SHOP_HINTS.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { type ->
                            val canAfford = gameState.coins >= type.price
                            HintShopCard(
                                type = type,
                                owned = gameState.hint(type.id),
                                canAfford = canAfford,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (!canAfford) onToast("Не хватает монет")
                                else onBuyHint(type) { ok ->
                                    if (ok) feedback?.play(Sfx.COIN)
                                    onToast(if (ok) "Куплено: ${type.title}" else "Не хватает монет")
                                }
                            }
                        }
                    }
                }
            }

            // Товары за кристаллы.
            SectionTitle("За кристаллы")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GemShopItem.entries.forEach { item ->
                    val canAfford = gameState.gems >= item.price
                    GemItemRow(
                        item = item,
                        owned = when (item) {
                            GemShopItem.STREAK_FREEZE -> "есть: ${gameState.streakFreezes}"
                            GemShopItem.LIFE_PACK -> "есть: ${gameState.hint(PowerUpType.EXTRA_LIFE.id)}"
                            else -> null
                        },
                        canAfford = canAfford
                    ) {
                        if (!canAfford) onToast("Не хватает кристаллов")
                        else onBuyGemItem(item) { ok ->
                            if (ok) feedback?.play(Sfx.COIN)
                            onToast(if (ok) "Куплено: ${item.title}" else "Не хватает кристаллов")
                        }
                    }
                }
            }
            Text(
                "Кристаллы можно получить в сундуках на карте, за боссов, достижения и некоторые задания дня.",
                fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary
            )

            // Персонажи 3×2.
            SectionTitle("Персонажи")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ALL_AVATARS.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { avatar ->
                            val owned = avatar.id in gameState.ownedAvatars || avatar.price == 0
                            val selected = avatar.id == gameState.avatarId
                            AvatarShopCard(
                                avatar = avatar,
                                label = when {
                                    selected -> "Выбран"
                                    owned -> "Выбрать"
                                    else -> "${avatar.price} 🪙"
                                },
                                selected = selected,
                                owned = owned,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (!owned && gameState.coins < avatar.price) onToast("Не хватает монет")
                                else onAvatar(avatar) { ok ->
                                    if (ok && !owned) {
                                        feedback?.play(Sfx.COIN)
                                        onToast("Новый друг: ${avatar.name}!")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Kids.TextPrimary)
}

@Composable
private fun HintShopCard(
    type: PowerUpType, owned: Int, canAfford: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    KidsCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            GameIcon(type.iconKey, type.emoji, 44.dp)
            Text(
                type.title, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = Kids.TextPrimary, textAlign = TextAlign.Center
            )
            Text(
                type.description, fontFamily = RubikFamily, fontSize = 11.sp, color = Kids.TextSecondary,
                textAlign = TextAlign.Center, minLines = 2, maxLines = 2
            )
            Text("есть: $owned", fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Kids.TextMuted)
            KidsButton(
                onClick = onClick, modifier = Modifier.fillMaxWidth().alpha(if (canAfford) 1f else 0.5f),
                color = Kids.Coin, shadow = Kids.CoinShadow, contentColor = Kids.CoinText,
                depth = 4.dp, cornerRadius = 12.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
            ) { Text("${type.price} 🪙", fontSize = 14.sp) }
        }
    }
}

@Composable
private fun AvatarShopCard(
    avatar: Avatar, label: String, selected: Boolean, owned: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Kids.Card)
            .border(3.dp, if (selected) Kids.Primary else Kids.CardShadow, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GameIcon(
            "avatar_${avatar.id}", avatar.emoji, 52.dp,
            colorFilter = if (owned) null else desaturate(0.6f)
        )
        Text(
            label, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
            color = when {
                selected -> Kids.Primary
                owned -> Kids.TextSecondary
                else -> Kids.CoinText
            }
        )
    }
}

@Composable
private fun GemItemRow(item: GemShopItem, owned: String?, canAfford: Boolean, onClick: () -> Unit) {
    KidsCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE1F2FF)),
                contentAlignment = Alignment.Center
            ) { GameIcon(item.iconKey, item.emoji, 38.dp) }
            Column(Modifier.weight(1f)) {
                Text(item.title, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Kids.TextPrimary)
                Text(
                    item.description + (owned?.let { " · $it" } ?: ""),
                    fontFamily = RubikFamily, fontSize = 12.sp, color = Kids.TextSecondary
                )
            }
            KidsButton(
                onClick = onClick,
                modifier = Modifier.alpha(if (canAfford) 1f else 0.5f),
                color = Kids.Gem, shadow = Kids.GemShadow, depth = 4.dp, cornerRadius = 14.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) { Text("${item.price} 💎", fontSize = 14.sp) }
        }
    }
}
