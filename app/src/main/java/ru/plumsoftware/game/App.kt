package ru.plumsoftware.game

import android.app.Application
import ru.plumsoftware.game.ads.AdsBase

class App : Application() {
    companion object {
        val adsBase: AdsBase = AdsBase.AdsGooglePlay()
    }
}