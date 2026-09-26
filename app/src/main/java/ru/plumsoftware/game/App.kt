package ru.plumsoftware.game

import android.app.Application
import ru.plumsoftware.game.ads.KidsAds

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KidsAds.init(this)
    }
}
