package com.teamviewer.assistvision.services.ws

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

interface WebSocketDataSource {
    val incoming: kotlinx.coroutines.flow.Flow<SocketMessage>
    fun send(message: SocketMessage)
    fun close()
}

class LocalMockWebSocket : WebSocketDataSource {
    private val ch = Channel<SocketMessage>(Channel.BUFFERED)
    override val incoming = ch.receiveAsFlow()

    override fun send(message: SocketMessage) {
        // Echo and augment
        ch.trySend(SocketMessage("echo", "local:${message.payload}"))
        if (message.type == "recognitionResult") {
            ch.trySend(SocketMessage("serverInfo", "Nice shot! Result=${message.payload}"))
        }
    }

    override fun close() { ch.close() }
}
