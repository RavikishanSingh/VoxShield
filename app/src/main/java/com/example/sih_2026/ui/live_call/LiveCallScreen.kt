package com.example.sih_2026.ui.live_call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sih_2026.ui.calls.MetricRow
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LIVE VOICE GUARD", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                actions = {
                    val statusColor = when {
                        uiState.connectionStatus == "Active" -> Color(0xFF4CAF50)
                        uiState.connectionStatus.startsWith("Error") -> Color(0xFFF44336)
                        else -> Color(0xFFFACC15)
                    }
                    Text(uiState.connectionStatus, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text("Mode: Real-time Analysis", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                VoiceActivityBar(audioLevel = uiState.audioEnergy)
                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.syntheticVoice > 0.65f) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🚨 AI VOICE CLONE DETECTED", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Synthetic deepfake voice signature identified! Probability: ${(uiState.syntheticVoice * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                MetricRow("🤖 AI Voice Clone Risk", uiState.syntheticVoice, uiState.syntheticVoice > 0.65f)
                MetricRow("Speaker Identity Match", uiState.speakerMatch, uiState.speakerMatch < 0.5f)
                MetricRow("Live Stream Health", 0.95f, false)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                uiState.challengeQuestion?.let { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ACTIONABLE VERIFICATION REQUIRED", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(q, color = Color.White, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                Text("LIVE TRANSCRIPT", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(uiState.transcriptLog) { line ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = line,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = if (line.contains("OTP") || line.contains("bank") || line.contains("money")) Color(0xFFF44336) else Color.White
                    )
                }
            }

            if (uiState.transcriptLog.isEmpty()) {
                item {
                    Text("Listening for speech...", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (uiState.riskScore > 80) Color(0xFFF44336) else if (uiState.riskScore > 50) Color(0xFFFACC15) else Color(0xFF4CAF50))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Risk Score", color = Color.White.copy(alpha = 0.8f))
                        Text("${uiState.riskScore}", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(uiState.riskLevel, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.simulateLiveSpeech() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("🎤 SIMULATE LIVE SPEECH & TRANSCRIPT")
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.stopMonitoring() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("STOP GUARD")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
