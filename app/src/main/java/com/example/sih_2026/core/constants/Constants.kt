package com.example.sih_2026.core.constants

object Constants {
    // Use 127.0.0.1 with ADB reverse port forwarding (configured via adb reverse tcp:8000 tcp:8000).
    private const val SERVER_IP = "127.0.0.1" 
    private const val PORT = "8000"

    const val BASE_URL = "http://$SERVER_IP:$PORT"
    const val WS_URL = "ws://$SERVER_IP:$PORT/ws/calls"
    
    const val ATTACK_LAB_ENDPOINT = "$BASE_URL/api/v1/attack-lab/start"
}
