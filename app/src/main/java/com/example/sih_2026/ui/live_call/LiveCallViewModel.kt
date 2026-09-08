package com.example.sih_2026.ui.live_call

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih_2026.core.constants.Constants
import com.example.sih_2026.network.NetworkModule
import com.example.sih_2026.webrtc.WebRTCManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class LiveCallUiState(
    val callerNumber: String = "LIVE MONITORING",
    val duration: String = "00:00",
    val voiceIntegrity: Float = 1.0f,
    val speakerMatch: Float = 1.0f,
    val syntheticVoice: Float = 0.0f,
    val replayProb: Float = 0.0f,
    val conversationRisk: Int = 0,
    val detectedIntent: String = "None",
    val transcriptLog: List<String> = emptyList(),
    val riskScore: Int = 0,
    val riskLevel: String = "LOW",
    val connectionStatus: String = "Disconnected",
    val audioEnergy: Float = 0.0f,
    val challengeQuestion: String? = null
)

class LiveCallViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LiveCallUiState())
    val uiState: StateFlow<LiveCallUiState> = _uiState.asStateFlow()
    
    private val wsManager = NetworkModule.webSocketManager
    private val webRTCManager = WebRTCManager(application)

    init {
        observeWebSocket()
        observeAudioEnergy()
    }

    private fun observeWebSocket() {
        wsManager.events.onEach { update ->
            val currentLog = _uiState.value.transcriptLog.toMutableList()
            update.context.reason_codes.forEach { code ->
                if (code !in currentLog) currentLog.add(code)
            }
            if (currentLog.size > 10) currentLog.removeAt(0)

            val challenge = if (update.risk.score > 60 || update.voice.synthetic > 0.7f) {
                "⚠️ Challenge: Ask the caller about a shared family event or a genuine previous transaction."
            } else {
                null
            }

            _uiState.value = _uiState.value.copy(
                riskScore = update.risk.score,
                riskLevel = update.risk.level,
                syntheticVoice = update.voice.synthetic,
                speakerMatch = update.voice.speaker_similarity,
                replayProb = update.voice.replay,
                detectedIntent = if (update.context.financial_intent) "Financial Fraud" else "None",
                transcriptLog = currentLog,
                connectionStatus = "Active",
                challengeQuestion = challenge
            )
        }.launchIn(viewModelScope)
    }

    private fun observeAudioEnergy() {
        webRTCManager.audioEnergyLevel.onEach { energy ->
            _uiState.value = _uiState.value.copy(audioEnergy = energy)
        }.launchIn(viewModelScope)
    }

    fun startMonitoring(isTestClone: Boolean = false) {
        val callId = if (isTestClone) "TEST-CLONE" else "LIVE-${System.currentTimeMillis() % 10000}"
        wsManager.connect("${Constants.WS_URL}/$callId")
        webRTCManager.startCall(isCaller = false) { }
        _uiState.value = _uiState.value.copy(connectionStatus = "Connecting...")
    }

    fun stopMonitoring() {
        wsManager.disconnect()
        webRTCManager.endCall()
        _uiState.value = _uiState.value.copy(connectionStatus = "Disconnected")
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
