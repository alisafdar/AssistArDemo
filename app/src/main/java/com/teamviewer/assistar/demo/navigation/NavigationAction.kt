package com.teamviewer.assistar.demo.navigation

import androidx.navigation.NavOptions
import com.teamviewer.assistvision.gateway.domain.model.AssistArResult

open class NavRoute(
    val route: String
) {
    data object DashboardScreen : NavRoute("dashboardScreen")

    data object DemoActivity : NavRoute("demoActivity")
}

sealed interface NavigationAction {
    data object Back : NavigationAction

    data class Navigate(
        val destination: String,
        val navOptions: NavOptions = NavOptions.Builder()
            .build()
    ) : NavigationAction
}

suspend fun Navigator.pop() = invoke(NavigationAction.Back)

suspend fun Navigator.toDemoActivity() =
    invoke(
        NavigationAction.Navigate(
            destination = NavRoute.DemoActivity.route
        )
    )
