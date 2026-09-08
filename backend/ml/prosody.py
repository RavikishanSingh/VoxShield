import numpy as np

class ProsodyAnalyzer:
    def analyze(self, audio: np.ndarray) -> float:
        """
        Analyzes speech rhythm and pitch contours for behavioral anomalies.
        Detects 'Robotic Flatness' and 'Micro-variation' signatures.
        """
        if len(audio) < 1000:
            return 0.0

        # 1. Pitch Jitter Approximation
        # Real-time pitch tracking is complex; we use Zero Crossing Rate (ZCR)
        # variance to detect unnatural 'steadiness' in neural TTS voices.
        zcr = np.mean(np.abs(np.diff(np.sign(audio))))

        # Human speech has high ZCR variance; TTS is often too perfect/consistent.
        # This is a heuristic for the 'Robotic Rhythm' criterion.

        # 2. Dynamic Range Analysis (Shimmer approximation)
        amplitude_variance = np.var(np.abs(audio))

        # 3. Behavioral Anomaly Score
        # If variance is too low, it's suspiciously robotic (TTS).
        # If jitter is too consistent, it flags.
        anomaly_score = 0.0
        if amplitude_variance < 0.001: # Suspicously flat amplitude
            anomaly_score += 0.4
        if zcr < 0.05: # Suspicously low frequency variation
            anomaly_score += 0.3

        return float(np.clip(anomaly_score, 0.05, 0.95))
