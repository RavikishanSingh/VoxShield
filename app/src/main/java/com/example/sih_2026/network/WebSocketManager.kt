package com.example.sih_2026.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString

class WebSocketManager(private val okHttpClient: OkHttpClient) {

    private val TAG = "WebSocketManager"
    private var webSocket: WebSocket? = null
    
    private val _events = MutableSharedFlow<RiskUpdate>(replay = 0)
    val events: SharedFlow<RiskUpdate> = _events

    private val json = Json { ignoreUnknownKeys = true }

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected to $url")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val riskUpdate = json.decodeFromString<RiskUpdate>(text)
                    _events.tryEmit(riskUpdate)
                    Log.d(TAG, "Risk Update received: ${riskUpdate.risk.score}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding risk update: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closing: $code / $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Failure: ${t.message}")
            }
        })
    }

    fun sendAudio(chunk: AudioChunk) {
        val message = json.encodeToString(chunk)
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
}
