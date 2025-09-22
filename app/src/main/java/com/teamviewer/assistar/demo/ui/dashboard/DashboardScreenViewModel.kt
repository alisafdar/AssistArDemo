package com.teamviewer.assistar.demo.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamviewer.assistar.demo.R
import com.teamviewer.assistar.demo.navigation.Navigator
import com.teamviewer.assistar.demo.navigation.toDemoActivity
import com.teamviewer.assistar.demo.ui.shared.UIEvent
import com.teamviewer.assistar.demo.ui.shared.UIEventListener
import com.teamviewer.assistar.demo.utils.ResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardScreenViewModel(
    private val navigator: Navigator,
    private val resourceProvider: ResourceProvider
) : ViewModel(), UIEventListener {
    private val _uiState = MutableStateFlow(DashboardScreenUiState())
    val uiState: StateFlow<DashboardScreenUiState> get() = _uiState

    init {
        _uiState.update {
            it.copy(
                welcomeTitle = resourceProvider.getString(R.string.welcome_to_assistar),
                buttonTitle = resourceProvider.getString(R.string.button_start_assistar),
                logo = R.drawable.assist_ar_logo,
                logoDescription = resourceProvider.getString(R.string.assistar_logo_description)
            )
        }
    }

    private fun startDemoActivity(){
        viewModelScope.launch {
            navigator.toDemoActivity()
        }
    }

    override fun onUIEvent(uiEvent: UIEvent) {
        when (uiEvent) {
            is UIEventStartAssistAr -> startDemoActivity()
        }
    }
}