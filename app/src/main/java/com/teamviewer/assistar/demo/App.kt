package com.teamviewer.assistar.demo

import android.app.Application
import com.teamviewer.assistar.demo.di.applicationModule
import com.teamviewer.assistar.demo.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                applicationModule,
                viewModelsModule
            )
        }
    }
}
