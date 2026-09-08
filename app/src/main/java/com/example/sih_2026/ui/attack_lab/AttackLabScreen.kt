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
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SECURITY FORENSICS LAB", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text("Unified Forensic Analysis", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Analyze live voice or existing recordings for multiple fraud vectors simultaneously.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabActionButton(
                        "SCAN LIVE",
                        Icons.Default.Mic,
                        Color(0xFF818CF8),
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isRunning,
                        onClick = { viewModel.startLiveScan() }
                    )
                    LabActionButton(
                        "TEST FILE",
                        Icons.Default.AudioFile,
                        Color(0xFF34D399),
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isRunning,
                        onClick = { filePickerLauncher.launch("audio/*") }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (uiState.isRunning) {
                    Button(
                        onClick = { viewModel.stopAnalysis() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("STOP ANALYSIS")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (uiState.isRunning) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp).alpha(if (uiState.mode == "LIVE") alpha else 1f),
                            shape = CircleShape,
                            color = if (uiState.mode == "LIVE") Color.Red else Color.Green
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.mode == "LIVE") "LIVE STREAM ACTIVE" else "PROCESSING FILE",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    MetricRow("Synthetic Voice", 1f - uiState.syntheticProb, uiState.syntheticProb > 0.7f)
                    MetricRow("Speaker Match", uiState.speakerMatch, uiState.speakerMatch < 0.5f)
                    MetricRow("Replay Signature", 1f - uiState.replayProb, uiState.replayProb > 0.7f)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text("DYNAMIC LOG", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(uiState.transcriptLog) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))
                    ) {
                        Text(log, modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = Color.White)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (uiState.riskScore > 70) Color(0xFFF44336) else Color(0xFF4CAF50))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Aggregated Risk", color = Color.White.copy(alpha = 0.8f))
                            Text("${uiState.riskScore}/100", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(uiState.statusText, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LabActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
