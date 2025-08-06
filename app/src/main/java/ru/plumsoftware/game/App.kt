package ru.plumsoftware.game

import android.app.Application
import ru.plumsoftware.game.ads.AdsRuStore

class App : Application() {
    companion object {
        val adsBase = AdsRuStore()
    }
}