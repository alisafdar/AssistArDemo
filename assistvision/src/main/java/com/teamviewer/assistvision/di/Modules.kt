package com.teamviewer.assistvision.di

import com.teamviewer.assistvision.domain.service.ObjectRecognitionService
import com.teamviewer.assistvision.domain.usecase.DetectObjectsUseCase
import com.teamviewer.assistvision.navigation.Navigator
import com.teamviewer.assistvision.navigation.NavigatorImpl
import com.teamviewer.assistvision.usecase.DetectObjectsUseCaseImpl
import com.teamviewer.assistvision.services.nativebridge.NativeObjectRecognitionService
import com.teamviewer.assistvision.services.ws.LocalMockWebSocket
import com.teamviewer.assistvision.services.ws.WebSocketDataSource
import com.teamviewer.assistvision.ui.detect.DetectViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val applicationModule = module {
    singleOf(::NativeObjectRecognitionService) { bind<ObjectRecognitionService>() }
    singleOf(::NavigatorImpl) { bind<Navigator>() }
}

val networkModule = module{
    singleOf(::LocalMockWebSocket) { bind<WebSocketDataSource>() }
}

val useCasesModule = module{
    factoryOf(::DetectObjectsUseCaseImpl) { bind<DetectObjectsUseCase>() }
}

val viewModelsModule = module{
    viewModelOf(::DetectViewModel)
}
