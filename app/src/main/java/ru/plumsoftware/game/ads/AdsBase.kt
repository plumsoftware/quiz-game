package ru.plumsoftware.game.ads

sealed class AdsBase(
    val interstitialAdsId: String,
    val rewardedAdsId: String,
    val openAppAdsId: String
) {
    data class AdsRuStore(val a: Int = 1) : AdsBase(
        interstitialAdsId = "R-M-16621058-2",
        rewardedAdsId = "R-M-16621058-1",
        openAppAdsId = ""
    )

    data class AdsHuaweiAppGallery(val a: Int = 1) : AdsBase(
        interstitialAdsId = "R-M-16661603-1",
        rewardedAdsId = "R-M-16661603-2",
        openAppAdsId = ""
    )

    data class AdsGooglePlay(val a: Int = 1) : AdsBase(
        interstitialAdsId = "R-M-16662673-1",
        rewardedAdsId = "R-M-16662673-2",
        openAppAdsId = ""
    )
}
