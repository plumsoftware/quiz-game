package ru.plumsoftware.game.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import ru.plumsoftware.game.App
import ru.plumsoftware.game.MainActivity
import ru.plumsoftware.game.ads.AdsManager
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.ui.components.game.CoinBadge
import ru.plumsoftware.game.ui.components.game.GameScreenTopBar
import ru.plumsoftware.game.ui.components.game.PowerUpShopCard
import ru.plumsoftware.game.ui.theme.*

@Composable
fun ShopScreen(
    coins: Int,
    inventory: Map<String, Int>,
    addCoins: (Int) -> Unit,
    onBack: () -> Unit,
    onPurchasePowerUp: (PowerUpType) -> Unit
) {
    val activity = LocalActivity.current ?: MainActivity()
    val adsManager = AdsManager(App.adsBase, activity)
    var adsWatchedToday by remember { mutableIntStateOf(0) }
    val maxAdsPerDay = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        GameScreenTopBar(
            title = "Магазин улучшений",
            onBack = onBack,
            actions = { CoinBadge(coins = coins) }
        )

        Text(
            text = "Покупай одноразовые подсказки и используй их в викторине",
            style = MaterialTheme.typography.bodyMedium,
            color = GameTextMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                AdRewardRow(
                    adsWatched = adsWatchedToday,
                    maxAds = maxAdsPerDay,
                    onWatchAd = {
                        if (adsWatchedToday < maxAdsPerDay) {
                            adsManager.showRewarded { reward ->
                                addCoins(if (reward == 1) 50 else reward)
                                adsWatchedToday++
                            }
                        }
                    },
                    enabled = adsWatchedToday < maxAdsPerDay
                )
            }

            items(PowerUpType.entries) { type ->
                PowerUpShopCard(
                    type = type,
                    owned = inventory[type.id] ?: 0,
                    canAfford = coins >= type.price,
                    onPurchase = { onPurchasePowerUp(type) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AdRewardRow(
    adsWatched: Int,
    maxAds: Int,
    onWatchAd: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = GameSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GameBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "🎬 Смотреть рекламу",
                    style = MaterialTheme.typography.titleSmall,
                    color = GameTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Осталось: ${maxAds - adsWatched}/$maxAds • +50 🪙",
                    style = MaterialTheme.typography.bodySmall,
                    color = GameTextMuted
                )
            }
            Button(
                onClick = onWatchAd,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = GamePurple)
            ) {
                Text("Смотреть")
            }
        }
    }
}
