package com.example.sih_2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sih_2026.domain.model.RiskAssessment
import com.example.sih_2026.domain.model.RiskLevel
import java.util.Locale

@Composable
fun CallScreen(
    callerName: String,
    riskAssessment: RiskAssessment?,
    latestTranscript: String,
    connectionQuality: String = "HD (WebRTC P2P)",
    callDurationSeconds: Int = 0,
    isVoiceActive: Boolean = false,
    onVerifyCaller: () -> Unit,
    onEndCall: () -> Unit,
) {
    var isMuted by remember { mutableStateOf(value = false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header: Caller info & WebRTC Advanced Telemetry
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))

                // Telemetry Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = "🟢 $connectionQuality",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = "⏱️ ${formatDuration(callDurationSeconds)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = callerName.take(1).uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = callerName, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                // VAD / Audio Activity status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceActive) Color.Green else Color.LightGray),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVoiceActive) "Voice Active (VAD)" else "Listening / Silent",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            }

            // Middle: Live Risk Banner & Transcript
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                riskAssessment?.let { assessment ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (assessment.riskLevel) {
                                RiskLevel.LOW -> Color(0xFFE8F5E9)
                                RiskLevel.SUSPICIOUS -> Color(0xFFFFF9C4)
                                RiskLevel.HIGH -> Color(0xFFFFE0B2)
                                RiskLevel.CRITICAL -> Color(0xFFFFCDD2)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = assessment.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = when (assessment.riskLevel) {
                                    RiskLevel.LOW -> Color(0xFF2E7D32)
                                RiskLevel.SUSPICIOUS -> Color(0xFFF57F17)
                                RiskLevel.HIGH -> Color(0xFFEF6C00)
                                RiskLevel.CRITICAL -> Color(0xFFC62828)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Risk Score: ${assessment.finalScore}/100", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            if (assessment.reasons.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                assessment.reasons.forEach { reason ->
                                    Text(text = "• $reason", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transcript Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Live Transcription & Telemetry:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = latestTranscript.ifEmpty { "Listening to conversation in real-time..." },
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // Bottom: Call Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Mute button
                Button(
                    onClick = { isMuted = !isMuted },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isMuted) Color.Gray else MaterialTheme.colorScheme.secondary),
                ) {
                    Text(if (isMuted) "Unmute" else "Mute")
                }

                // Verify Caller button (if high/critical risk)
                if ((riskAssessment != null) && (riskAssessment.riskLevel >= RiskLevel.HIGH)) {
                    Button(
                        onClick = onVerifyCaller,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("🚨 Verify Caller")
                    }
                }

                // End Call button
                Button(
                    onClick = onEndCall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                ) {
                    Text("End Call")
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
