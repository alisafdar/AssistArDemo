package com.teamviewer.assistar.demo.ui.dashboard

import androidx.annotation.DrawableRes

data class DashboardScreenUiState(
    val welcomeTitle: String = "",
    val buttonTitle: String = "",
    @DrawableRes val logo: Int? = null,
    val logoDescription: String = "",
)