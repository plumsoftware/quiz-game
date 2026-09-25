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
import ru.plumsoftware.game.data.ALL_AVATARS
import ru.plumsoftware.game.data.GameState
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.ui.components.kids.KidsBackButton
import ru.plumsoftware.game.ui.components.kids.KidsButton
import ru.plumsoftware.game.ui.components.kids.KidsCard
import ru.plumsoftware.game.ui.components.kids.KidsChip
import ru.plumsoftware.game.ui.components.kids.ParentalGate
import ru.plumsoftware.game.ui.theme.Kids
import ru.plumsoftware.game.ui.theme.RubikFamily
import ru.plumsoftware.game.ui.theme.UnboundedFamily

private data class GemPack(val gems: Int, val price: String, val badge: String? = null)

private val GEM_PACKS = listOf(
    GemPack(50, "149 ₽"),
    GemPack(180, "399 ₽", badge = "ВЫГОДНО"),
    GemPack(500, "899 ₽"),
)

// Подсказки магазина (ТЗ §5.7). Цена берётся из PowerUpType.price.
private val SHOP_HINTS = listOf(
    PowerUpType.FIFTY_FIFTY,
    PowerUpType.FREEZE_TIME,
    PowerUpType.EXTRA_LIFE,
    PowerUpType.SKIP_QUESTION,
)

/** Магазин (ТЗ §5.7). */
@Composable
fun ShopScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onClaimFreeCoins: ((Boolean) -> Unit) -> Unit,
    onPurchasePowerUp: (PowerUpType, (Boolean) -> Unit) -> Unit,
    onPurchaseAvatar: (String, Int, (Boolean) -> Unit) -> Unit,
    onGrantGems: (Int) -> Unit,
    onRemoveAds: () -> Unit,
    modifier: Modifier = Modifier
) {
    var toast by remember { mutableStateOf<String?>(null) }
    var pendingRealPurchase by remember { mutableStateOf<(() -> Unit)?>(null) }
    val freeCoinsLeft = (5 - gameState.freeCoinsClaimedToday).coerceAtLeast(0)

    if (toast != null) {
        androidx.compose.runtime.LaunchedEffect(toast) {
            kotlinx.coroutines.delay(1800); toast = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Шапка + балансы.
            Row(verticalAlignment = Alignment.CenterVertically) {
                KidsBackButton(onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Text("Магазин", fontFamily = UnboundedFamily, fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp, color = Kids.TextPrimary, modifier = Modifier.weight(1f))
                KidsChip("${gameState.coins}", Kids.CoinSoft, Kids.CoinText, leading = "🪙")
                Spacer(Modifier.width(6.dp))
                KidsChip("${gameState.gems}", Color(0xFFE1F2FF), Kids.GemShadow, leading = "💎")
            }

            // Бесплатные монеты (rewarded).
            KidsCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Kids.CoinSoft),
                        contentAlignment = Alignment.Center) { Text("🎁", fontSize = 26.sp) }
                    Column(Modifier.weight(1f)) {
                        Text("Бесплатные монеты", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = Kids.TextPrimary)
                        Text("+100 🪙 за рекламу · осталось $freeCoinsLeft из 5", fontFamily = RubikFamily,
                            fontSize = 12.sp, color = Kids.TextSecondary)
                    }
                    KidsButton(
                        onClick = {
                            if (freeCoinsLeft <= 0) toast = "На сегодня всё"
                            else onClaimFreeCoins { ok -> toast = if (ok) "+100 монет!" else "На сегодня всё" }
                        },
                        enabled = freeCoinsLeft > 0,
                        color = Kids.Coin, shadow = Kids.CoinShadow, contentColor = Kids.CoinText,
                        depth = 4.dp, cornerRadius = 14.dp
                    ) { Text("+100") }
                }
            }

            // Подсказки 2×2.
            SectionTitle("Подсказки")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SHOP_HINTS.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { type ->
                            HintShopCard(
                                type = type,
                                owned = gameState.powerUpInventory[type.id] ?: 0,
                                canAfford = gameState.coins >= type.price,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (gameState.coins < type.price) toast = "Не хватает монет"
                                else onPurchasePowerUp(type) { ok -> toast = if (ok) "Куплено!" else "Не хватает монет" }
                            }
                        }
                    }
                }
            }

            // Персонажи 3×2.
            SectionTitle("Персонажи")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ALL_AVATARS.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { avatar ->
                            val owned = avatar.id in gameState.ownedAvatars || avatar.price == 0
                            val selected = avatar.id == gameState.avatarId
                            AvatarShopCard(
                                emoji = avatar.emoji,
                                priceLabel = when {
                                    selected -> "Выбран"
                                    owned -> "Выбрать"
                                    else -> "${avatar.price} 🪙"
                                },
                                selected = selected,
                                canAfford = owned || gameState.coins >= avatar.price,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (!owned && gameState.coins < avatar.price) toast = "Не хватает монет"
                                else onPurchaseAvatar(avatar.id, avatar.price) { ok ->
                                    if (ok && !owned) toast = "Персонаж куплен!"
                                }
                            }
                        }
                    }
                }
            }

            // Кристаллы (реальные деньги → родительский барьер).
            SectionTitle("Кристаллы")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GEM_PACKS.forEach { pack ->
                    GemPackRow(pack) {
                        pendingRealPurchase = {
                            onGrantGems(pack.gems)
                            toast = "+${pack.gems} 💎"
                        }
                    }
                }
            }

            // Отключить рекламу.
            if (!gameState.adsRemoved) {
                KidsCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Kids.PrimarySoft),
                            contentAlignment = Alignment.Center) { Text("🚫", fontSize = 24.sp) }
                        Column(Modifier.weight(1f)) {
                            Text("Отключить рекламу", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, color = Kids.TextPrimary)
                            Text("Уберём баннер на главной", fontFamily = RubikFamily,
                                fontSize = 12.sp, color = Kids.TextSecondary)
                        }
                        KidsButton(onClick = {
                            pendingRealPurchase = { onRemoveAds(); toast = "Реклама отключена" }
                        }, depth = 4.dp, cornerRadius = 14.dp) { Text("199 ₽") }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Тост.
        toast?.let { msg ->
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
                    .clip(RoundedCornerShape(16.dp)).background(Kids.TextPrimary)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) { Text(msg, fontFamily = RubikFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                color = Color.White) }
        }

        // Родительский барьер перед покупкой за реальные деньги (§9.1).
        pendingRealPurchase?.let { action ->
            ParentalGate(
                onPass = { action(); pendingRealPurchase = null },
                onDismiss = { pendingRealPurchase = null }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp,
        color = Kids.TextPrimary)
}

@Composable
private fun HintShopCard(
    type: PowerUpType, owned: Int, canAfford: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    KidsCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(type.emoji, fontSize = 30.sp)
            Text(type.title, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = Kids.TextPrimary, textAlign = TextAlign.Center)
            Text("есть: $owned", fontFamily = RubikFamily, fontSize = 11.sp, color = Kids.TextMuted)
            KidsButton(
                onClick = onClick, modifier = Modifier.fillMaxWidth().alpha(if (canAfford) 1f else 0.5f),
                color = Kids.Coin, shadow = Kids.CoinShadow, contentColor = Kids.CoinText,
                depth = 4.dp, cornerRadius = 12.dp
            ) { Text("${type.price} 🪙") }
        }
    }
}

@Composable
private fun AvatarShopCard(
    emoji: String, priceLabel: String, selected: Boolean, canAfford: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Kids.Card)
            .border(3.dp, if (selected) Kids.Primary else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(vertical = 14.dp).alpha(if (canAfford) 1f else 0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 40.sp)
        Text(priceLabel, fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
            color = if (selected) Kids.Primary else Kids.TextSecondary)
    }
}

@Composable
private fun GemPackRow(pack: GemPack, onClick: () -> Unit) {
    KidsCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE1F2FF)),
                contentAlignment = Alignment.Center) { Text("💎", fontSize = 26.sp) }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${pack.gems} кристаллов", fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = Kids.TextPrimary)
                if (pack.badge != null) {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Kids.Success)
                        .padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(pack.badge, fontFamily = RubikFamily, fontWeight = FontWeight.Bold,
                            fontSize = 10.sp, color = Color.White)
                    }
                }
            }
            KidsButton(onClick = onClick, color = Kids.Gem, shadow = Kids.GemShadow,
                depth = 4.dp, cornerRadius = 14.dp) { Text(pack.price) }
        }
    }
}
