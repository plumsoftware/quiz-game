package ru.plumsoftware.game.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import ru.plumsoftware.game.App
import ru.plumsoftware.game.MainActivity
import ru.plumsoftware.game.ads.AdsGooglePlay
import ru.plumsoftware.game.ads.AdsManager

data class ShopItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun ShopScreen(
    coins: Int,
    addCoins: (Int) -> Unit,
    onBack: () -> Unit,
    onPurchase: (itemId: Int, price: Int) -> Unit
) {
    val activity = LocalActivity.current ?: MainActivity()
    val adsManager = AdsManager(App.adsBase, activity)
    val displayAds by remember { mutableStateOf(Firebase.remoteConfig.getBoolean("display_ads")) }

    val shopItems = remember {
        listOf(
            ShopItem(
                id = 1,
                name = "Дополнительные жизни",
                description = "Получи 3 дополнительные жизни для сложных вопросов",
                price = 100,
                icon = Icons.Default.Favorite,
                color = Color(0xFFE91E63)
            ),
            ShopItem(
                id = 2,
                name = "Набор подсказок",
                description = "Получи 5 подсказок для сложных вопросов",
                price = 150,
                icon = Icons.Default.Lightbulb,
                color = Color(0xFFFF9800)
            ),
            ShopItem(
                id = 3,
                name = "Двойной опыт",
                description = "Получай двойной опыт на 1 час",
                price = 200,
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF4CAF50)
            ),
            ShopItem(
                id = 4,
                name = "Бонус за отличный результат",
                description = "Получай на 50% больше монет за отличные результаты",
                price = 300,
                icon = Icons.Default.Star,
                color = Color(0xFFFFD700)
            ),
            ShopItem(
                id = 5,
                name = "Разблокировать все категории",
                description = "Доступ ко всем категориям вопросов",
                price = 500,
                icon = Icons.Default.Category,
                color = Color(0xFF9C27B0)
            ),
            ShopItem(
                id = 6,
                name = "Ежедневный бонус",
                description = "Получай 50 дополнительных монет каждый день",
                price = 1000,
                icon = Icons.Default.CalendarToday,
                color = Color(0xFF2196F3)
            ),
            ShopItem(
                id = 7,
                name = "Золотой пропуск",
                description = "Разблокируй все уровни викторины сразу",
                price = 1500,
                icon = Icons.Default.VpnKey,
                color = Color(0xFFFF6B35)
            ),
            ShopItem(
                id = 8,
                name = "Неограниченные подсказки",
                description = "Получай неограниченное количество подсказок навсегда",
                price = 2000,
                icon = Icons.Default.AutoAwesome,
                color = Color(0xFF9C27B0)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Магазин",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = Color(0xFFFFD700)
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rewarded Ads Section
        if (App.adsBase == AdsGooglePlay()) {
            if (displayAds)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Watch Ad",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Смотри рекламу за награды",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Заработай дополнительные монеты (+50), посмотрев короткую рекламу",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                adsManager.showRewarded { reward ->
                                    addCoins(reward)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Смотреть рекламу")
                        }
                    }
                }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Watch Ad",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Смотри рекламу за награды",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Заработай дополнительные монеты (+50), посмотрев короткую рекламу",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            adsManager.showRewarded { reward ->
                                addCoins(reward)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Смотреть рекламу")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shop items
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(shopItems) { item ->
                ShopItemCard(
                    item = item,
                    canAfford = coins >= item.price,
                    onPurchase = onPurchase
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    canAfford: Boolean,
    onPurchase: (itemId: Int, price: Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = item.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = item.color
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Item details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Price and buy button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = "Price",
                        tint = if (canAfford) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${item.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color(0xFFFFD700) else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onPurchase(item.id, item.price) },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) item.color else Color.Gray
                    )
                ) {
                    Text(
                        text = if (canAfford) "Купить" else "Недостаточно",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 