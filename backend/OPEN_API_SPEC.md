# VoxShield Security Layer - Open API Specification

This document provides technical documentation for integrating the VoxShield voice security layer into third-party banking, telecom, or enterprise applications.

## 📡 Real-Time WebSocket Interface
**Endpoint**: `ws://<server>:<port>/ws/calls/{call_id}`

### ⬆️ Upstream (App to Server)
Send audio chunks in 200ms - 500ms intervals.

```json
{
  "type": "audio_chunk",
  "call_id": "unique-session-id",
  "sequence": 42,
  "timestamp": 1757263200,
  "audio": "BASE64_PCM16_16KHZ_DATA"
}
```

### ⬇️ Downstream (Server to App)
Receive risk updates and forensic evidence.

```json
{
  "type": "risk_update",
  "call_id": "session-id",
  "risk": {
    "score": 87,
    "level": "HIGH"
  },
  "voice": {
    "synthetic": 0.89,
    "speaker_similarity": 0.61,
    "replay": 0.12,
    "prosody": 0.74
  },
  "decision": "BLOCK",
  "reason_codes": [
    "HIGH_CONFIDENCE_DEEPFAKE",
    "FINANCIAL_FRAUD_INTENT"
  ]
}
```

## 🌐 REST API Endpoints

### 1. Security Analytics
`GET /api/v1/analytics`
Returns aggregated security statistics with detection rates and threat distributions.

### 2. Incident Audit Logs
`GET /api/v1/incidents`
Returns a list of security incidents including tamper-proof SHA-256 `audit_hash` for each record.

### 3. Forensic Lab Simulation
`POST /api/v1/attack-lab/start`
**Body**: `{"attack_type": "voice_clone" | "replay" | "ai_generated"}`

---

## 🛠️ Integration SDK (Conceptual)
VoxShield can be embedded as an Android AAR or iOS Framework.
1. Initialize `VoxShieldManager` with API Gateway URL.
2. Hook into the device's Audio Stream.
3. Observe `RiskUpdate` Flow to trigger UI Alerts or Transaction Holds.
