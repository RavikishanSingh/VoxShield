package com.example.sih_2026.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.sih_2026.MainActivity
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
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    private val trustedNumbers = setOf("+919876543210", "+919123456789", "+919988776655", "9876543210", "9123456789", "9988776655", "+91 98765 43210")

    private fun isTrustedNumber(number: String): Boolean {
        val cleaned = number.replace(Regex("[^0-9+]"), "")
        return trustedNumbers.any { cleaned.contains(it.replace(Regex("[^0-9+]"), "")) }
    }

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

        val isTrusted = isTrustedNumber(activeCallerNumber)
        val notifTitle = if (isTrusted) "VoxShield: Trusted Caller" else "VoxShield Guard Active"
        startForeground(NOTIFICATION_ID, createNotification(notifTitle, "Monitoring call from $activeCallerNumber"))

        when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                Log.d(TAG, "Call ringing from: $activeCallerNumber (Trusted: $isTrusted)")
                showOverlay(activeCallerNumber)
                _isMonitoring.value = true
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d(TAG, "Call answered / offhook. Starting audio monitoring & speaker routing.")
                removeOverlay()
                if (!isTrusted) {
                    enableSpeakerphone()
                    startAudioCapture()
                    connectToBackend()
                }
                _isMonitoring.value = true
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d(TAG, "Call ended. Recording session.")
                removeOverlay()
                stopAudioCapture()
                disconnectFromBackend()

                val assessment = lastRiskAssessment
                val riskScore = if (isTrusted) 0 else (assessment?.finalScore ?: 15)
                val riskLevel = if (isTrusted) "SAFE" else (assessment?.riskLevel?.name ?: "LOW")
                val reasons = if (isTrusted) listOf("Verified Trusted Circle Contact") else (assessment?.reasons ?: listOf("Normal conversation flow"))

                CallHistoryRepository.INSTANCE.recordCall(
                    callerName = if (isTrusted) "Trusted Contact ($activeCallerNumber)" else "Caller ($activeCallerNumber)",
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

    private fun showOverlay(callerNumber: String) {
        if (overlayView != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW permission not granted. Cannot draw overlay.")
            return
        }

        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val contextWrapper = ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault)
            val isTrusted = isTrustedNumber(callerNumber)

            val layout = LinearLayout(contextWrapper).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 48, 48, 48)
                setBackgroundColor(Color.parseColor("#E61E293B"))
            }

            val titleTv = TextView(contextWrapper).apply {
                text = if (isTrusted) "🛡️ VOXSHIELD: TRUSTED CONTACT" else "⚠️ VOXSHIELD AI CALL GUARD"
                setTextColor(if (isTrusted) Color.parseColor("#4CAF50") else Color.parseColor("#FACC15"))
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
            }
            layout.addView(titleTv)

            val numberTv = TextView(contextWrapper).apply {
                text = "Incoming: $callerNumber"
                setTextColor(Color.WHITE)
                textSize = 22f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 12, 0, 8)
            }
            layout.addView(numberTv)

            val statusTv = TextView(contextWrapper).apply {
                text = if (isTrusted) "Verified in your Trusted Circle. Safe from deepfakes." else "Analyzing voice clone & scam risk..."
                setTextColor(if (isTrusted) Color.parseColor("#86EFAC") else Color.parseColor("#94A3B8"))
                textSize = 13f
                setPadding(0, 0, 0, 20)
            }
            layout.addView(statusTv)

            val btnLayout = LinearLayout(contextWrapper).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            val openBtn = Button(contextWrapper).apply {
                text = "OPEN APP"
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(intent)
                    removeOverlay()
                }
            }
            btnLayout.addView(openBtn)

            val dismissBtn = Button(contextWrapper).apply {
                text = "DISMISS"
                setBackgroundColor(Color.parseColor("#EF4444"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    removeOverlay()
                }
                setPadding(24, 0, 0, 0)
            }
            btnLayout.addView(dismissBtn)

            layout.addView(btnLayout)
            overlayView = layout

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                x = 0
                y = 80
            }

            windowManager?.addView(overlayView, params)
            Log.d(TAG, "Incoming call overlay drawn successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to draw overlay: ${e.message}", e)
        }
    }

    private fun removeOverlay() {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
                overlayView = null
                Log.d(TAG, "Incoming call overlay removed.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay: ${e.message}", e)
        }
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
                    call_id = "CURRENT",
                    sequence = chunkSequence++,
                    timestamp = System.currentTimeMillis(),
                    audio = b64Audio
                ))

                processAudioLocally(pcmChunk)
            }
            delay(200)
        }
    }

    private fun processAudioLocally(pcmChunk: ByteArray) {
        // Local analysis fallback
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
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
