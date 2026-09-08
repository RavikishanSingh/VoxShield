package com.example.sih_2026.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class VoxShieldNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "VoxShieldNotifListener"
        private val otpKeywords = listOf("otp", "code", "verify", "pin", "password", "debit", "credited", "rs", "₹")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""
        val combined = "$title $text".lowercase()

        if (otpKeywords.any { combined.contains(it) }) {
            Log.d(TAG, "Financial / OTP notification detected: Title=$title, Text=$text")
            val isCallActive = CallMonitorService.isMonitoring.value
            if (isCallActive) {
                Log.w(TAG, "⚠️ ALERT: OTP/Financial notification received while on an active call!")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
