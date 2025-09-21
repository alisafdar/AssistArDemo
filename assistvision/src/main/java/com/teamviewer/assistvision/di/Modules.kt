package com.teamviewer.assistvision.di

import com.teamviewer.assistvision.domain.usecase.DetectObjectsUseCase
import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import com.teamviewer.assistvision.services.ws.LocalMockWebSocket
import com.teamviewer.assistvision.services.ws.WebSocketDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        NativeObjectRecognitionService(androidContext())
    }
    factory { DetectObjectsUseCase(get()) }
    single<WebSocketDataSource> { LocalMockWebSocket() } bind WebSocketDataSource::class
}
