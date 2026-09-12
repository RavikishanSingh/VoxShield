# VoxShield: Comprehensive Project Documentation & Technical Reference

**AI-Powered Real-Time Detection and Prevention of Voice Cloning & Impersonation Attacks**  
*Smart India Hackathon 2026 (Problem Statement ID: 26104)*

---

## 1. Executive Summary & Problem Statement

Recent breakthroughs in neural speech synthesis, generative AI, and text-to-speech (TTS) models (such as ElevenLabs, Bark, Tortoise, and VITS) have democratized high-fidelity voice cloning. Malicious actors can now clone a person's voice using as little as 3 seconds of reference audio extracted from social media videos or voicemails. 

This has led to a surge in real-time **vishing (voice phishing) and CEO fraud**, where victims receive phone calls from what sounds indistinguishable from their bank manager, government official, or family member, demanding immediate fund transfers or OTP verification. Traditional telephony verification (Caller ID, manual call-backs) fails against real-time generative audio impersonation.

**VoxShield** is a state-of-the-art, end-to-end cyber-security defense framework designed to intercept live voice streams, compute multi-signal AI risk assessments in real-time, detect AI voice clones (deepfakes), and trigger actionable defense mechanisms (such as call termination and cybercrime incident reporting).

---

## 2. End-to-End System Architecture

VoxShield employs a high-performance client-server hybrid architecture. Lightweight edge capture on Android handles local recording, speech recognition, and UI rendering, while a high-throughput Python FastAPI gateway handles neural deepfake detection and RocketRide evidence synthesis.

```mermaid
graph TD
    A[Android App / Mic & SpeechRecognizer] -->|WebSocket 16kHz PCM & Transcripts| B(FastAPI Gateway /ws/calls/{call_id})
    B --> C[Audio Buffer & VAD]
    C --> D[Parallel AI Inference Pipeline]
    
    subgraph "AI Inference & Security Engine"
    D --> D1[Deepfake Spectral Analysis]
    D --> D2[Speaker Verification Vault]
    D --> D3[Whisper ASR & Intent Analysis]
    D --> D4[Prosody Jitter & Shimmer]
    end
    
    D1 & D2 & D3 & D4 --> E[RocketRide Evidence Synthesizer]
    E --> F[Weighted Risk Scoring Engine 0-100]
    F --> G[Deterministic Policy Engine]
    
    G -->|ALLOW / VERIFY / BLOCK| H[WebSocket Downlink]
    H --> A
```

---

## 3. Core Technical Modules

### 3.1 AI Voice Clone Detection (Deepfake / Synthetic Voice)
*   **Spectral & Vocoder Artifact Analysis (`deepfake.py`)**: Evaluates high-frequency spectral flatness and vocoder mirroring artifacts (4kHz-8kHz band energy ratios) characteristic of neural text-to-speech models.
*   **Acoustic Biometric Fingerprints (`LiveCallScreen.kt`)**: Real-time monitoring of pitch jitter, formant stability, and phase coherence.
*   **Main Guard Priority**: Weighted at 50% in the evidence synthesis pipeline, making deepfake detection the primary line of defense.

### 3.2 Native Android Live Transcription (`SpeechRecognizerManager.kt`)
*   **On-Device ASR**: Utilizes Android's native `SpeechRecognizer` API for instantaneous, zero-latency speech transcription directly on the mobile device.
*   **Fail-Safe Conversational Stream**: Combined with a real-time conversational analysis ticker to ensure live transcripts and threat indicators remain 100% active and responsive.

### 3.3 Multilingual Scam Intent Taxonomy (`intent.py`)
*   **Bilingual Support (English + Hinglish)**: Scans transcripts for multi-modal fraud triggers across categories:
    *   *Financial Fraud*: "otp", "pin", "upi", "bank", "transfer", "rupees", "paise", "khata".
    *   *Authority Impersonation*: "police", "cbi", "manager", "rbi", "income tax", "court".
    *   *Urgency & Coercion*: "immediately", "urgently", "arrest", "block", "abhi ke abhi", "jaldi".

### 3.4 RocketRide Evidence Synthesis Engine (`client.py`)
*   Synthesizes multi-signal evidence into an explainable decision vector:
    $$\text{Final Score} = (\text{Voice Clone} \times 0.50) + (\text{Speaker Match} \times 0.25) + (\text{Intent AI} \times 0.15) + (\text{Replay} \times 0.10)$$
*   Applies cross-modal threat boosts (e.g., AI Voice Clone + Financial Request = Extreme Danger).

---

## 4. Academic Research Papers & Industry References

VoxShield's architecture is grounded in peer-reviewed academic research and industry standards in audio forensics, deepfake detection, and secure speech processing:

| Topic / Domain | Reference & Authors | Key Contribution / Relevance | Link |
| :--- | :--- | :--- | :--- |
| **Audio Deepfake Detection** | *ASVspoof: Automatic Speaker Verification Spoofing and Countermeasures*<br>Tak et al. (IEEE / Odyssey / Interspeech) | Benchmark challenge datasets and baseline countermeasures for synthetic speech detection. | [ASVspoof Challenge](https://www.asvspoof.org/) |
| **Robust Speech Recognition** | *Robust Speech Recognition via Large-Scale Weak Supervision*<br>Alec Radford et al. (OpenAI) | Introduces Whisper ASR architecture for multilingual, noise-robust speech transcription. | [OpenAI Whisper Paper](https://arxiv.org/abs/2212.04356) |
| **Neural Audio Forensics** | *Deep Learning for Audio Deepfake Detection: A Survey*<br>IEEE Access / arXiv Surveys | Comprehensive taxonomy of spectral analysis, vocoder artifact detection, and phase incoherence. | [IEEE Deepfake Survey](https://ieeexplore.ieee.org/document/9803450) |
| **Multi-Modal Evidence Synthesis** | *RocketRide AI Pipeline Builder & Evidence Synthesis Specification*<br>RocketRide Architecture Docs | Standards for combining multi-signal asynchronous AI lanes into explainable risk scores. | [RocketRide Docs](.rocketride/docs/ROCKETRIDE_README.md) |
| **Android Security Architecture** | *Android Security & Interprocess Communication Best Practices*<br>Google Android Developers | Guidelines for Foreground Services (`microphone` type), Broadcast Receivers, and System Overlay Windows (`SYSTEM_ALERT_WINDOW`). | [Android Security Docs](https://developer.android.com/topic/security) |

---

## 5. Developer Quickstart & Deployment Guide

### 5.1 Backend Server Setup
```bash
cd backend
python -m venv venv
# On Windows PowerShell:
venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### 5.2 Android App Setup & ADB Port Forwarding
1. Open the project in **Android Studio**.
2. Connect your Android device or start an emulator.
3. Configure ADB reverse port forwarding so the app can reach the local FastAPI backend:
   ```bash
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000
   ```
4. Build and run the app (`app:assembleDebug`) and deploy to your device.
