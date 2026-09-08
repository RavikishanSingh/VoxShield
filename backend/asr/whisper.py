import whisper
import numpy as np
import torch
import os

class WhisperASR:
    def __init__(self, model_name="base"):
        # Use CPU for demo if CUDA not available
        device = "cuda" if torch.cuda.is_available() else "cpu"
        print(f"Initializing Whisper model '{model_name}' on {device}...")
        self.model = whisper.load_model(model_name, device=device)

    def transcribe(self, audio: np.ndarray) -> str:
        """Transcribes raw float32 audio array."""
        if len(audio) == 0:
            return ""

        # Whisper expects 16kHz audio
        result = self.model.transcribe(audio, fp16=torch.cuda.is_available())
        return result.get("text", "").strip()
