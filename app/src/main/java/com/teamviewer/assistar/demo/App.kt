package com.teamviewer.assistar.demo

import android.app.Application
import com.teamviewer.assistvision.ui.detect.DetectViewModel
import com.teamviewer.assistvision.boot.TfLiteBoot
import com.teamviewer.assistvision.di.appModule
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin { modules(appModule + module {
            single { applicationContext }
            viewModelOf(::DetectViewModel)
        }) }

        TfLiteBoot.initBlocking(this, preferGpu = true, timeoutMs = 500)
        TfLiteBoot.ensureNativeLibraryLoaded("assistvision")
    }
}
