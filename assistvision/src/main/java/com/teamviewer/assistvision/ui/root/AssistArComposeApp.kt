package com.teamviewer.assistvision.ui.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teamviewer.assistvision.gateway.domain.model.AssistArResult
import com.teamviewer.assistvision.ui.navigation.NavRoute
import com.teamviewer.assistvision.ui.navigation.NavigationAction
import com.teamviewer.assistvision.ui.screens.detect.DetectScreen
import com.teamviewer.assistvision.ui.navigation.Navigator
import com.teamviewer.assistvision.ui.shared.AppBar
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
internal fun AssistArComposeApp(
    navigator: Navigator = koinInject(),
    onResult: (AssistArResult) -> Unit
) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        navigator.navActions.collectLatest { navigationAction ->
            when (navigationAction) {
                is NavigationAction.Navigate -> {
                    if (navController.currentDestination?.route == navigationAction.destination) {
                        return@collectLatest
                    }
                    navController.navigate(
                        route = navigationAction.destination,
                        builder = {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                }

                is NavigationAction.Back ->
                    navController.popBackStack()

                is NavigationAction.Quit ->
                    onResult(navigationAction.assistArResult)

                NavigationAction.CameraSettings -> {}
            }
        }
    }

    Box(
        content = {
            Column {
                AppBar(
                    visible = true,
                    onClose = {
                        onResult(AssistArResult.Cancelled)
                    }
                )

                NavHost(
                    navController = navController,
                    startDestination = NavRoute.DetectScreen.route,
                    builder = {
                        composable(
                            route = NavRoute.DetectScreen.route,
                            content = {
                                DetectScreen()
                            }
                        )
                    }
                )
            }
        }
    )
}
