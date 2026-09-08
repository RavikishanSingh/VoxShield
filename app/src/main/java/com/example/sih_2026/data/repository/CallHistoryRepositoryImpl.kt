package com.example.sih_2026.data.repository

import com.example.sih_2026.domain.model.CallLogEntry
import com.example.sih_2026.domain.repository.CallHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallHistoryRepositoryImpl : CallHistoryRepository {

    private val _callLogs = MutableStateFlow<List<CallLogEntry>>(
        listOf(
            CallLogEntry(
                callerName = "Amit (Brother - Impersonator)",
                phoneNumber = "+91 98765 43210",
                timestamp = "Today, 11:32 AM",
                riskScore = 92,
                riskLevel = "CRITICAL",
                reasons = listOf("Synthetic voice signature detected", "Financial request (₹20,000)", "Urgency tactics")
            ),
            CallLogEntry(
                callerName = "Unknown Bank Official",
                phoneNumber = "+91 88221 11900",
                timestamp = "Yesterday, 4:15 PM",
                riskScore = 78,
                riskLevel = "HIGH",
                reasons = listOf("OTP / PIN requested", "Impersonation alert")
            )
        )
    )
    override val callLogs: StateFlow<List<CallLogEntry>> = _callLogs.asStateFlow()

    private val _totalCallsMonitored = MutableStateFlow(142)
    override val totalCallsMonitored: StateFlow<Int> = _totalCallsMonitored.asStateFlow()

    private val _deepfakesBlocked = MutableStateFlow(11)
    override val deepfakesBlocked: StateFlow<Int> = _deepfakesBlocked.asStateFlow()

    private val _scamIntentsIntercepted = MutableStateFlow(19)
    override val scamIntentsIntercepted: StateFlow<Int> = _scamIntentsIntercepted.asStateFlow()

    override fun recordCall(callerName: String, phoneNumber: String, riskScore: Int, riskLevel: String, reasons: List<String>) {
        val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val newEntry = CallLogEntry(
            callerName = callerName,
            phoneNumber = phoneNumber,
            timestamp = timestamp,
            riskScore = riskScore,
            riskLevel = riskLevel,
            reasons = reasons
        )

        val updatedList = mutableListOf(newEntry) + _callLogs.value
        _callLogs.value = updatedList

        _totalCallsMonitored.value += 1
        if (riskScore > 70) {
            _deepfakesBlocked.value += 1
            _scamIntentsIntercepted.value += 1
        }
    }
}
