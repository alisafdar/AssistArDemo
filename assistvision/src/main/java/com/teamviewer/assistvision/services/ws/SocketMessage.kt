package com.teamviewer.assistvision.services.ws

import kotlinx.serialization.Serializable

@Serializable
data class SocketMessage(
    val type: String,
    val payload: String
)
