package com.example.sih_2026.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SECURITY ANALYTICS", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatCard("Calls Analyzed", uiState.totalCallsAnalyzed.toString(), Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard("Threats Detected", uiState.totalThreats.toString(), Modifier.weight(1f), Color(0xFFF44336))
                }
            }

            item {
                ThreatDistributionSection(uiState)
            }

            item {
                Text("Detection Rate: ${uiState.detectionRate}%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                RiskTrendChart()
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.Unspecified) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun ThreatDistributionSection(uiState: AnalyticsUiState) {
    Column {
        Text("Threat Distribution", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ThreatBar("Deepfake", uiState.deepfakeThreats, uiState.totalThreats, Color(0xFF818CF8))
        ThreatBar("Replay", uiState.replayThreats, uiState.totalThreats, Color(0xFFFACC15))
        ThreatBar("Impersonation", uiState.impersonationThreats, uiState.totalThreats, Color(0xFFF44336))
        ThreatBar("Other", uiState.otherThreats, uiState.totalThreats, Color.Gray)
    }
}

@Composable
fun ThreatBar(label: String, count: Int, total: Int, color: Color) {
    val progress = count.toFloat() / total.toFloat()
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp)
            Text(count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun RiskTrendChart() {
    Column {
        Text("Risk Trend", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.8f)
                quadraticBezierTo(size.width * 0.2f, size.height * 0.4f, size.width * 0.4f, size.height * 0.7f)
                quadraticBezierTo(size.width * 0.6f, size.height * 0.9f, size.width * 0.8f, size.height * 0.2f)
                lineTo(size.width, size.height * 0.5f)
            }
            drawPath(path, color = Color(0xFF818CF8), style = Stroke(width = 4.dp.toPx()))
        }
    }
}
