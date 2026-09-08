package com.example.sih_2026.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sih_2026.domain.model.CallLogEntry
import com.example.sih_2026.domain.repository.CallHistoryRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBackClick: () -> Unit,
    onTrustedContactsClick: () -> Unit,
) {
    val callLogs = CallHistoryRepository.INSTANCE.callLogs.collectAsStateWithLifecycle().value
    val totalCalls = CallHistoryRepository.INSTANCE.totalCallsMonitored.collectAsStateWithLifecycle().value
    val deepfakesBlocked = CallHistoryRepository.INSTANCE.deepfakesBlocked.collectAsStateWithLifecycle().value
    val scamIntents = CallHistoryRepository.INSTANCE.scamIntentsIntercepted.collectAsStateWithLifecycle().value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🛡️ VoxShield Security Dashboard", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        TextButton(onClick = onBackClick) {
                            Text("← Back", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Security Status Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🟢", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("On-Device Protection Active", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFA7F3D0))
                                    Text("AASIST-L & DistilBERT guards running locally", fontSize = 12.sp, color = Color(0xFFD1FAE5))
                                }
                            }
                        }
                    }
                }

                // Telemetry Stats Grid (Real-Time Data)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Calls Monitored",
                            value = totalCalls.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Deepfakes Blocked",
                            value = deepfakesBlocked.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Scam Intents",
                            value = scamIntents.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recent Intercepted Calls Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Call Security Logs", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        TextButton(onClick = onTrustedContactsClick) {
                            Text("Trusted Circle →")
                        }
                    }
                }

                // Call Logs List (Real-Time Data) or Empty State
                if (callLogs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛡️", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Suspicious Calls Intercepted", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("All monitored calls have been secure.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(callLogs) { log ->
                        CallLogCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CallLogCard(log: CallLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(log.callerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(log.phoneNumber, fontSize = 12.sp, color = Color.Gray)
                }
                Surface(
                    color = when (log.riskLevel) {
                        "CRITICAL" -> Color(0xFF7F1D1D)
                        "HIGH" -> Color(0xFF78350F)
                        else -> Color(0xFF065F46)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${log.riskLevel} (${log.riskScore})",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (log.riskLevel) {
                            "CRITICAL" -> Color(0xFFFCA5A5)
                            "HIGH" -> Color(0xFFFCD34D)
                            else -> Color(0xFF6EE7B7)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(log.timestamp, fontSize = 11.sp, color = Color.Gray)

            if (log.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                log.reasons.forEach { reason ->
                    Text(text = "• $reason", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
