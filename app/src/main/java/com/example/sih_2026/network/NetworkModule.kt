package com.example.sih_2026.network

import okhttp3.OkHttpClient

object NetworkModule {
    private val client = OkHttpClient()
    
    val webSocketManager by lazy {
        WebSocketManager(client)
    }
}
