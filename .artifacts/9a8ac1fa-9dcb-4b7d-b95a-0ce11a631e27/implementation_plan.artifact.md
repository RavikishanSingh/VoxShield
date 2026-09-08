# Advanced WebRTC & Live Call Tracking Implementation Plan

Enhance `WebRTCManager` and call telemetry to provide robust, advanced live call tracking, including real-time audio track monitoring, Voice Activity Detection (VAD), network quality metrics (jitter, packet loss, RTT), and live call analytics.

## User Review Required

> [!IMPORTANT]
> This upgrade transitions `WebRTCManager` from a basic dummy simulation loop to an advanced telemetry-enabled call tracker with real-time audio stream metrics, VAD energy analysis, and connection state monitoring.

## Open Questions

- None. The proposed architecture supports both fallback simulation and real-time WebRTC audio track ingestion.

## Proposed Changes

### WebRTC & Audio Processing

#### [MODIFY] [WebRTCManager.kt](file:///C:/Users/RAVI KISHAN SINGH/AndroidStudioProjects/SIH2026/app/src/main/java/com/example/sih_2026/webrtc/WebRTCManager.kt)
- Add StateFlows for call telemetry: `connectionQuality` (Good, Fair, Poor), `audioLevel` (RMS energy for real-time visualization), `packetLossRate`, and `callDurationSeconds`.
- Implement robust Voice Activity Detection (VAD) on incoming audio chunks.
- Support configurable callback hooks for WebRTC PeerConnection statistics and audio track inspection.

#### [MODIFY] [AudioProcessor.kt](file:///C:/Users/RAVI KISHAN SINGH/AndroidStudioProjects/SIH2026/app/src/main/java/com/example/sih_2026/audio/AudioProcessor.kt)
- Add helper method to calculate RMS audio energy levels from PCM chunks for real-time visualizers and VAD.

#### [MODIFY] [CallScreen.kt](file:///C:/Users/RAVI KISHAN SINGH/AndroidStudioProjects/SIH2026/app/src/main/java/com/example/sih_2026/ui/CallScreen.kt)
- Display advanced live call metrics: Network Quality indicator, Call Timer, Audio Activity level meter, and real-time telemetry badge.

## Verification Plan

### Automated Tests
- Run unit tests and Gradle build (`app:assembleDebug`) to verify compilation and structural integrity.

### Manual Verification
- Start a call simulation from the app and verify live telemetry metrics, audio level reactions, and connection status updates on the `CallScreen`.
