import numpy as np
import base64
from typing import List

class AudioBuffer:
    def __init__(self, sample_rate: int = 16000):
        self.sample_rate = sample_rate
        self.buffer: List[np.ndarray] = []

    def add_base64_chunk(self, b64_data: str):
        """Decodes base64 PCM16 and adds to buffer."""
        audio_bytes = base64.b64decode(b64_data)
        # Convert bytes to int16, then normalize to float32
        samples = np.frombuffer(audio_bytes, dtype=np.int16).astype(np.float32) / 32768.0
        self.buffer.append(samples)

    def get_window(self, seconds: float = 3.0) -> np.ndarray:
        """Returns the most recent 'seconds' of audio as a single array."""
        if not self.buffer:
            return np.array([], dtype=np.float32)

        all_samples = np.concatenate(self.buffer)
        required_samples = int(seconds * self.sample_rate)

        if len(all_samples) > required_samples:
            return all_samples[-required_samples:]
        return all_samples

    def clear(self):
        self.buffer = []

    def length_seconds(self) -> float:
        if not self.buffer:
            return 0.0
        return sum(len(chunk) for chunk in self.buffer) / self.sample_rate
