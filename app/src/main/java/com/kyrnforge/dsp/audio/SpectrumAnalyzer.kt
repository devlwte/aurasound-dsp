package com.kyrnforge.dsp.audio

import android.content.Context
import android.media.audiofx.Visualizer
import android.util.Log
import com.kyrnforge.dsp.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.sin

data class FrequencyBands(
    val bars: FloatArray = FloatArray(BAND_COUNT),
    val peakDots: FloatArray = FloatArray(BAND_COUNT)
) {
    companion object {
        const val BAND_COUNT = 10
        val BAND_LABELS = listOf("32", "64", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrequencyBands) return false
        return bars.contentEquals(other.bars) && peakDots.contentEquals(other.peakDots)
    }

    override fun hashCode(): Int {
        var result = bars.contentHashCode()
        result = 31 * result + peakDots.contentHashCode()
        return result
    }
}

class SpectrumAnalyzer(private val context: Context) {

    companion object {
        private const val TAG = "SpectrumAnalyzer"
        private const val CAPTURE_SIZE = 256
        private const val DECAY_RATE = 0.065f
        private const val PEAK_DECAY = 0.030f
    }

    @Volatile
    private var visualizer: Visualizer? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var idleAnimationJob: Job? = null

    private val currentBars = FloatArray(FrequencyBands.BAND_COUNT)
    private val currentPeaks = FloatArray(FrequencyBands.BAND_COUNT)

    private val _spectrumFlow = MutableStateFlow(FrequencyBands())
    val spectrumFlow: StateFlow<FrequencyBands> = _spectrumFlow.asStateFlow()

    fun start() {
        if (!tryAttachVisualizer()) {
            startIdleAnimation()
        }
    }

    fun onPermissionGranted() {
        if (visualizer == null) {
            if (tryAttachVisualizer()) {
                stopIdleAnimation()
            }
        }
    }

    fun ensureAttached() {
        if (visualizer == null && PermissionHelper.hasRecordAudioPermission(context)) {
            if (tryAttachVisualizer()) {
                stopIdleAnimation()
            }
        }
    }

    fun stop() {
        releaseVisualizer()
        stopIdleAnimation()
    }

    @Synchronized
    fun tryAttachVisualizer(): Boolean {
        if (visualizer != null) return true

        if (!PermissionHelper.hasRecordAudioPermission(context)) {
            Log.d(TAG, "Audio permission not yet granted")
            return false
        }

        return try {
            val vis = Visualizer(0)
            vis.captureSize = CAPTURE_SIZE
            vis.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}

                override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                    if (fft != null && fft.isNotEmpty()) {
                        processFftData(fft)
                    }
                }
            }, Visualizer.getMaxCaptureRate(), false, true)
            vis.enabled = true
            visualizer = vis
            stopIdleAnimation()
            Log.d(TAG, "Visualizer attached and enabled successfully on session 0")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Visualizer attach error: ${e.message}")
            releaseVisualizer()
            false
        }
    }

    @Synchronized
    private fun releaseVisualizer() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {}
        visualizer = null
    }

    private fun processFftData(fft: ByteArray) {
        val n = fft.size / 2
        val magnitudes = FloatArray(n)

        for (i in 1 until n) {
            val r = fft[2 * i].toFloat()
            val im = fft[2 * i + 1].toFloat()
            magnitudes[i] = hypot(r, im)
        }

        val bandRanges = arrayOf(
            1..2,    // ~32 Hz
            3..5,    // ~64 Hz
            6..10,   // ~125 Hz
            11..18,  // ~250 Hz
            19..30,  // ~500 Hz
            31..48,  // ~1 kHz
            49..70,  // ~2 kHz
            71..95,  // ~4 kHz
            96..115, // ~8 kHz
            116..(n - 1).coerceAtLeast(116) // ~16 kHz
        )

        for (b in 0 until FrequencyBands.BAND_COUNT) {
            val range = bandRanges[b]
            var sum = 0f
            var count = 0
            for (idx in range) {
                if (idx < n) {
                    sum += magnitudes[idx]
                    count++
                }
            }
            val avg = if (count > 0) sum / count else 0f
            val curveFactor = 1.0f + (b * 0.16f)
            val targetLevel = ((avg / 70f) * curveFactor).coerceIn(0.0f, 1.0f)

            // Ease toward the new level instead of jumping
            if (targetLevel > currentBars[b]) {
                currentBars[b] = currentBars[b] + (targetLevel - currentBars[b]) * 0.70f
            } else {
                currentBars[b] = (currentBars[b] - DECAY_RATE).coerceAtLeast(0.0f)
            }

            if (currentBars[b] > currentPeaks[b]) {
                currentPeaks[b] = currentBars[b]
            } else {
                currentPeaks[b] = (currentPeaks[b] - PEAK_DECAY).coerceAtLeast(0.0f)
            }
        }

        _spectrumFlow.value = FrequencyBands(currentBars.clone(), currentPeaks.clone())
    }

    private fun startIdleAnimation() {
        if (idleAnimationJob?.isActive == true) return
        idleAnimationJob = scope.launch {
            var step = 0f
            while (isActive && visualizer == null) {
                step += 0.06f
                for (b in 0 until FrequencyBands.BAND_COUNT) {
                    val base = 0.20f + 0.12f * sin(step + b * 0.60f).toFloat()
                    val target = base.coerceIn(0.05f, 0.60f)
                    currentBars[b] = target
                    currentPeaks[b] = (target + 0.05f).coerceAtMost(0.70f)
                }
                _spectrumFlow.value = FrequencyBands(currentBars.clone(), currentPeaks.clone())
                delay(33) // ~30 FPS
            }
        }
    }

    private fun stopIdleAnimation() {
        idleAnimationJob?.cancel()
        idleAnimationJob = null
    }
}
