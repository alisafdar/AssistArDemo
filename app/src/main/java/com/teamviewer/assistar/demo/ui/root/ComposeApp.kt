package com.teamviewer.assistar.demo.ui.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teamviewer.assistar.demo.navigation.NavRoute
import com.teamviewer.assistar.demo.navigation.NavigationAction
import com.teamviewer.assistar.demo.navigation.Navigator
import com.teamviewer.assistar.demo.ui.dashboard.DashboardScreen
import com.teamviewer.assistar.demo.ui.demo.DemoActivity
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun ComposeApp(navigator: Navigator = koinInject()) {
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
            }
        }
    }

    Scaffold(
        content = { padding ->
            Column(
                modifier =
                Modifier
                    .padding(padding),
                content = {
                    NavHost(
                        navController = navController,
                        startDestination = NavRoute.DashboardScreen.route
                    ) {
                        composable(NavRoute.DashboardScreen.route) {
                            DashboardScreen()
                        }

                        activity(NavRoute.DemoActivity.route) {
                            this.activityClass = DemoActivity::class
                        }
                    }
                }
            )
        }
    )
}
