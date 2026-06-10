package ru.plumsoftware.game.ads

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader

class AdsManager(
    private val adsBase: AdsBase,
    private val activity: Activity
) {

    var isAdLoading by mutableStateOf(false)
        private set

    class Builder {
        private var adsBase: AdsBase? = null
        private var activity: Activity? = null

        fun activity(activity: Activity): Builder {
            this.activity = activity
            MobileAds.initialize(activity.baseContext) {}
            return this
        }

        fun withAds(adsBase: AdsBase): Builder {
            this.adsBase = adsBase
            return this
        }

        fun build(): AdsManager {
            requireNotNull(activity) { "Activity must be set before building AdsManager" }
            requireNotNull(adsBase) { "AdsBase must be set before building AdsManager" }
            return AdsManager(adsBase!!, activity!!)
        }
    }

    fun showInterstitial(onAdDismissed: () -> Unit) {
        isAdLoading = true
        val interstitialAdsLoader = InterstitialAdLoader(activity).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    isAdLoading = false
                    showAd(interstitialAd, this@apply, onAdDismissed)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    isAdLoading = false
                    onAdDismissed()
                }
            })
        }
        loadInterstitialAd(interstitialAdsLoader)
    }

    private fun loadInterstitialAd(interstitialAdsLoader: InterstitialAdLoader) {
        val adRequestConfiguration =
            AdRequestConfiguration.Builder(adsBase.interstitialAdsId).build()
        interstitialAdsLoader.loadAd(adRequestConfiguration)
    }

    private fun showAd(
        interstitialAds: InterstitialAd,
        interstitialAdsLoader: InterstitialAdLoader,
        onAdDismissed: () -> Unit
    ) {
        interstitialAds.apply {
            setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {}
                override fun onAdFailedToShow(adError: AdError) {
                    interstitialAds.setAdEventListener(null)
                    loadInterstitialAd(interstitialAdsLoader)
                }

                override fun onAdDismissed() {
                    onAdDismissed()
                }

                override fun onAdClicked() {
                    onAdDismissed()
                }

                override fun onAdImpression(impressionData: ImpressionData?) {}
            })
            show(activity)
        }
    }

    fun showOpen() {
        isAdLoading = true
        val appOpenAdLoader = AppOpenAdLoader(activity.baseContext)
        val adRequestConfiguration = AdRequestConfiguration.Builder(adsBase.openAppAdsId).build()

        val appOpenAdLoadListener = object : AppOpenAdLoadListener {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                isAdLoading = false
                appOpenAd.setAdEventListener(object : AppOpenAdEventListener {
                    override fun onAdClicked() {}

                    override fun onAdDismissed() {}

                    override fun onAdFailedToShow(adError: AdError) {
                        loadOpenAd(
                            appOpenAdLoader, adRequestConfiguration
                        )
                    }

                    override fun onAdImpression(impressionData: ImpressionData?) {}

                    override fun onAdShown() {}
                })
                if (adsBase.openAppAdsId != "")
                    appOpenAd.show(activity)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                isAdLoading = false
            }
        }
        appOpenAdLoader.setAdLoadListener(appOpenAdLoadListener)
        loadOpenAd(
            appOpenAdLoader, adRequestConfiguration
        )
    }

    private fun loadOpenAd(
        appOpenAdLoader: AppOpenAdLoader,
        adRequestConfiguration: AdRequestConfiguration
    ) {
        appOpenAdLoader.loadAd(adRequestConfiguration)
    }

    fun showRewarded(
        onGotRewarded: (Int) -> Unit,
        onDismissed: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        isAdLoading = true

        val rewardedAdLoader: RewardedAdLoader = RewardedAdLoader(context = activity).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewarded: RewardedAd) {
                    isAdLoading = false
                    rewarded.apply {
                        setAdEventListener(object : RewardedAdEventListener {
                            override fun onAdShown() {}

                            override fun onAdFailedToShow(adError: AdError) {
                                isAdLoading = false
                                rewarded.setAdEventListener(null)
                                onFailed?.invoke()
                            }

                            override fun onAdDismissed() {
                                rewarded.setAdEventListener(null)
                                onDismissed?.invoke()
                            }

                            override fun onAdClicked() {}

                            override fun onAdImpression(impressionData: ImpressionData?) {}

                            override fun onRewarded(reward: Reward) {
                                onGotRewarded(reward.amount)
                            }
                        })
                        show(activity)
                    }
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    isAdLoading = false
                    onFailed?.invoke()
                }
            })
        }
        loadRewardedAd(rewardedAdLoader)
    }

    private fun loadRewardedAd(rewardedAdLoader: RewardedAdLoader) {
        val adRequestConfiguration = AdRequestConfiguration.Builder(adsBase.rewardedAdsId).build()
        rewardedAdLoader.loadAd(adRequestConfiguration)
    }
}