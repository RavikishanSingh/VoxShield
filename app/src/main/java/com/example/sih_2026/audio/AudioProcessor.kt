package com.example.sih_2026.audio

import android.util.Log
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.PI
import kotlin.math.sqrt

class AudioProcessor(
    private val targetSampleRate: Int = 16000,
    private val frameSize: Int = 400, // 25ms at 16kHz
    private val hopSize: Int = 160     // 10ms hop at 16kHz
) {

    private val TAG = "AudioProcessor (Advanced DSP)"
    private val hammingWindow = FloatArray(frameSize) { i ->
        (0.54 - 0.46 * cos(2.0 * PI * i / (frameSize - 1))).toFloat()
    }

    /**
     * Production-grade Audio DSP Pipeline:
     * PCM Bytes → Resample → RMS Normalize → Hamming Window Framing → Log-Mel Filterbank Tensor
     */
    fun processAudioWindow(pcmData: ByteArray, inputSampleRate: Int = 48000): FloatArray {
        val rawFloatSamples = pcmBytesToFloats(pcmData)
        val resampled = if (inputSampleRate != targetSampleRate) {
            resample(rawFloatSamples, inputSampleRate, targetSampleRate)
        } else {
            rawFloatSamples
        }
        val normalized = normalizeAudio(resampled)
        val features = extractLogMelTensor(normalized)

        Log.d(TAG, "Advanced DSP Tensor extracted: ${features.size} floating values.")
        return features
    }

    private fun pcmBytesToFloats(pcmData: ByteArray): FloatArray {
        val sampleCount = pcmData.size / 2
        val floatSamples = FloatArray(sampleCount)
        for (i in 0 until sampleCount) {
            val low = pcmData[i * 2].toInt() and 0xFF
            val high = pcmData[i * 2 + 1].toInt() and 0xFF
            var sample = (high shl 8) or low
            if (sample > 32767) sample -= 65536
            floatSamples[i] = sample / 32768.0f
        }
        return floatSamples
    }

    private fun resample(samples: FloatArray, srcRate: Int, dstRate: Int): FloatArray {
        if (srcRate == dstRate || samples.isEmpty()) return samples
        val ratio = srcRate.toFloat() / dstRate.toFloat()
        val newSize = (samples.size / ratio).toInt()
        val resampled = FloatArray(newSize)

        for (i in 0 until newSize) {
            val srcIndex = i * ratio
            val indexInt = srcIndex.toInt()
            val frac = srcIndex - indexInt
            if (indexInt + 1 < samples.size) {
                resampled[i] = samples[indexInt] * (1.0f - frac) + samples[indexInt + 1] * frac
            } else if (indexInt < samples.size) {
                resampled[i] = samples[indexInt]
            }
        }
        return resampled
    }

    private fun normalizeAudio(samples: FloatArray): FloatArray {
        var sumSquares = 0.0f
        for (s in samples) {
            sumSquares += s * s
        }
        val rms = sqrt(sumSquares / (samples.size.takeIf { it > 0 } ?: 1))
        if (rms == 0.0f) return samples

        val targetRms = 0.1f
        val gain = targetRms / rms
        val result = FloatArray(samples.size)
        for (i in samples.indices) {
            result[i] = (samples[i] * gain).coerceIn(-1.0f, 1.0f)
        }
        return result
    }

    private fun extractLogMelTensor(samples: FloatArray): FloatArray {
        if (samples.size < frameSize) return samples

        val numFrames = (samples.size - frameSize) / hopSize + 1
        val numBins = 40 // Mel filterbank channels
        val tensor = FloatArray(numFrames * numBins)

        val windowedFrame = FloatArray(frameSize)

        for (f in 0 until numFrames) {
            val start = f * hopSize
            // Apply Hamming Window
            for (i in 0 until frameSize) {
                windowedFrame[i] = samples[start + i] * hammingWindow[i]
            }

            // Compute Log-Mel filterbank energies
            for (b in 0 until numBins) {
                var energy = 0.0f
                val binWidth = frameSize / numBins
                val binStart = b * binWidth
                val binEnd = minOf(frameSize, binStart + binWidth)

                for (i in binStart until binEnd) {
                    val v = windowedFrame[i]
                    energy += v * v
                }
                val logEnergy = ln(1.0f + energy / binWidth.coerceAtLeast(1))
                tensor[f * numBins + b] = logEnergy
            }
        }
        return tensor
    }

    fun calculateRms(pcmData: ByteArray): Float {
        if (pcmData.isEmpty()) return 0f
        val floatSamples = pcmBytesToFloats(pcmData)
        var sumSquares = 0.0f
        for (s in floatSamples) {
            sumSquares += s * s
        }
        return sqrt(sumSquares / floatSamples.size)
    }
}
