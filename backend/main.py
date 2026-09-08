from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from websocket.manager import manager
from audio.buffer import AudioBuffer
from audio.vad import VADProvider
from audio.preprocessing import normalize_audio
from ml.deepfake import DeepfakeDetector
from ml.speaker import SpeakerVerifier
from ml.replay import ReplayDetector
from ml.prosody import ProsodyAnalyzer
from asr.whisper import WhisperASR
from intelligence.intent import IntentEngine
from intelligence.evidence import EvidenceSynthesizer
from risk.engine import risk_engine
from risk.policy import policy_engine
from api import attack_lab, analytics, incidents, profiles
import json
import asyncio
import time
import random
from database.repository import repo

app = FastAPI(title="VoxShield Advanced Security Gateway")

# Initialize AI Components
vad = VADProvider(threshold=0.01)
deepfake_detector = DeepfakeDetector()
speaker_verifier = SpeakerVerifier()
replay_detector = ReplayDetector()
prosody_analyzer = ProsodyAnalyzer()
asr = WhisperASR(model_name="tiny")
intent_engine = IntentEngine()
evidence_synthesizer = EvidenceSynthesizer()

# Include routers
app.include_router(attack_lab.router)
app.include_router(analytics.router)
app.include_router(incidents.router)
app.include_router(profiles.router)

@app.get("/")
async def root():
    return {
        "status": "ok",
        "service": "VoxShield Security Gateway",
        "version": "2.1.0-rocketride"
    }

@app.websocket("/ws/calls/{call_id}")
async def websocket_endpoint(websocket: WebSocket, call_id: str):
    await manager.connect(call_id, websocket)

    is_lab = call_id.startswith("LAB-")
    is_clone_test = (call_id == "TEST-CLONE")

    if is_lab:
        print(f"Opening Forensic Lab Session: {call_id}")
    elif is_clone_test:
        print("ALERT: High-Sensitivity Voice Clone Test Mode Active")

    # Initialize per-call session state
    audio_buffer = AudioBuffer(sample_rate=16000)
    chunk_count = 0

    try:
        while True:
            data = await websocket.receive_text()
            message = json.loads(data)

            if message.get("type") == "audio_chunk":
                chunk_count += 1
                audio_buffer.add_base64_chunk(message.get("audio", ""))

                # Process every 1-2 seconds
                interval = 5 if is_lab else 10

                if chunk_count % interval == 0:
                    window = audio_buffer.get_window(seconds=3.0)

                    if vad.is_speech(window):
                        clean_audio = normalize_audio(window)

                        # 1. Voice ML Signals
                        raw_deepfake = deepfake_detector.detect(clean_audio)
                        if is_clone_test:
                            raw_deepfake = min(1.0, raw_deepfake * 1.5)

                        # Cross-session consistency check (Profile matching)
                        trusted_profile = profiles.get_trusted_voice(call_id)
                        speaker_similarity = speaker_verifier.verify(
                            clean_audio,
                            reference_embedding=trusted_profile["embedding"] if trusted_profile else None
                        )

                        ml_results = {
                            "deepfake": raw_deepfake,
                            "speaker": speaker_similarity,
                            "replay": replay_detector.detect(clean_audio),
                            "prosody": prosody_analyzer.analyze(clean_audio)
                        }

                        # 2. Real Whisper Transcription
                        try:
                            transcript = asr.transcribe(clean_audio)
                            transcript = transcript.replace("[", "").replace("]", "").strip()
                            if transcript:
                                print(f"[{call_id}] Transcribe: {transcript}")
                        except Exception as e:
                            transcript = ""

                        intent_results = intent_engine.analyze(transcript)

                        # 3. Evidence Synthesis (RocketRide Pipeline)
                        synthesis_result = evidence_synthesizer.synthesize(ml_results, intent_results)

                        # 4. Risk & Policy
                        score = synthesis_result["risk_score"] * 100
                        decision = policy_engine.get_decision(score)

                        # UI Display Reasons
                        display_reasons = []
                        if transcript:
                            display_reasons.append(f"Speech: \"{transcript}\"")
                        display_reasons.extend(synthesis_result["reason_codes"])
                        if not display_reasons:
                            display_reasons = ["Analyzing stream..."]

                        # Record incident if critical (only for real calls)
                        if not is_lab and decision == "BLOCK":
                            repo.record_incident({
                                "id": f"INC-{int(time.time())}",
                                "caller": call_id,
                                "risk_score": int(score),
                                "decision": decision,
                                "reasons": display_reasons
                            })

                        risk_update = {
                            "type": "risk_update",
                            "call_id": call_id,
                            "risk": {
                                "score": int(score),
                                "level": "CRITICAL" if score > 85 else "HIGH" if score > 60 else "MEDIUM" if score > 30 else "LOW"
                            },
                            "voice": {
                                "synthetic": ml_results["deepfake"],
                                "speaker_similarity": ml_results["speaker"],
                                "replay": ml_results["replay"],
                                "prosody": ml_results["prosody"]
                            },
                            "context": {
                                "financial_intent": "FINANCIAL_FRAUD" in intent_results["intents"],
                                "urgency": 0.9 if "URGENCY_COERCION" in intent_results["intents"] else 0.1,
                                "reason_codes": display_reasons
                            },
                            "decision": decision
                        }
                        await manager.send_json(call_id, risk_update)

    except WebSocketDisconnect:
        manager.disconnect(call_id)
    except Exception as e:
        print(f"Error in secure session {call_id}: {e}")
        manager.disconnect(call_id)
