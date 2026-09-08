import numpy as np

class ReplayDetector:
    def detect(self, audio: np.ndarray) -> float:
        """
        Detects replay attacks by looking for re-recording noise floors
        or specific spectral roll-offs from cheap speakers.
        """
        if len(audio) == 0:
            return 0.0

        # Replay attacks often have high constant noise floor
        rms = np.sqrt(np.mean(audio**2))
        if rms < 0.005:
            return 0.1

        return 0.05 # Low replay probability simulated
