package com.example.sih_2026.ui.attack_lab

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sih_2026.ui.calls.MetricRow
import com.example.sih_2026.ui.components.VoiceActivityBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackLabScreen(viewModel: AttackLabViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.analyzeLocalFile(it) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SECURITY FORENSICS LAB", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Deepfake & Spoof Forensics", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Test live microphone streams or upload audio recordings for multi-vector threat analysis.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LabActionButton(
                                "SCAN LIVE",
                                Icons.Default.Mic,
                                MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isRunning,
                                onClick = { viewModel.startLiveScan() }
                            )
                            LabActionButton(
                                "TEST FILE",
                                Icons.Default.AudioFile,
                                Color(0xFF059669),
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isRunning,
                                onClick = { filePickerLauncher.launch("audio/*") }
                            )
                        }
                        
                        if (uiState.isRunning) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.stopAnalysis() },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                            ) {
                                Text("STOP FORENSIC ANALYSIS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (uiState.isRunning) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(10.dp).alpha(if (uiState.mode == "LIVE") alpha else 1f),
                                    shape = CircleShape,
                                    color = if (uiState.mode == "LIVE") Color(0xFFDC2626) else Color(0xFF059669)
                                ) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.mode == "LIVE") "LIVE STREAM FORENSICS ACTIVE" else "UPLOADING & ANALYZING FILE",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            VoiceActivityBar(audioLevel = uiState.audioEnergy)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (uiState.mode == "FILE") {
                                LinearProgressIndicator(
                                    progress = { uiState.streamingProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            MetricRow("AI Voice Clone (Deepfake)", 1f - uiState.syntheticProb, uiState.syntheticProb > 0.7f)
                            MetricRow("Speaker Identity Match", uiState.speakerMatch, uiState.speakerMatch < 0.5f)
                            MetricRow("Replay Signature", 1f - uiState.replayProb, uiState.replayProb > 0.7f)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.riskScore > 70) Color(0xFF7F1D1D) else Color(0xFF065F46)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Aggregated Threat Score", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${uiState.riskScore}/100", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(uiState.statusText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }

                item {
                    Text("FORENSIC LOG STREAM", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(uiState.transcriptLog) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(log, modifier = Modifier.padding(14.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun LabActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
        }
    }
}
