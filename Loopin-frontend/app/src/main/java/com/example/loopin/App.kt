package com.example.loopin

import android.app.Application
import com.example.loopin.PreferenceManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceManager.init(applicationContext)
    }
}