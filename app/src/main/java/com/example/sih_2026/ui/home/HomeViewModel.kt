package com.example.sih_2026.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.viewModelScope
import com.example.sih_2026.core.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

data class HomeUiState(
    val isProtectionActive: Boolean = true,
    val callsAnalyzed: Int = 0,
    val suspiciousCalls: Int = 0,
    val attacksBlocked: Int = 0,
    val securityHealthScore: Int = 100,
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        refreshStats()
    }

    fun refreshStats() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/api/v1/analytics")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@launch
                        val result = json.parseToJsonElement(body).jsonObject
                        
                        val analyzed = result["totalCallsAnalyzed"]?.jsonPrimitive?.int ?: 18
                        val threats = result["totalThreats"]?.jsonPrimitive?.int ?: 2
                        val blocked = result["attacksBlocked"]?.jsonPrimitive?.int ?: 0
                        
                        // Calculate Health: 100 - (threats * 10) bounded
                        val health = (100 - (threats * 15)).coerceIn(20, 100)

                        _uiState.value = _uiState.value.copy(
                            callsAnalyzed = analyzed,
                            suspiciousCalls = threats,
                            attacksBlocked = blocked,
                            securityHealthScore = health,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback to dummy data
                _uiState.value = _uiState.value.copy(
                    callsAnalyzed = 18,
                    suspiciousCalls = 2,
                    attacksBlocked = 0,
                    securityHealthScore = 85,
                    isLoading = false
                )
            }
        }
    }
}
