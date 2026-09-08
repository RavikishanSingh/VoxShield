package com.example.sih_2026.ui.attack_lab

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih_2026.core.constants.Constants
import com.example.sih_2026.network.AudioChunk
import com.example.sih_2026.network.NetworkModule
import com.example.sih_2026.webrtc.WebRTCManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.InputStream

data class AttackLabUiState(
    val isRunning: Boolean = false,
    val isSimulating: Boolean = false,
    val mode: String = "IDLE", // "LIVE" or "FILE"
    val syntheticProb: Float = 0f,
    val speakerMatch: Float = 0f,
    val replayProb: Float = 0f,
    val riskScore: Int = 0,
    val statusText: String = "READY",
    val transcriptLog: List<String> = emptyList(),
    val errorMessage: String? = null,
    val streamingProgress: Float = 0f,
    val audioEnergy: Float = 0.0f
)

class AttackLabViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AttackLabUiState())
    val uiState: StateFlow<AttackLabUiState> = _uiState.asStateFlow()
    
    private val wsManager = NetworkModule.webSocketManager
    private val webRTCManager = WebRTCManager(application)
    private var chunkSequence = 0

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
            if (currentLog.size > 5) currentLog.removeAt(0)

            _uiState.value = _uiState.value.copy(
                syntheticProb = update.voice.synthetic,
                speakerMatch = update.voice.speaker_similarity,
                replayProb = update.voice.replay,
                riskScore = update.risk.score,
                statusText = if (update.risk.score > 70) "🔴 THREAT DETECTED" else "🟢 MONITORING...",
                transcriptLog = currentLog,
                isRunning = true
            )
        }.launchIn(viewModelScope)
    }

    private fun observeAudioEnergy() {
        webRTCManager.audioEnergyLevel.onEach { energy ->
            if (_uiState.value.mode == "LIVE") {
                _uiState.value = _uiState.value.copy(audioEnergy = energy)
            }
        }.launchIn(viewModelScope)
    }

    fun startLiveScan() {
        resetState()
        val callId = "LAB-LIVE-${System.currentTimeMillis() % 10000}"
        _uiState.value = _uiState.value.copy(isRunning = true, mode = "LIVE", statusText = "INITIALIZING MIC...")
        
        wsManager.connect("${Constants.WS_URL}/$callId")
        webRTCManager.startCall(isCaller = false) { }
    }

    fun analyzeLocalFile(uri: Uri) {
        resetState()
        val callId = "LAB-FILE-${System.currentTimeMillis() % 10000}"
        _uiState.value = _uiState.value.copy(isRunning = true, mode = "FILE", statusText = "UPLOADING FILE...")
        
        wsManager.connect("${Constants.WS_URL}/$callId")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                
                // Stream file in chunks to simulate real-time
                val chunkSize = 3200 // ~100ms of 16kHz audio
                val totalChunks = (bytes.size / chunkSize) + 1
                
                for (i in 0 until totalChunks) {
                    if (!_uiState.value.isRunning) break
                    
                    val start = i * chunkSize
                    val end = minOf(start + chunkSize, bytes.size)
                    if (start >= bytes.size) break
                    
                    val chunk = bytes.copyOfRange(start, end)
                    val b64Audio = Base64.encodeToString(chunk, Base64.NO_WRAP)
                    
                    wsManager.sendAudio(AudioChunk(
                        call_id = callId,
                        sequence = chunkSequence++,
                        timestamp = System.currentTimeMillis(),
                        audio = b64Audio
                    ))
                    
                    _uiState.value = _uiState.value.copy(
                        streamingProgress = (i.toFloat() / totalChunks),
                        statusText = "STREAMING FILE: ${(i.toFloat() / totalChunks * 100).toInt()}%",
                        audioEnergy = 0.5f // Simulate constant energy for file
                    )
                    
                    delay(100) 
                }
                _uiState.value = _uiState.value.copy(statusText = "ANALYSIS COMPLETE", audioEnergy = 0f)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "File error: ${e.message}")
            }
        }
    }

    fun stopAnalysis() {
        wsManager.disconnect()
        webRTCManager.endCall()
        _uiState.value = _uiState.value.copy(isRunning = false, mode = "IDLE", statusText = "STOPPED", audioEnergy = 0f)
    }

    private fun resetState() {
        chunkSequence = 0
        _uiState.value = AttackLabUiState()
    }

    override fun onCleared() {
        super.onCleared()
        stopAnalysis()
    }
}
