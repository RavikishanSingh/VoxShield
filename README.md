# 🛡️ VoxShield: AI-Powered Real-Time Voice Cloning & Impersonation Defense
*Smart India Hackathon 2026 — Problem Statement ID: 26104*

[![Kotlin](https://img.shields.io/badge/Android-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![FastAPI](https://img.shields.io/badge/Backend-FastAPI-teal.svg)](https://fastapi.tiangolo.com/)
[![PyTorch](https://img.shields.io/badge/AI-PyTorch%20%2F%20Whisper-orange.svg)](https://pytorch.org/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

---

## 📖 Executive Summary
**VoxShield** is a production-grade, privacy-preserving mobile security platform engineered to detect and prevent real-time voice cloning, deepfake impersonation, and social engineering attacks during live calls. Designed specifically to solve **SIH 2026 Problem Statement 26104**, VoxShield bridges the gap in modern telephony by combining advanced deep learning, digital signal processing (DSP), and contextual intent analysis into an actionable real-time defense framework.

---

## 🛠️ Comprehensive Tech Stack

### Frontend (Android Client)
*   **Language**: [Kotlin](https://kotlinlang.org/) (Coroutines, StateFlow for asynchronous reactive UI)
*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3 (Declarative UI, Custom Animations, Canvas visualizers)
*   **Navigation**: Jetpack Navigation Compose (Type-safe destinations across 7 core screens)
*   **Audio Hardware**: Android `AudioRecord` (16kHz PCM16 Mono capture via `VOICE_RECOGNITION` source)
*   **Networking**: [OkHttp 4](https://square.github.io/okhttp/) (WebSocket bi-directional streaming & REST API client)
*   **Serialization**: Kotlinx Serialization (`kotlinx-serialization-json`) for strict protocol compliance

### Backend (AI Gateway & Intelligence)
*   **Framework**: [FastAPI](https://fastapi.tiangolo.com/) (High-performance async Python web framework)
*   **ASGI Server**: [Uvicorn](https://www.uvicorn.org/) (Lightning-fast lightning server implementation)
*   **Speech-to-Text**: [OpenAI Whisper](https://github.com/openai/whisper) (Tiny/Base model for real-time speech transcription)
*   **Deep Learning & DSP**: [PyTorch](https://pytorch.org/) & [NumPy](https://numpy.org/) (Spectral FFT analysis, VAD, jitter/shimmer calculations)
*   **Security & Persistence**: Local JSON-backed Security Vault with **SHA-256 Cryptographic Audit Hashing**

---

## 🧠 In-Depth Code Logic & Data Flow

### 1. Audio Ingestion & Edge Streaming (Android)
*   **`WebRTCManager.kt` / `CallMonitorService.kt`**: When a live session or voice guard is initiated, the app requests `RECORD_AUDIO` permission and initializes an `AudioRecord` instance at 16kHz.
*   **Buffer & Encoding**: Raw PCM16 byte arrays are read in real-time. The `AudioProcessor` calculates RMS energy levels to emit real-time volume states (`audioEnergyLevel`), updating the **Voice Activity Bar** in the UI. Simultaneously, chunks are encoded into Base64 strings and packaged into an `AudioChunk` data class.
*   **WebSocket Uplink**: `WebSocketManager` maintains an active persistent WebSocket connection to `ws://{SERVER_IP}:8000/ws/calls/{call_id}`, continuously streaming the base64 audio packets.

### 2. Server-Side Ingestion & VAD (FastAPI Backend)
*   **`main.py` & `buffer.py`**: The FastAPI WebSocket gateway receives upstream chunks, decodes the Base64 payload, and pushes them into an `AudioBuffer` sliding window (default: 3 seconds).
*   **Voice Activity Detection (`vad.py`)**: Before wasting compute on neural inference, the `VADProvider` computes the Root Mean Square (RMS) energy. If audio falls below the threshold (silence/noise), inference is skipped.
*   **Normalization (`preprocessing.py`)**: Active speech chunks undergo RMS scaling to a target level of $0.1$ to normalize volume differences across devices.

### 3. Parallel AI Inference Pipeline
Once speech is confirmed, the audio window is evaluated concurrently across four specialized modules:
1.  **Deepfake Spectral Analysis (`deepfake.py`)**: Computes Fast Fourier Transforms (FFT) to examine high-frequency band energy ($4\text{kHz}-8\text{kHz}$) relative to mid-bands, exposing vocoder mirroring artifacts.
2.  **Behavioral Prosody (`prosody.py`)**: Approximates pitch jitter and amplitude shimmer using Zero Crossing Rate variance. Unnatural "robotic flatness" heavily penalizes the prosody score.
3.  **Speaker Verification (`speaker.py`)**: Compares ongoing embeddings against enrolled **Trusted Voice Profiles** (stored via `profiles.py`) to catch impersonation attempts of known contacts.
4.  **Whisper ASR (`whisper.py`) & Intent Taxonomy (`intent.py`)**: OpenAI Whisper transcribes the audio window into text. The `IntentEngine` scans the transcript against a multilingual taxonomy (English + Hindi/Hinglish triggers like `"paise bhej de"` or `"OTP"`).

### 4. RocketRide Evidence Synthesis & Risk Evaluation
*   **`evidence.py` & `client.py`**: The `RocketRideClient` synthesizes ML signals and semantic intents. It applies a **Cross-Modal Threat Boosting** formula: if high deepfake probability correlates with financial transfer intent, an aggressive risk multiplier is applied.
*   **Risk & Policy Engines (`engine.py`, `policy.py`)**: Computes a weighted score ($0-100$). The `PolicyEngine` maps this score to a deterministic directive: `ALLOW`, `VERIFY`, `MFA`, or `BLOCK`.
*   **Tamper-Proof Audit Logging (`repository.py`)**: If a critical threat is blocked, the incident is committed to `security_db.json` along with a cryptographic **SHA-256 Audit Hash**.

### 5. Downlink & Reactive UI StateFlow (Android)
*   **`WebSocketManager.kt`**: Listens for incoming JSON `RiskUpdate` events from the server.
*   **ViewModels (`LiveCallViewModel.kt`, `AttackLabViewModel.kt`)**: Decodes incoming risk updates and emits them into Compose `StateFlow` streams.
*   **UI Rendering (`LiveCallScreen.kt`, `AttackLabScreen.kt`)**: The UI instantly reacts—updating the risk dial color, animating the risk score, appending transcribed text to the scrolling **Dynamic Log**, and displaying **Actionable Challenge-Response Verification** cards when risk spikes.

---

## 📱 Core UI & User Experience (The 7 Screens)

1.  **🛡️ Security Dashboard**: Real-time protection status, daily call statistics, and a live **Security Health Index** dial.
2.  **📞 Incoming Call Security**: Hero screen analyzing incoming calls before connection with live confidence meters.
3.  **🔴 Live Call Protection**: Real-time conversation monitoring featuring a live **Voice Activity Bar**, scrolling live transcript, and **Actionable Challenge-Response Verification** cards.
4.  **🔬 Security Forensics Lab**: A unified testing hub supporting both live mic scanning and local audio file uploads (`.wav`, `.mp3`) for multi-vector fraud analysis.
5.  **🚨 Incident Center**: Detailed logs of blocked threats equipped with cryptographic **SHA-256 audit hashes**.
6.  **📊 Security Analytics**: Detection rate charts and threat distributions over time.
7.  **⚙️ Settings & Trusted Profiles**: Granular control over risk thresholds, privacy options, and enrollment of **Trusted Voice Profiles** for cross-session consistency checks.

---

## ⚙️ Quick Start Guide

### 1. Run the FastAPI Backend
```bash
cd backend
python -m venv venv
# Windows: .\venv\Scripts\Activate | Mac/Linux: source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### 2. Configure Android App
Get your PC's IPv4 address via `ipconfig` and update `Constants.kt`:
```kotlin
private const val SERVER_IP = "YOUR_PC_IP"
```

### 3. Build & Run
Open the project in Android Studio (Ladybug or newer) and click **Run** on your physical Android device or emulator. Ensure both devices are on the same Wi-Fi network.

---

## 📄 Documentation Reference
*   For complete system sequence diagrams, math formulas, and data pipelines, read the [TECHNICAL_WHITE_PAPER.md](TECHNICAL_WHITE_PAPER.md).
*   For enterprise SDK and REST/WebSocket integration specs, read the [backend/OPEN_API_SPEC.md](backend/OPEN_API_SPEC.md).
