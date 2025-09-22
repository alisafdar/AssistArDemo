package com.teamviewer.assistvision.ui.navigation

import androidx.navigation.NavOptions
import com.teamviewer.assistvision.gateway.domain.model.AssistArResult

open class NavRoute(
    val route: String
) {
    data object DetectScreen : NavRoute("detectScreen")
}

sealed interface NavigationAction {
    data object Back : NavigationAction

    data class Navigate(
        val destination: String,
        val navOptions: NavOptions = NavOptions.Builder()
            .build()
    ) : NavigationAction

    data class Quit(
        val assistArResult: AssistArResult
    ) : NavigationAction

    data object CameraSettings : NavigationAction
}

suspend fun Navigator.pop() = invoke(NavigationAction.Back)

suspend fun Navigator.toDetectScreen() =
    invoke(
        NavigationAction.Navigate(
            destination = NavRoute.DetectScreen.route
        )
    )
