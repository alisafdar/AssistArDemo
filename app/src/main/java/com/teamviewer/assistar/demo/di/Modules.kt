package com.teamviewer.assistar.demo.di

import com.teamviewer.assistar.demo.navigation.Navigator
import com.teamviewer.assistar.demo.navigation.NavigatorImpl
import com.teamviewer.assistar.demo.ui.dashboard.DashboardScreenViewModel
import com.teamviewer.assistar.demo.utils.ResourceProvider
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val applicationModule =
    module {
        singleOf(::ResourceProvider)
        singleOf(::NavigatorImpl) { bind<Navigator>() }
    }

val viewModelsModule = module {
    viewModelOf(::DashboardScreenViewModel)
}