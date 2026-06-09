package ru.plumsoftware.game.ui.components.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.plumsoftware.game.data.PowerUpType
import ru.plumsoftware.game.ui.theme.*

@Composable
fun HomePowerUpShopSection(
    coins: Int,
    inventory: Map<String, Int>,
    onOpenFullShop: () -> Unit,
    onPurchase: (PowerUpType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "🛍 Магазин улучшений",
                    style = MaterialTheme.typography.titleMedium,
                    color = GameTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Одноразовые подсказки и бонусы",
                    style = MaterialTheme.typography.bodySmall,
                    color = GameTextMuted
                )
            }
            TextButton(onClick = onOpenFullShop) {
                Text("Все", color = GamePurpleLight)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(PowerUpType.entries.take(6)) { type ->
                val owned = inventory[type.id] ?: 0
                CompactPowerUpCard(
                    type = type,
                    owned = owned,
                    canAfford = coins >= type.price,
                    onPurchase = { onPurchase(type) }
                )
            }
        }
    }
}

@Composable
fun PowerUpShopOverlay(
    coins: Int,
    inventory: Map<String, Int>,
    onClose: () -> Unit,
    onPurchase: (PowerUpType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground.copy(alpha = 0.97f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, "Закрыть", tint = GameTextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Магазин",
                        style = MaterialTheme.typography.titleLarge,
                        color = GameTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Викторина на паузе ⏸",
                        style = MaterialTheme.typography.bodySmall,
                        color = GameStreak
                    )
                }
                CoinBadge(coins = coins)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PowerUpType.entries) { type ->
                    PowerUpShopCard(
                        type = type,
                        owned = inventory[type.id] ?: 0,
                        canAfford = coins >= type.price,
                        onPurchase = { onPurchase(type) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun QuizPowerUpBar(
    inventory: Map<String, Int>,
    enabled: Boolean,
    onUse: (PowerUpType) -> Unit,
    onOpenShop: () -> Unit
) {
    val ownedTypes = PowerUpType.entries.filter { (inventory[it.id] ?: 0) > 0 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            onClick = onOpenShop,
            shape = RoundedCornerShape(12.dp),
            color = GameGold.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, GameGold.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.ShoppingCart, null, tint = GameGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Магазин", style = MaterialTheme.typography.labelSmall, color = GameGold)
            }
        }

        if (ownedTypes.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ownedTypes) { type ->
                    val count = inventory[type.id] ?: 0
                    PowerUpUseChip(
                        type = type,
                        count = count,
                        enabled = enabled,
                        onClick = { onUse(type) }
                    )
                }
            }
        } else {
            Text(
                "Купи улучшения в магазине",
                style = MaterialTheme.typography.labelSmall,
                color = GameTextMuted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactPowerUpCard(
    type: PowerUpType,
    owned: Int,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    Surface(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(16.dp),
        color = type.color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, type.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Text(type.emoji, style = MaterialTheme.typography.headlineSmall)
                if (owned > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(GamePurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$owned",
                            style = MaterialTheme.typography.labelSmall,
                            color = GameTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                type.title,
                style = MaterialTheme.typography.labelSmall,
                color = GameTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "🪙 ${type.price}",
                style = MaterialTheme.typography.labelSmall,
                color = if (canAfford) GameGold else GameTextDisabled
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilledTonalButton(
                onClick = onPurchase,
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 2.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GamePurple.copy(alpha = 0.3f)
                )
            ) {
                Text("Купить", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun PowerUpShopCard(
    type: PowerUpType,
    owned: Int,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GameSurface,
        border = BorderStroke(1.dp, GameBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(type.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(type.emoji, style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        type.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = GameTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (owned > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GamePurple.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "×$owned",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = GamePurpleLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GameTextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🪙${type.price}", style = MaterialTheme.typography.labelMedium, color = GameGold)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onPurchase,
                    enabled = canAfford,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GamePurple)
                ) {
                    Text("+1", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun PowerUpUseChip(
    type: PowerUpType,
    count: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled && count > 0,
        shape = RoundedCornerShape(12.dp),
        color = type.color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, type.color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(type.emoji, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                color = GameTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
