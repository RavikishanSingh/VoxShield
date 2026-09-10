package com.example.sih_2026.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*

class WebSocketManager(private val okHttpClient: OkHttpClient) {

    private val TAG = "WebSocketManager"
    private var webSocket: WebSocket? = null
    
    private val _events = MutableSharedFlow<RiskUpdate>(replay = 0)
    val events: SharedFlow<RiskUpdate> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val json = Json { ignoreUnknownKeys = true }

    fun connect(url: String) {
        _connectionState.value = ConnectionState.Connecting
        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected to $url")
                _connectionState.value = ConnectionState.Connected
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
                _connectionState.value = ConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Failure: ${t.message}")
                _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
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
        _connectionState.value = ConnectionState.Disconnected
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
