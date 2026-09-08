package com.example.sih_2026.core.constants

object Constants {
    // Replace with your PC's IP address from ipconfig
    private const val SERVER_IP = "172.30.254.239" 
    private const val PORT = "8000"

    const val BASE_URL = "http://$SERVER_IP:$PORT"
    const val WS_URL = "ws://$SERVER_IP:$PORT/ws/calls"
    
    const val ATTACK_LAB_ENDPOINT = "$BASE_URL/api/v1/attack-lab/start"
}
