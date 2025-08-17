package ru.plumsoftware.game.ads

class AdsGooglePlay : AdsBase(
    interstitialAdsId = "R-M-16662673-1",
    rewardedAdsId = "R-M-16662673-2",
    openAppAdsId = ""
) {
    override fun equals(other: Any?): Boolean {
        if (other !is AdsGooglePlay)
            return false
        return other.rewardedAdsId == this.rewardedAdsId
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}