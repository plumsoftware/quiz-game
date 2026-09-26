package ru.plumsoftware.game.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.YandexAds
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader

/** Инициализация SDK с настройками для детской аудитории (ТЗ §8). */
object KidsAds {
    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        // Детская аудитория, без персонализации и геолокации (SDK 8: YandexAds вместо MobileAds).
        YandexAds.setAgeRestricted(true)
        YandexAds.setUserConsent(false)
        YandexAds.setLocationTracking(false)
        YandexAds.initialize(context.applicationContext) {}
    }
}

/**
 * Rewarded-видео с предзагрузкой: кнопка активна, только когда видео загружено
 * («Видео пока нет» — иначе). Награда выдаётся строго в onRewarded (§8).
 */
class RewardedAdController(
    private val activity: Activity,
    private val adUnitId: String
) {
    var isLoaded by mutableStateOf(false)
        private set

    /** Видео сейчас на экране — на это время глушим музыку (§10). */
    var isShowing by mutableStateOf(false)
        private set

    private var isLoading = false
    private var ad: RewardedAd? = null
    private var destroyed = false
    private val handler = Handler(Looper.getMainLooper())

    // SDK 8: слушатель передаётся прямо в loadAd, setAdLoadListener больше нет.
    private val loader = RewardedAdLoader(activity)

    private val loadListener = object : RewardedAdLoadListener {
        override fun onAdLoaded(rewardedAd: RewardedAd) {
            isLoading = false
            ad = rewardedAd
            isLoaded = true
        }

        override fun onAdFailedToLoad(adRequestError: AdRequestError) {
            isLoading = false
            isLoaded = false
            // Повторим позже, без спама запросами.
            if (!destroyed) handler.postDelayed({ load() }, 30_000L)
        }
    }

    fun load() {
        if (destroyed || isLoading || isLoaded) return
        isLoading = true
        loader.loadAd(AdRequest.Builder(adUnitId).build(), loadListener)
    }

    /**
     * Показывает видео. [onRewarded] вызывается только при полном просмотре.
     * @return false — видео ещё не загружено.
     */
    fun show(onRewarded: () -> Unit, onClosed: () -> Unit = {}): Boolean {
        val current = ad ?: return false
        ad = null
        isLoaded = false
        isShowing = true
        current.setAdEventListener(object : RewardedAdEventListener {
            override fun onAdShown() {}

            override fun onAdFailedToShow(adError: AdError) {
                current.setAdEventListener(null)
                isShowing = false
                onClosed()
                load()
            }

            override fun onAdDismissed() {
                current.setAdEventListener(null)
                isShowing = false
                onClosed()
                load()
            }

            override fun onAdClicked() {}

            override fun onAdImpression(impressionData: ImpressionData?) {}

            override fun onRewarded(reward: Reward) {
                onRewarded()
            }
        })
        current.show(activity)
        return true
    }

    fun destroy() {
        destroyed = true
        handler.removeCallbacksAndMessages(null)
        ad?.setAdEventListener(null)
        ad = null
    }
}
