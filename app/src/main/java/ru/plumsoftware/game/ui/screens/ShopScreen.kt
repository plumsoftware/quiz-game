package ru.plumsoftware.game.ui.screens

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
    onBack: () -> Unit,
    onPurchase: (itemId: Int, price: Int) -> Unit
) {
    val shopItems = remember {
        listOf(
            ShopItem(
                id = 1,
                name = "Extra Lives",
                description = "Get 3 extra lives for difficult questions",
                price = 100,
                icon = Icons.Default.Favorite,
                color = Color(0xFFE91E63)
            ),
            ShopItem(
                id = 2,
                name = "Hint Pack",
                description = "Get 5 hints to help with tough questions",
                price = 150,
                icon = Icons.Default.Lightbulb,
                color = Color(0xFFFF9800)
            ),
            ShopItem(
                id = 3,
                name = "Double XP",
                description = "Earn double experience for 1 hour",
                price = 200,
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF4CAF50)
            ),
            ShopItem(
                id = 4,
                name = "Perfect Score Bonus",
                description = "Get 50% more coins for perfect scores",
                price = 300,
                icon = Icons.Default.Star,
                color = Color(0xFFFFD700)
            ),
            ShopItem(
                id = 5,
                name = "Unlock All Categories",
                description = "Access all question categories",
                price = 500,
                icon = Icons.Default.Category,
                color = Color(0xFF9C27B0)
            ),
            ShopItem(
                id = 6,
                name = "Daily Bonus",
                description = "Get 50 extra coins every day",
                price = 1000,
                icon = Icons.Default.CalendarToday,
                color = Color(0xFF2196F3)
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
                text = "Shop",
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
                        text = if (canAfford) "Buy" else "Not Enough",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 