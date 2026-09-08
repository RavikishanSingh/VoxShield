import numpy as np

def normalize_audio(audio: np.ndarray) -> np.ndarray:
    """RMS Normalization to a target level."""
    rms = np.sqrt(np.mean(audio**2))
    if rms < 1e-6:
        return audio

    target_rms = 0.1
    gain = target_rms / rms
    return np.clip(audio * gain, -1.0, 1.0)
