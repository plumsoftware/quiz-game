package ru.plumsoftware.game.ui.components.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.plumsoftware.game.ads.AdsManager
import ru.plumsoftware.game.ui.theme.*

fun normalizeAdReward(reward: Int): Int = if (reward == 1) 50 else reward

@Composable
fun rememberAdsManager(activity: android.app.Activity): AdsManager {
    return remember(activity) { AdsManager(ru.plumsoftware.game.App.adsBase, activity) }
}

@Composable
fun GameAdOverlays(
    isAdLoading: Boolean,
    rewardCoins: Int?,
    onDismissReward: () -> Unit
) {
    if (isAdLoading) {
        AdLoadingDialog()
    }
    rewardCoins?.let { coins ->
        CoinRewardDialog(coins = coins, onDismiss = onDismissReward)
    }
}

fun AdsManager.watchRewardedForCoins(
    onCoinsGranted: (Int) -> Unit,
    onRewardDialog: (Int) -> Unit
) {
    var pendingCoins: Int? = null
    showRewarded(
        onGotRewarded = { reward ->
            val coins = normalizeAdReward(reward)
            pendingCoins = coins
            onCoinsGranted(coins)
        },
        onDismissed = {
            pendingCoins?.let(onRewardDialog)
            pendingCoins = null
        }
    )
}

@Composable
private fun AdLoadingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GameSurface,
            border = BorderStroke(1.dp, GameBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(44.dp),
                    color = GamePurple,
                    trackColor = GameBorder,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "Загрузка рекламы",
                    style = MaterialTheme.typography.titleMedium,
                    color = GameTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Подождите немного…",
                    style = MaterialTheme.typography.bodySmall,
                    color = GameTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CoinRewardDialog(
    coins: Int,
    onDismiss: () -> Unit
) {
    val scale by rememberInfiniteTransition(label = "rewardPulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coinScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GameSurface,
            border = BorderStroke(1.dp, GameGold.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GameGold.copy(alpha = 0.08f),
                                GameSurface
                            )
                        )
                    )
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(scale)
                        .background(GameGold.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪙", style = MaterialTheme.typography.displaySmall)
                }

                Text(
                    text = "Награда получена!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GameTextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = GameGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GameGold.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "+$coins монет",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = GameGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Монеты уже на твоём счёте",
                    style = MaterialTheme.typography.bodySmall,
                    color = GameTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GamePurple)
                ) {
                    Text("Забрать", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
