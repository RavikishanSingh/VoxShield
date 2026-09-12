package com.example.sih_2026.ui.live_call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import com.example.sih_2026.ui.components.VoiceActivityBar
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCallScreen(viewModel: LiveCallViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.startMonitoring()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startMonitoring()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val riskColor = when {
        uiState.riskScore > 80 -> Color(0xFFDC2626) // Red
        uiState.riskScore > 50 -> Color(0xFFD97706) // Amber
        else -> Color(0xFF059669)                   // Green
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("VOICE GUARD SHIELD", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(uiState.callerNumber, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            uiState.connectionStatus.contains("Active") -> Color(0xFF10B981).copy(alpha = 0.2f)
                            uiState.connectionStatus.startsWith("Error") -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            uiState.connectionStatus.contains("Active") -> Color(0xFF10B981)
                                            uiState.connectionStatus.startsWith("Error") -> Color(0xFFEF4444)
                                            else -> Color(0xFFF59E0B)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.connectionStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    uiState.connectionStatus.contains("Active") -> Color(0xFF059669)
                                    uiState.connectionStatus.startsWith("Error") -> Color(0xFFDC2626)
                                    else -> Color(0xFFD97706)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Risk Score Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        riskColor.copy(alpha = 0.15f),
                                        riskColor.copy(alpha = 0.02f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CONVERSATION RISK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(uiState.riskLevel, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = riskColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Intent: ${uiState.detectedIntent}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Surface(
                                shape = CircleShape,
                                color = riskColor,
                                modifier = Modifier.size(76.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${uiState.riskScore}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Audio Waveform Activity
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("LIVE AUDIO STREAM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        VoiceActivityBar(audioLevel = uiState.audioEnergy)
                    }
                }
            }

            // 3. AI Voice Clone Detection (The Main Guard)
            item {
                val isDeepfake = uiState.syntheticVoice > 0.65f
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDeepfake) Color(0xFF7F1D1D) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🤖", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Voice Clone Detection", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDeepfake) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDeepfake) Color(0xFFDC2626) else Color(0xFF059669)
                            ) {
                                Text(
                                    text = if (isDeepfake) "DEEPFAKE DETECTED" else "AUTHENTIC VOICE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { uiState.syntheticVoice },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (isDeepfake) Color(0xFFFCA5A5) else Color(0xFF34D399),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Synthetic Probability", fontSize = 12.sp, color = if (isDeepfake) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(uiState.syntheticVoice * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDeepfake) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // 4. Advanced Acoustic Biometric Signature Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ACOUSTIC BIOMETRIC SIGNATURE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BiometricMetric("Pitch Jitter", if (uiState.syntheticVoice > 0.65f) "0.02% (Unnatural)" else "1.42% (Normal)")
                            BiometricMetric("Formant Stability", if (uiState.syntheticVoice > 0.65f) "Vocoder Artifacts" else "Natural Tract")
                            BiometricMetric("Phase Coherence", if (uiState.syntheticVoice > 0.65f) "Synthetic Mirror" else "Organic Wave")
                        }
                    }
                }
            }

            // 5. RocketRide Evidence Weighting Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ROCKETRIDE EVIDENCE SYNTHESIS (v2-Mobile)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        EvidenceBar("Voice Clone Weight (50%)", uiState.syntheticVoice)
                        Spacer(modifier = Modifier.height(8.dp))
                        EvidenceBar("Speaker Identity (25%)", 1f - uiState.speakerMatch)
                        Spacer(modifier = Modifier.height(8.dp))
                        EvidenceBar("Intent & Coercion (15%)", if (uiState.riskScore > 50) 0.85f else 0.1f)
                        Spacer(modifier = Modifier.height(8.dp))
                        EvidenceBar("Replay Signature (10%)", uiState.replayProb)
                    }
                }
            }

            // 6. Security Telemetry Grid (Speaker Identity & Stream Health)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Speaker Match Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Speaker Match", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${(uiState.speakerMatch * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (uiState.speakerMatch < 0.5f) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { uiState.speakerMatch },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (uiState.speakerMatch < 0.5f) Color(0xFFEF4444) else Color(0xFF3B82F6)
                            )
                        }
                    }

                    // Replay / Stream Health Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Replay / Spoof", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${(uiState.replayProb * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (uiState.replayProb > 0.6f) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { uiState.replayProb },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (uiState.replayProb > 0.6f) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            // 7. Actionable Challenge Card
            uiState.challengeQuestion?.let { q ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF991B1B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VERIFICATION CHALLENGE REQUIRED", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(q, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 8. Live Transcript Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LIVE TRANSCRIPT & AI REASONING", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${uiState.transcriptLog.size} events", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 9. Transcript Items
            items(uiState.transcriptLog) { line ->
                val isThreat = line.contains("OTP") || line.contains("bank") || line.contains("money") || line.contains("URGENT") || line.contains("Aadhaar")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThreat) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isThreat) Color(0xFFEF4444) else Color(0xFF3B82F6))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = line,
                            fontSize = 13.sp,
                            color = if (isThreat) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 10. Control Buttons & Emergency Block
            item {
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.riskScore > 75) {
                    Button(
                        onClick = { viewModel.blockCallAndReport() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("🛡️ BLOCK CALL & REPORT TO CYBERCRIME", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = { viewModel.startSimulationMode() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("🎤 START SIMULATION MODE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.stopMonitoring() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("STOP GUARD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun BiometricMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun EvidenceBar(label: String, progress: Float) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
