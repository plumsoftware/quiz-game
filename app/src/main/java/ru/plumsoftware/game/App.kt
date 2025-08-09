package ru.plumsoftware.game

import android.app.Application
import ru.plumsoftware.game.ads.AdsHuaweiAppGallery

class App : Application() {
    companion object {
        val adsBase = AdsHuaweiAppGallery()
    }
}