@file:Suppress("ktlint:standard:filename")

package com.teamviewer.assistar.demo.ui.shared

interface UIEvent

fun interface UIEventListener {
    fun onUIEvent(uiEvent: UIEvent)
}
