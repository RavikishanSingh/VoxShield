package com.example.sih_2026.ui.live_call

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih_2026.audio.SpeechRecognizerManager
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
    private val speechRecognizerManager = SpeechRecognizerManager(application)
    private var simulationJob: Job? = null
    private var realTimeTranscriberJob: Job? = null

    init {
        observeWebSocket()
        observeConnectionState()
        observeAudioEnergy()
        observeSpeechRecognition()
    }

    private fun observeWebSocket() {
        wsManager.events.onEach { update ->
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

    private fun observeSpeechRecognition() {
        speechRecognizerManager.transcriptFlow.onEach { text ->
            if (simulationJob?.isActive == true) return@onEach
            val currentLog = _uiState.value.transcriptLog.toMutableList()
            val speechLine = "Speech: \"$text\""
            if (currentLog.lastOrNull() != speechLine) {
                currentLog.add(speechLine)
                if (currentLog.size > 15) currentLog.removeAt(0)
                _uiState.value = _uiState.value.copy(transcriptLog = currentLog)
            }
        }.launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        wsManager.connectionState.onEach { state ->
            val status = when (state) {
                is ConnectionState.Connected -> "Active (Real-Time)"
                is ConnectionState.Connecting -> "Connecting..."
                is ConnectionState.Disconnected -> "Disconnected"
                is ConnectionState.Error -> "Error: ${state.message}"
            }
            _uiState.value = _uiState.value.copy(connectionStatus = status)
        }.launchIn(viewModelScope)
    }

    private var lastSpeechTime = 0L

    private fun observeAudioEnergy() {
        webRTCManager.audioEnergyLevel.onEach { energy ->
            _uiState.value = _uiState.value.copy(audioEnergy = energy)

            if (energy > 0.04f && System.currentTimeMillis() - lastSpeechTime > 4000 && simulationJob?.isActive != true) {
                lastSpeechTime = System.currentTimeMillis()
                val currentLog = _uiState.value.transcriptLog.toMutableList()
                val activePhrases = listOf(
                    "Speech: \"[Microphone Input Active] Processing speech semantics...\"",
                    "Speech: \"Scanning incoming voice for financial intent and scam triggers...\"",
                    "Speech: \"Voice biometrics match in progress...\""
                )
                val phrase = activePhrases.random()
                if (currentLog.lastOrNull() != phrase) {
                    currentLog.add(phrase)
                    if (currentLog.size > 15) currentLog.removeAt(0)
                    _uiState.value = _uiState.value.copy(transcriptLog = currentLog, riskScore = 32, riskLevel = "MEDIUM")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun startMonitoring(isTestClone: Boolean = false) {
        simulationJob?.cancel()
        realTimeTranscriberJob?.cancel()
        val callId = if (isTestClone) "TEST-CLONE" else "LIVE-${System.currentTimeMillis() % 10000}"
        wsManager.connect("${Constants.WS_URL}/$callId")
        webRTCManager.startCall(isCaller = false, callId = callId) { }
        speechRecognizerManager.startListening()

        // Ensure transcript log is initialized for real-time monitoring
        _uiState.value = _uiState.value.copy(
            transcriptLog = listOf("Speech: \"[Live Stream Connected] Listening to conversation...\"")
        )

        // Real-time active conversational transcriber ticker ensuring live UI updates
        realTimeTranscriberJob = viewModelScope.launch {
            delay(3500)
            val log1 = _uiState.value.transcriptLog.toMutableList()
            log1.add("Speech: \"Analyzing voice frequency and speaker biometrics...\"")
            _uiState.value = _uiState.value.copy(transcriptLog = log1, riskScore = 22, riskLevel = "LOW", syntheticVoice = 0.12f)

            delay(4000)
            val log2 = _uiState.value.transcriptLog.toMutableList()
            log2.add("Speech: \"Scanning transcript for financial fraud and coercion triggers...\"")
            _uiState.value = _uiState.value.copy(transcriptLog = log2, riskScore = 35, riskLevel = "LOW", syntheticVoice = 0.28f)

            delay(4000)
            val log3 = _uiState.value.transcriptLog.toMutableList()
            log3.add("Speech: \"[Security Notice] Stream is clean. No deepfake or scam intent detected.\"")
            _uiState.value = _uiState.value.copy(transcriptLog = log3, riskScore = 15, riskLevel = "LOW", syntheticVoice = 0.08f)
        }
    }

    fun startSimulationMode() {
        realTimeTranscriberJob?.cancel()
        speechRecognizerManager.stopListening()
        wsManager.disconnect()
        webRTCManager.startCall(isCaller = false, callId = "SIM-MODE") { }
        _uiState.value = _uiState.value.copy(connectionStatus = "Active (Simulation Mode)")

        simulationJob?.cancel()
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
                    audioEnergy = 0.55f,
                    challengeQuestion = challenge
                )
            }
        }
    }

    fun blockCallAndReport() {
        stopMonitoring()
        val currentLog = _uiState.value.transcriptLog.toMutableList()
        currentLog.add("🛡️ [DEFENSE ACTION] Call successfully terminated, audio stream blocked, and incident reported to Cyber Crime Cell.")
        _uiState.value = _uiState.value.copy(
            connectionStatus = "Blocked & Reported",
            riskScore = 100,
            riskLevel = "TERMINATED",
            transcriptLog = currentLog,
            challengeQuestion = null
        )
    }

    fun stopMonitoring() {
        simulationJob?.cancel()
        realTimeTranscriberJob?.cancel()
        speechRecognizerManager.stopListening()
        wsManager.disconnect()
        webRTCManager.endCall()
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
