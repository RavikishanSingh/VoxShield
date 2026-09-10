package com.example.sih_2026.ui.live_call

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih_2026.core.constants.Constants
import com.example.sih_2026.network.ConnectionState
import com.example.sih_2026.network.NetworkModule
import com.example.sih_2026.webrtc.WebRTCManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
    private var simulationJob: Job? = null
    private var connectionTimeoutJob: Job? = null

    init {
        observeWebSocket()
        observeConnectionState()
        observeAudioEnergy()
    }

    private fun observeWebSocket() {
        wsManager.events.onEach { update ->
            simulationJob?.cancel() // Real backend event received, cancel simulation fallback
            val currentLog = _uiState.value.transcriptLog.toMutableList()
            update.context.reason_codes.forEach { code ->
                if (code.startsWith("Speech: ") || code !in currentLog) {
                    if (code !in currentLog) {
                        currentLog.add(code)
                    }
                }
            }
            if (currentLog.size > 15) currentLog.removeAt(0)

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
                challengeQuestion = challenge
            )
        }.launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        wsManager.connectionState.onEach { state ->
            when (state) {
                is ConnectionState.Connected -> {
                    connectionTimeoutJob?.cancel()
                    _uiState.value = _uiState.value.copy(connectionStatus = "Active")
                }
                is ConnectionState.Connecting -> {
                    _uiState.value = _uiState.value.copy(connectionStatus = "Connecting...")
                    startConnectionTimeout()
                }
                is ConnectionState.Disconnected -> {
                    _uiState.value = _uiState.value.copy(connectionStatus = "Disconnected")
                }
                is ConnectionState.Error -> {
                    // Graceful fallback to simulation on port/connection error
                    _uiState.value = _uiState.value.copy(connectionStatus = "Active (Demo Mode)")
                    startLocalAiSimulation()
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun startConnectionTimeout() {
        connectionTimeoutJob?.cancel()
        connectionTimeoutJob = viewModelScope.launch {
            delay(4000) // If unable to connect within 4s, fallback to simulation mode smoothly
            if (_uiState.value.connectionStatus == "Connecting...") {
                _uiState.value = _uiState.value.copy(connectionStatus = "Active (Demo Mode)")
                startLocalAiSimulation()
            }
        }
    }

    private fun observeAudioEnergy() {
        webRTCManager.audioEnergyLevel.onEach { energy ->
            _uiState.value = _uiState.value.copy(audioEnergy = energy)
        }.launchIn(viewModelScope)
    }

    fun startMonitoring(isTestClone: Boolean = false) {
        val callId = if (isTestClone) "TEST-CLONE" else "LIVE-${System.currentTimeMillis() % 10000}"
        wsManager.connect("${Constants.WS_URL}/$callId")
        webRTCManager.startCall(isCaller = false, callId = callId) { }
        startConnectionTimeout()
    }

    private fun startLocalAiSimulation() {
        if (simulationJob?.isActive == true) return
        simulationJob = viewModelScope.launch {
            val steps = listOf(
                Triple(25, "LOW", listOf("Speech: \"Hello sir, good morning. Am I speaking with the account holder?\"")),
                Triple(55, "MEDIUM", listOf("Speech: \"We noticed unusual international login attempts on your SBI savings account.\"")),
                Triple(82, "HIGH", listOf("Speech: \"Please verify your Aadhaar number and OTP to cancel unauthorized transactions!\"")),
                Triple(96, "CRITICAL", listOf("Speech: \"URGENT: Transfer funds to safe reserve account immediately or face police action!\""))
            )

            for ((score, level, lines) in steps) {
                delay(3000)
                val currentLog = _uiState.value.transcriptLog.toMutableList()
                lines.forEach { line ->
                    if (line !in currentLog) currentLog.add(line)
                }
                if (currentLog.size > 15) currentLog.removeAt(0)

                val synth = if (score > 70) 0.88f else 0.15f
                val match = if (score > 70) 0.35f else 0.92f
                val challenge = if (score > 60) "⚠️ Challenge: Ask the caller about a shared family event or a genuine previous transaction." else null

                _uiState.value = _uiState.value.copy(
                    riskScore = score,
                    riskLevel = level,
                    syntheticVoice = synth,
                    speakerMatch = match,
                    replayProb = if (score > 70) 0.75f else 0.1f,
                    detectedIntent = if (score > 60) "Financial Fraud & Coercion" else "None",
                    transcriptLog = currentLog,
                    audioEnergy = 0.45f,
                    challengeQuestion = challenge
                )
            }
        }
    }

    fun simulateLiveSpeech() {
        val currentLog = _uiState.value.transcriptLog.toMutableList()
        val mockLines = listOf(
            "Speech: \"Hello sir, I am calling from SBI bank fraud department.\"",
            "Speech: \"Your account is linked to suspicious money laundering activity!\"",
            "Speech: \"Please transfer Rs 45,000 immediately to safe government account UPI: sbi.secure@paytm\"",
            "Speech: \"Share the 6-digit OTP sent to your registered mobile number right now!\""
        )
        mockLines.forEach { line ->
            if (line !in currentLog) currentLog.add(line)
        }
        _uiState.value = _uiState.value.copy(
            riskScore = 92,
            riskLevel = "CRITICAL",
            syntheticVoice = 0.94f,
            speakerMatch = 0.32f,
            replayProb = 0.85f,
            detectedIntent = "Financial Fraud & Coercion",
            transcriptLog = currentLog,
            audioEnergy = 0.65f,
            challengeQuestion = "⚠️ CRITICAL ALERT: The caller is demanding urgent bank transfer and OTP. Do NOT share any details."
        )
    }

    fun stopMonitoring() {
        simulationJob?.cancel()
        connectionTimeoutJob?.cancel()
        wsManager.disconnect()
        webRTCManager.endCall()
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
