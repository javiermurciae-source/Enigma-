package com.enigma.mobile

import android.app.Application
import android.webkit.WebView

class EnigmaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(false)
    }
}