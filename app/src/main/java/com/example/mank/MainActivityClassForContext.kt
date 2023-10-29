package com.example.mank

import android.app.Application
import android.content.Context

class MainActivityClassForContext : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        var appContext: Context? = null
    }
}