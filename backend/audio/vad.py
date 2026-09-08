import numpy as np

class VADProvider:
    def __init__(self, threshold: float = 0.01):
        self.threshold = threshold

    def is_speech(self, audio: np.ndarray) -> bool:
        """Simple energy-based VAD."""
        if len(audio) == 0:
            return False
        rms = np.sqrt(np.mean(audio**2))
        return rms > self.threshold
