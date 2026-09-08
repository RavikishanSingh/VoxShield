package com.example.sih_2026.domain.repository

import com.example.sih_2026.domain.model.CallLogEntry
import com.example.sih_2026.data.repository.CallHistoryRepositoryImpl
import kotlinx.coroutines.flow.StateFlow

interface CallHistoryRepository {
    val callLogs: StateFlow<List<CallLogEntry>>
    val totalCallsMonitored: StateFlow<Int>
    val deepfakesBlocked: StateFlow<Int>
    val scamIntentsIntercepted: StateFlow<Int>

    fun recordCall(
        callerName: String,
        phoneNumber: String,
        riskScore: Int,
        riskLevel: String,
        reasons: List<String>
    )

    companion object {
        val INSTANCE: CallHistoryRepository = CallHistoryRepositoryImpl()
    }
}
