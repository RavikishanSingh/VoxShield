package com.example.sih_2026.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class VoxShieldAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "VoxShieldAccessibility"
        private val financialAppPackages = listOf(
            "com.google.android.apps.nbu.paisa.user", // GPay
            "com.phonepe.app",                         // PhonePe
            "net.one97.paytm",                         // Paytm
            "in.org.npci.upiapp",                      // BHIM
            "com.sbi.lotusintouch",                    // YONO SBI
            "com.icicibank.mobilebanking"              // ICICI
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (financialAppPackages.any { packageName.contains(it) }) {
                Log.d(TAG, "Financial app opened during active session: $packageName")
                val isCallActive = CallMonitorService.isMonitoring.value
                val currentRisk = CallMonitorService.currentRisk.value

                if (isCallActive && (currentRisk == null || currentRisk.finalScore > 50)) {
                    Log.w(TAG, "⚠️ SECURITY ALERT: Financial app opened while on a suspicious call!")
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted.")
    }
}
