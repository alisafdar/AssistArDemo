package com.teamviewer.assistvision.gateway.ui

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import com.teamviewer.assistvision.boot.TfLiteBoot
import com.teamviewer.assistvision.constants.FailureReasons
import com.teamviewer.assistvision.di.applicationModule
import com.teamviewer.assistvision.di.networkModule
import com.teamviewer.assistvision.di.useCasesModule
import com.teamviewer.assistvision.di.viewModelsModule
import com.teamviewer.assistvision.gateway.constants.ArgKeys.ASSIST_AR_KOIN_CONFIG_KEY
import com.teamviewer.assistvision.gateway.domain.model.AssistArConfig
import com.teamviewer.assistvision.gateway.domain.model.AssistArResult
import com.teamviewer.assistvision.ui.root.AssistArComposeApp
import com.teamviewer.assistvision.ui.theme.AssistArTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

internal interface BaseSimpleIdent {
    fun returnResult(result: AssistArResult)
}


internal fun BaseSimpleIdent.getModulesList() = listOf(
    applicationModule, networkModule, useCasesModule, viewModelsModule
)

internal fun BaseSimpleIdent.makeView(
    activity: Activity?, context: Context?, savedInstanceState: Bundle?, config: AssistArConfig
): View {

    if (context == null) {
        returnResult(makeInvalidContextResult())
        return View(context)
    }

    TfLiteBoot.initBlocking(context, preferGpu = true, timeoutMs = 500)
    TfLiteBoot.ensureNativeLibraryLoaded("assistvision")

    loadKoin(
        context, config
    )

    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    return ComposeView(context).apply {
        setContent {
            AssistArTheme(
                content = {
                    AssistArComposeApp(
                        onResult = {
                            unloadKoin()
                            returnResult(it)
                        })
                })
        }
    }
}

internal fun BaseSimpleIdent.loadKoin(context: Context, config: AssistArConfig) {
    try {
        loadKoinModules(getModulesList())
    } catch (e: Exception) {
        startKoin {
            androidContext(context)
            modules(getModulesList())
        }
    }

    GlobalContext.get().setProperty(ASSIST_AR_KOIN_CONFIG_KEY, config)

}

internal fun BaseSimpleIdent.unloadKoin() {
    try {
        unloadKoinModules(getModulesList())
    } catch (_: Exception) { // Ignoring if Koin is not running
    }
}

internal fun makeInvalidConfigResult(): AssistArResult = AssistArResult.Failed(
    reason = FailureReasons.INVALID_CONFIG, isRecoverable = false
)

internal fun makeInvalidContextResult(): AssistArResult = AssistArResult.Failed(
    reason = FailureReasons.INVALID_CONTEXT, isRecoverable = true
)
