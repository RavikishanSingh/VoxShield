package com.example.sih_2026.ui.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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

@Composable
fun IncomingCallScreen(viewModel: IncomingCallViewModel, onAnswer: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("INCOMING CALL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF334155)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(uiState.callerNumber, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (uiState.isAnalyzing) {
            Text("⚠️ ANALYZING VOICE", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        MetricRow("Voice Authenticity", uiState.syntheticVoiceProb, isSuspicious = uiState.syntheticVoiceProb > 0.7f)
        MetricRow("Speaker Match", uiState.speakerMatchProb, isSuspicious = false)
        MetricRow("Replay Probability", uiState.replayProb, isSuspicious = false)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        RiskCard(uiState.riskScore, uiState.riskLevel, uiState.threatWarning)
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(modifier = Modifier.clickable { onAnswer() }) {
                CallActionButton(Icons.Default.Call, "ANSWER", Color(0xFF4CAF50))
            }
            CallActionButton(Icons.Default.Clear, "BLOCK", Color(0xFFF44336))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricRow(label: String, value: Float, isSuspicious: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.LightGray, fontSize = 14.sp)
            Text("${(value * 100).toInt()}% ${if (isSuspicious) "suspicious" else ""}", 
                 color = if (isSuspicious) Color(0xFFF44336) else Color.White, 
                 fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (isSuspicious) Color(0xFFF44336) else Color(0xFF818CF8),
            trackColor = Color(0xFF334155)
        )
    }
}

@Composable
fun RiskCard(score: Int, level: String, warning: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Risk Score", color = Color.LightGray, fontSize = 14.sp)
            Text("$score / 100", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(level, color = Color(0xFFF44336), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(warning, color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CallActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
