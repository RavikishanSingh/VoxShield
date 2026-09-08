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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCallScreen(viewModel: LiveCallViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startMonitoring()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LIVE VOICE GUARD", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                actions = {
                    Text(uiState.connectionStatus, color = if (uiState.connectionStatus == "Active") Color(0xFF4CAF50) else Color.Gray, modifier = Modifier.padding(end = 16.dp))
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
                
                MetricRow("Voice Authenticity", 1f - uiState.syntheticVoice, uiState.syntheticVoice > 0.7f)
                MetricRow("Speaker Identity", uiState.speakerMatch, uiState.speakerMatch < 0.5f)
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
