package com.teamviewer.assistar.demo.ui.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface Navigator {
    val navActions: Flow<NavigationAction>

    suspend operator fun invoke(navAction: NavigationAction)
}

class NavigatorImpl : Navigator {
    private val _navActions = Channel<NavigationAction>()
    override val navActions: Flow<NavigationAction> = _navActions.receiveAsFlow()

    override suspend operator fun invoke(navAction: NavigationAction) {
        _navActions.send(navAction)
    }
}
