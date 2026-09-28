package com.example

import android.app.Application
import android.os.Build
import android.webkit.WebView

class MznliveApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = getProcessName()
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName)
                }
            }
        } catch (_: Throwable) {}
    }
}
