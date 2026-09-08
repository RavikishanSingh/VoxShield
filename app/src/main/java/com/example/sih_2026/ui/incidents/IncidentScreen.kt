package com.example.sih_2026.ui.incidents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentScreen(viewModel: IncidentViewModel) {
    val incidents by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("INCIDENT CENTER", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(incidents) { incident ->
                IncidentCard(incident)
            }
        }
    }
}

@Composable
fun IncidentCard(incident: IncidentDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SECURITY INCIDENT", fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                Text(incident.severity, fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Caller: ${incident.caller}", fontWeight = FontWeight.Medium)
            Text("Risk: ${incident.riskScore}/100", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Threat: ${incident.threatType}", fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Evidence:", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.Gray)
            incident.evidence.forEach { item ->
                Text("✓ $item", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 2.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Action: ${incident.action}", color = Color(0xFFF44336), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("VIEW DETAILS")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}
