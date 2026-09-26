package ru.plumsoftware.game.data

/**
 * Товары за кристаллы (реальных покупок в игре нет).
 * Курс подобран так, чтобы кристаллы были «дороже» монет (~17–20 🪙 за 1 💎)
 * и не обесценивали монетную экономику §7: 💎 приходят медленно — сундуки, боссы,
 * достижения и два «трудных» задания дня.
 */
enum class GemShopItem(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val iconKey: String,
    val emoji: String
) {
    STREAK_FREEZE("streak_freeze", "Заморозка серии", "Сохранит серию, если пропустишь день", Economy.STREAK_FREEZE_PRICE_GEMS, "streak_freeze", "🧊"),
    HINT_PACK("hint_pack", "Набор подсказок", "2× 50/50, 1× Стоп, 1× Пропуск", 30, "map_chest", "🎁"),
    LIFE_PACK("life_pack", "Две доп. жизни", "2× ❤️ для трудных уровней", 25, "hint_life", "❤️"),
    COIN_BAG("coin_bag", "Мешок монет", "+400 монет сразу", 20, "currency_coin", "🪙");

    companion object {
        const val COIN_BAG_AMOUNT = 400
    }
}
