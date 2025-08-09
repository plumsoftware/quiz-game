package ru.plumsoftware.game

import android.app.Application
import ru.plumsoftware.game.ads.AdsGooglePlay

class App : Application() {
    companion object {
        val adsBase = AdsGooglePlay()
    }
}