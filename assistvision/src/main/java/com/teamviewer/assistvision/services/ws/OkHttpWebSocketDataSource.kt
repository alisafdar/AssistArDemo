package com.teamviewer.assistvision.services.ws

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*
import okio.ByteString

class OkHttpWebSocketDataSource(
    private val url: String
) : WebSocketDataSource, WebSocketListener() {
    private val client = OkHttpClient()
    private var socket: WebSocket? = null
    private val ch = Channel<SocketMessage>(Channel.BUFFERED)
    override val incoming = ch.receiveAsFlow()

    init {
        val req = Request.Builder().url(url).build()
        socket = client.newWebSocket(req, this)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        ch.trySend(SocketMessage("message", text))
    }
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        ch.trySend(SocketMessage("binary", bytes.hex()))
    }
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        ch.close()
    }
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        ch.close(t)
    }

    override fun send(message: SocketMessage) {
        socket?.send("${message.type}:${message.payload}")
    }

    override fun close() {
        socket?.close(1000, "bye")
        client.dispatcher.executorService.shutdown()
    }
}
