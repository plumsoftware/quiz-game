package ru.plumsoftware.game

/**
 * Параметры площадки публикации. Значения задаются во flavor в app/build.gradle.kts
 * (buildConfigField), поэтому для новой площадки достаточно добавить ещё один productFlavor.
 */
object StoreConfig {
    /** ID баннера 320×50 на главной (§8, место №1). */
    val bannerAdUnitId: String get() = BuildConfig.BANNER_AD_UNIT_ID

    /** ID rewarded-видео («Удвоить награду», «Бесплатные монеты»). */
    val rewardedAdUnitId: String get() = BuildConfig.REWARDED_AD_UNIT_ID

    /** Страница приложения в магазине — для «Оценить приложение». */
    val storeAppUrl: String get() = BuildConfig.STORE_APP_URL

    /** Название магазина для подписей в интерфейсе. */
    val storeName: String get() = BuildConfig.STORE_NAME
}
