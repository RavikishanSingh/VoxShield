package com.example.sih_2026.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sih_2026.service.CallMonitorService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateToProfiles: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PROTECTION SETTINGS", fontWeight = FontWeight.Bold) })
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
                Text("Incoming Call Feature Testing", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val context = LocalContext.current
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                            Toast.makeText(context, "Please grant 'Display over other apps' permission", Toast.LENGTH_LONG).show()
                        } else {
                            val intent = Intent(context, CallMonitorService::class.java).apply {
                                putExtra("phone_state", TelephonyManager.EXTRA_STATE_RINGING)
                                putExtra("incoming_number", "+91 98765 43210 (TEST)")
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                            Toast.makeText(context, "Simulating incoming call overlay & notification...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Incoming Call Overlay & Notification")
                }
            }

            item {
                Text("Identity Verification", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToProfiles,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Trusted Voice Profiles")
                }
            }

            item {
                SettingToggle(
                    "Voice Protection",
                    "Monitor calls for AI deepfakes and scams",
                    uiState.voiceProtectionEnabled,
                    onToggle = { viewModel.toggleVoiceProtection(it) }
                )
            }

            item {
                Text("Risk Thresholds", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ThresholdSlider("Low", uiState.lowRiskThreshold)
                ThresholdSlider("Medium", uiState.medRiskThreshold)
                ThresholdSlider("High", uiState.highRiskThreshold)
            }

            item {
                Text("Privacy & Compliance", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                SettingToggle(
                    "Delete Raw Audio",
                    "Remove captured audio immediately after analysis",
                    uiState.deleteRawAudio,
                    onToggle = { viewModel.toggleDeleteRawAudio(it) }
                )
            }
        }
    }
}

@Composable
fun SettingToggle(title: String, description: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
fun ThresholdSlider(label: String, value: Float) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp)
            Text("${(value * 100).toInt()}%", fontWeight = FontWeight.Bold)
        }
        Slider(value = value, onValueChange = {}, enabled = false)
    }
}
