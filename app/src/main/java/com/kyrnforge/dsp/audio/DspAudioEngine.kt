package com.kyrnforge.dsp.audio

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class DspAudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "DspAudioEngine"
        const val GLOBAL_SESSION_ID = 0
        const val MAX_GAIN_MB = 1500 // 100% boost = +15 dB (scaled by preset, capped at +22 dB)
        const val EFFECT_PRIORITY = Int.MAX_VALUE // win control of session 0 over OEM effects (Dolby, SoundAlive...)
        const val MIN_LIMITER_RATIO = 10f // brickwall: boosted peaks must not reach 0 dBFS and clip

        // 0% = 0 dB; 100% = MAX_GAIN_MB scaled by preset (Studio +10.5 dB, Party +22 dB)
        fun boostGainMb(percent: Int, preset: DspPreset): Int =
            ((percent / 100f) * MAX_GAIN_MB * (preset.targetGainMb / 500f)).roundToInt().coerceIn(0, 2200)
    }

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var dynamicsProcessing: DynamicsProcessing? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _boostLevelPercent = MutableStateFlow(40) // 0% to 100%
    val boostLevelPercent: StateFlow<Int> = _boostLevelPercent.asStateFlow()

    private val _antiDistortionActive = MutableStateFlow(true)
    val antiDistortionActive: StateFlow<Boolean> = _antiDistortionActive.asStateFlow()

    private val _vocalClarityPercent = MutableStateFlow(50) // 0% to 100%
    val vocalClarityPercent: StateFlow<Int> = _vocalClarityPercent.asStateFlow()

    private val _bassResonancePercent = MutableStateFlow(35) // 0% to 100%
    val bassResonancePercent: StateFlow<Int> = _bassResonancePercent.asStateFlow()

    private val _spatialVirtualizerPercent = MutableStateFlow(25) // 0% to 100%
    val spatialVirtualizerPercent: StateFlow<Int> = _spatialVirtualizerPercent.asStateFlow()

    private val _currentPreset = MutableStateFlow(DspPreset.ALL_PRESETS.first())
    val currentPreset: StateFlow<DspPreset> = _currentPreset.asStateFlow()

    private var currentDevice: AudioOutputDevice = AudioOutputDevice.defaultSpeaker()

    fun initialize() {
        broadcastSessionOpen()
        rebuildAudioEffects()
        applyAllParameters()
    }

    fun release() {
        releaseEffects()
        broadcastSessionClose()
    }

    private fun broadcastSessionOpen() {
        try {
            val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, GLOBAL_SESSION_ID)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting session open", e)
        }
    }

    private fun broadcastSessionClose() {
        try {
            val intent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, GLOBAL_SESSION_ID)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting session close", e)
        }
    }

    @Synchronized
    private fun rebuildAudioEffects() {
        releaseEffects()
        try {
            loudnessEnhancer = LoudnessEnhancer(GLOBAL_SESSION_ID).apply {
                enabled = false // fallback only, enabled below if DynamicsProcessing fails
            }
        } catch (e: Exception) {
            Log.w(TAG, "LoudnessEnhancer init failed", e)
        }

        // DynamicsProcessing carries the boost (input gain) and the limiter after it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val preset = _currentPreset.value
                val channelCount = 2
                val configBuilder = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    channelCount,
                    false, // PreEQ in use
                    0,
                    false, // Multiband compressor in use
                    0,
                    false, // PostEQ
                    0,
                    true   // Limiter in use
                )

                val limiter = DynamicsProcessing.Limiter(
                    true,                                // inUse
                    _antiDistortionActive.value,         // enabled
                    0,                                   // linkGroup
                    preset.limiterAttackMs,              // attackTime (ms)
                    preset.limiterReleaseMs,             // releaseTime (ms)
                    maxOf(preset.dynamicRangeRatio, MIN_LIMITER_RATIO), // ratio (brickwall)
                    preset.limiterThresholdDb,           // threshold (e.g. -1.5 dBFS)
                    0.0f                                 // postGain
                )

                val config = configBuilder.build()
                for (ch in 0 until channelCount) {
                    config.setLimiterByChannelIndex(ch, limiter)
                }

                dynamicsProcessing = DynamicsProcessing(EFFECT_PRIORITY, GLOBAL_SESSION_ID, config).apply {
                    enabled = _isEnabled.value
                    if (!hasControl()) Log.w(TAG, "DynamicsProcessing: another app controls session 0")
                }
            } catch (e: Exception) {
                Log.w(TAG, "DynamicsProcessing init fallback", e)
            }
        }
        loudnessEnhancer?.enabled = _isEnabled.value && dynamicsProcessing == null

        try {
            bassBoost = BassBoost(0, GLOBAL_SESSION_ID).apply {
                enabled = _isEnabled.value
            }
        } catch (e: Exception) {
            Log.w(TAG, "BassBoost init failed", e)
        }

        try {
            virtualizer = Virtualizer(0, GLOBAL_SESSION_ID).apply {
                enabled = _isEnabled.value && currentDevice.category != DeviceCategory.INTERNAL_SPEAKER
            }
        } catch (e: Exception) {
            Log.w(TAG, "Virtualizer init failed", e)
        }
    }

    private fun releaseEffects() {
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        loudnessEnhancer = null

        try { dynamicsProcessing?.release() } catch (_: Exception) {}
        dynamicsProcessing = null

        try { bassBoost?.release() } catch (_: Exception) {}
        bassBoost = null

        try { virtualizer?.release() } catch (_: Exception) {}
        virtualizer = null
    }

    fun setMasterBoost(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _boostLevelPercent.value = clamped
        applyLoudnessGain()
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        loudnessEnhancer?.enabled = enabled && dynamicsProcessing == null
        dynamicsProcessing?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled && currentDevice.category != DeviceCategory.INTERNAL_SPEAKER
    }

    fun setAntiDistortionActive(active: Boolean) {
        _antiDistortionActive.value = active
        applyLimiterSettings()
    }

    fun setVocalClarity(percent: Int) {
        _vocalClarityPercent.value = percent.coerceIn(0, 100)
        applyVocalClarity()
    }

    fun setBassResonance(percent: Int) {
        _bassResonancePercent.value = percent.coerceIn(0, 100)
        applyBassSettings()
    }

    fun setSpatialVirtualizer(percent: Int) {
        _spatialVirtualizerPercent.value = percent.coerceIn(0, 100)
        applyVirtualizerSettings()
    }

    fun selectPreset(preset: DspPreset) {
        _currentPreset.value = preset
        applyAllParameters()
    }

    fun onDeviceChanged(device: AudioOutputDevice) {
        currentDevice = device
        applyBassSettings()
        applyVirtualizerSettings()
    }

    @Synchronized
    private fun applyAllParameters() {
        applyLoudnessGain()
        applyLimiterSettings()
        applyVocalClarity()
        applyBassSettings()
        applyVirtualizerSettings()
    }

    private fun applyLoudnessGain() {
        val gainMb = boostGainMb(_boostLevelPercent.value, _currentPreset.value)

        try {
            val dp = dynamicsProcessing
            if (dp != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dp.setInputGainAllChannelsTo(gainMb / 100f) // dB, before the limiter
            } else {
                loudnessEnhancer?.setTargetGain(gainMb)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying target gain", e)
        }
    }

    private fun applyLimiterSettings() {
        val dp = dynamicsProcessing ?: return
        val active = _antiDistortionActive.value
        val preset = _currentPreset.value

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val limiter = DynamicsProcessing.Limiter(
                    true,
                    active,
                    0,
                    preset.limiterAttackMs,
                    preset.limiterReleaseMs,
                    if (active) maxOf(preset.dynamicRangeRatio, MIN_LIMITER_RATIO) else 1.0f,
                    if (active) preset.limiterThresholdDb else 0.0f,
                    0.0f
                )
                for (ch in 0 until 2) {
                    dp.setLimiterByChannelIndex(ch, limiter)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating limiter", e)
            }
        }
    }

    private fun applyVocalClarity() {
        // shortcut: not implemented, the slider only stores the value; needs a PreEQ band in DynamicsProcessing
    }

    private fun applyBassSettings() {
        val bb = bassBoost ?: return
        val rawPercent = _bassResonancePercent.value
        val preset = _currentPreset.value

        // Phone speakers rattle with strong bass, so cap it there
        val effectivePercent = if (currentDevice.category == DeviceCategory.INTERNAL_SPEAKER) {
            (rawPercent * 0.35f).roundToInt()
        } else {
            rawPercent
        }

        val strength = ((effectivePercent / 100f) * (preset.bassBoostStrength.toFloat() * 1.5f))
            .roundToInt().coerceIn(0, 1000).toShort()

        try {
            if (bb.strengthSupported) {
                bb.setStrength(strength)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying bass boost", e)
        }
    }

    private fun applyVirtualizerSettings() {
        val virt = virtualizer ?: return
        val rawPercent = _spatialVirtualizerPercent.value
        val isSpeaker = currentDevice.category == DeviceCategory.INTERNAL_SPEAKER

        if (isSpeaker) {
            try { virt.enabled = false } catch (_: Exception) {}
            return
        }

        val strength = ((rawPercent / 100f) * 1000f).roundToInt().coerceIn(0, 1000).toShort()
        try {
            virt.enabled = _isEnabled.value && strength > 0
            if (virt.strengthSupported) {
                virt.setStrength(strength)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying virtualizer", e)
        }
    }
}
