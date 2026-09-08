package com.example.sih_2026.ui.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih_2026.core.constants.Constants
import com.example.sih_2026.webrtc.WebRTCManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class VoiceProfile(
    val id: String,
    val name: String,
    val relationship: String,
    val isEnrolled: Boolean = false
)

data class VoiceProfileUiState(
    val profiles: List<VoiceProfile> = listOf(
        VoiceProfile("1", "Mom", "Family"),
        VoiceProfile("2", "Dad", "Family"),
        VoiceProfile("3", "Rahul", "Manager")
    ),
    val isEnrolling: Boolean = false,
    val enrollmentProgress: Float = 0f,
    val enrollmentStatus: String = "IDLE",
    val activeProfileId: String? = null
)

class VoiceProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(VoiceProfileUiState())
    val uiState: StateFlow<VoiceProfileUiState> = _uiState.asStateFlow()
    
    private val webRTCManager = WebRTCManager(application)
    private val client = OkHttpClient()

    fun startEnrollment(profileId: String) {
        val profile = _uiState.value.profiles.find { it.id == profileId } ?: return
        
        _uiState.value = _uiState.value.copy(
            isEnrolling = true,
            activeProfileId = profileId,
            enrollmentStatus = "RECORDING REFERENCE...",
            enrollmentProgress = 0f
        )
        
        viewModelScope.launch {
            webRTCManager.startCall(isCaller = false) { }
            
            for (i in 1..50) {
                delay(100)
                _uiState.value = _uiState.value.copy(enrollmentProgress = i / 50f)
            }
            
            webRTCManager.endCall()
            _uiState.value = _uiState.value.copy(enrollmentStatus = "SYNCING TO VAULT...")

            // Send to backend
            try {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val json = "{\"id\": \"$profileId\", \"name\": \"${profile.name}\", \"embedding_stub\": \"VOICE_PRINT_ALPHA_9\"}"
                val requestBody = json.toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/api/v1/profiles/enroll")
                    .post(requestBody)
                    .build()

                viewModelScope.launch(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val updatedProfiles = _uiState.value.profiles.map {
                                if (it.id == profileId) it.copy(isEnrolled = true) else it
                            }
                            _uiState.value = _uiState.value.copy(
                                profiles = updatedProfiles,
                                isEnrolling = false,
                                enrollmentStatus = "ENROLLED SUCCESSFULLY"
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isEnrolling = false, enrollmentStatus = "SYNC FAILED")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isEnrolling = false, enrollmentStatus = "SYNC FAILED")
            }
        }
    }
}
