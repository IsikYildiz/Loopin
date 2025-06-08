package com.example.loopin

import android.app.Application
import com.example.loopin.PreferenceManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceManager.init(applicationContext)
    }
}