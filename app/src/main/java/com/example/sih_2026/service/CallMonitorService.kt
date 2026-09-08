package com.example.sih_2026.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sih_2026.audio.AudioProcessor
import com.example.sih_2026.core.constants.Constants
import com.example.sih_2026.domain.model.RiskAssessment
import com.example.sih_2026.domain.repository.CallHistoryRepository
import com.example.sih_2026.ml.RiskEngine
import com.example.sih_2026.ml.ScamIntentDetector
import com.example.sih_2026.ml.VoiceSpoofDetector
import com.example.sih_2026.network.AudioChunk
import com.example.sih_2026.network.NetworkModule
import com.example.sih_2026.network.WebSocketManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicBoolean

class PredictionSmoother(private val windowSize: Int = 4) {
    private val queue = ArrayDeque<Float>()

    fun addAndSmooth(prediction: Float): Float {
        if (queue.size >= windowSize) {
            queue.removeFirst()
        }
        queue.addLast(prediction)
        return queue.average().toFloat()
    }
}

class CallMonitorService : Service() {

    companion object {
        private const val TAG = "CallMonitorService"
        private const val CHANNEL_ID = "VoxShieldCallChannel"
        private const val NOTIFICATION_ID = 1001

        private val _currentRisk = MutableStateFlow<RiskAssessment?>(null)
        val currentRisk: StateFlow<RiskAssessment?> = _currentRisk

        private val _latestTranscript = MutableStateFlow("")
        val latestTranscript: StateFlow<String> = _latestTranscript

        private val _isMonitoring = MutableStateFlow(false)
        val isMonitoring: StateFlow<Boolean> = _isMonitoring
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)

    private lateinit var audioProcessor: AudioProcessor
    private lateinit var voiceSpoofDetector: VoiceSpoofDetector
    private lateinit var scamIntentDetector: ScamIntentDetector
    private lateinit var riskEngine: RiskEngine
    private lateinit var audioManager: AudioManager
    private lateinit var wsManager: WebSocketManager
    private val predictionSmoother = PredictionSmoother(windowSize = 4)

    private var activeCallerNumber = "Unknown Number"
    private var lastRiskAssessment: RiskAssessment? = null
    private var chunkSequence = 0

    override fun onCreate() {
        super.onCreate()
        audioProcessor = AudioProcessor()
        voiceSpoofDetector = VoiceSpoofDetector(this)
        scamIntentDetector = ScamIntentDetector()
        riskEngine = RiskEngine()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        wsManager = NetworkModule.webSocketManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val stateStr = intent?.getStringExtra("phone_state")
        val incomingNumber = intent?.getStringExtra("incoming_number")
        if (!incomingNumber.isNullOrEmpty() && incomingNumber != "Unknown") {
            activeCallerNumber = incomingNumber
        }

        startForeground(NOTIFICATION_ID, createNotification("VoxShield Guard Active", "Monitoring call from $activeCallerNumber"))

        when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                Log.d(TAG, "Call ringing from: $activeCallerNumber")
                _isMonitoring.value = true
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d(TAG, "Call answered / offhook. Starting audio monitoring & speaker routing.")
                enableSpeakerphone()
                startAudioCapture()
                connectToBackend()
                _isMonitoring.value = true
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d(TAG, "Call ended. Recording session.")
                stopAudioCapture()
                disconnectFromBackend()

                val assessment = lastRiskAssessment
                val riskScore = assessment?.finalScore ?: 15
                val riskLevel = assessment?.riskLevel?.name ?: "LOW"
                val reasons = assessment?.reasons ?: listOf("Normal conversation flow")

                CallHistoryRepository.INSTANCE.recordCall(
                    callerName = "Caller ($activeCallerNumber)",
                    phoneNumber = activeCallerNumber,
                    riskScore = riskScore,
                    riskLevel = riskLevel,
                    reasons = reasons
                )

                _isMonitoring.value = false
                _currentRisk.value = null
                _latestTranscript.value = ""
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun enableSpeakerphone() {
        try {
            audioManager.isSpeakerphoneOn = true
            Log.d(TAG, "Speakerphone enabled for better audio capture.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable speakerphone", e)
        }
    }

    private fun connectToBackend() {
        val callId = "CALL-${System.currentTimeMillis() % 10000}"
        wsManager.connect("${Constants.WS_URL}/$callId")
    }

    private fun disconnectFromBackend() {
        wsManager.disconnect()
    }

    private fun startAudioCapture() {
        if (isRecording.get()) return

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
                isRecording.set(true)
                Log.d(TAG, "AudioRecord started successfully.")

                serviceScope.launch {
                    runAudioProcessingLoop(bufferSize)
                }
                
                // Observe risk updates from backend
                serviceScope.launch {
                    wsManager.events.collect { update ->
                        _latestTranscript.value = update.context.reason_codes.firstOrNull() ?: "Call in progress"
                        // Map update to RiskAssessment for UI
                        // ... existing logic to update _currentRisk
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioRecord: ${e.message}")
        }
    }

    private suspend fun runAudioProcessingLoop(bufferSize: Int) {
        val buffer = ByteArray(bufferSize)

        while (isRecording.get()) {
            val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (readBytes > 0) {
                val pcmChunk = buffer.copyOf(readBytes)
                
                // 1. Stream to backend
                val b64Audio = Base64.encodeToString(pcmChunk, Base64.NO_WRAP)
                wsManager.sendAudio(AudioChunk(
                    call_id = "CURRENT", // Placeholder, server uses websocket path call_id
                    sequence = chunkSequence++,
                    timestamp = System.currentTimeMillis(),
                    audio = b64Audio
                ))

                // 2. Local fallback analysis (optional, keeps UI responsive)
                processAudioLocally(pcmChunk)
            }
            delay(200) // Fast streaming
        }
    }

    private fun processAudioLocally(pcmChunk: ByteArray) {
        // ... implementation of local analysis as fallback
    }

    private fun stopAudioCapture() {
        isRecording.set(false)
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "AudioRecord stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopAudioCapture()
        disconnectFromBackend()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VoxShield Call Protection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active protection monitoring calls for AI voice scams"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
