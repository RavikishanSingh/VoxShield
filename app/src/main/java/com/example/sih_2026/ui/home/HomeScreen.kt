package com.example.sih_2026.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
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
import com.example.sih_2026.ui.navigation.VoxShieldDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigate: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VoxShield", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SecurityHealthCard(uiState.securityHealthScore)
                Spacer(modifier = Modifier.height(24.dp))
                ProtectionStatusCard(uiState.isProtectionActive)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Button(
                    onClick = { onNavigate(VoxShieldDestinations.LIVE_CALL) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🚀 START REAL-TIME GUARD", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                QuickActionsSection(onNavigate)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                StatsSection(uiState)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                ProtectionModulesSection()
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                RecentActivitySection(onNavigate)
            }
        }
    }
}

@Composable
fun SecurityHealthCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Security Health Index", fontSize = 12.sp, color = Color.Gray)
                Text("System Resilience", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (score > 75) Color(0xFF4CAF50) else Color(0xFFFACC15)),
                contentAlignment = Alignment.Center
            ) {
                Text("$score", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun QuickActionsSection(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { onNavigate(VoxShieldDestinations.ATTACK_LAB) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Labs")
        }
        OutlinedButton(
            onClick = { onNavigate(VoxShieldDestinations.ANALYTICS) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Analytics")
        }
    }
}

@Composable
fun ProtectionStatusCard(isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isActive) "PROTECTION ACTIVE" else "PROTECTION DISABLED",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
fun StatsSection(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's Protection", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Calls Analyzed", uiState.callsAnalyzed.toString())
                StatItem("Suspicious", uiState.suspiciousCalls.toString())
                StatItem("Attacks Blocked", uiState.attacksBlocked.toString())
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProtectionModulesSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Active Modules", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModuleChip("Voice")
            ModuleChip("Speaker")
            ModuleChip("Intent")
            ModuleChip("Risk")
        }
    }
}

@Composable
fun ModuleChip(label: String) {
    Surface(
        color = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "🟢 $label",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1976D2)
        )
    }
}

@Composable
fun RecentActivitySection(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.clickable { onNavigate(VoxShieldDestinations.INCIDENTS) }) {
            ActivityItem("Unknown Caller", "Risk: 78%", Color(0xFFF44336))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Box(modifier = Modifier.clickable { onNavigate(VoxShieldDestinations.INCIDENTS) }) {
            ActivityItem("Verified Contact", "Risk: 12%", Color(0xFF4CAF50))
        }
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, riskColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text("Today, 2:45 PM", fontSize = 12.sp, color = Color.Gray)
        }
        Text(subtitle, color = riskColor, fontWeight = FontWeight.Bold)
    }
}
