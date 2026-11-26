package com.kyrnforge.dsp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kyrnforge.dsp.AuraSoundApp
import com.kyrnforge.dsp.audio.AudioOutputDevice
import com.kyrnforge.dsp.audio.DspPreset
import com.kyrnforge.dsp.audio.FrequencyBands
import com.kyrnforge.dsp.service.AuraSoundService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuraSoundViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AuraSoundApp
    private val engine = app.audioEngine
    private val detector = app.deviceDetector
    private val spectrumAnalyzer = app.spectrumAnalyzer

    val isEnabled: StateFlow<Boolean> = engine.isEnabled
    val boostPercent: StateFlow<Int> = engine.boostLevelPercent
    val antiDistortionActive: StateFlow<Boolean> = engine.antiDistortionActive
    val vocalClarityPercent: StateFlow<Int> = engine.vocalClarityPercent
    val bassResonancePercent: StateFlow<Int> = engine.bassResonancePercent
    val spatialPercent: StateFlow<Int> = engine.spatialVirtualizerPercent
    val currentPreset: StateFlow<DspPreset> = engine.currentPreset

    val currentDevice: StateFlow<AudioOutputDevice> = detector.currentDevice
    val spectrum: StateFlow<FrequencyBands> = spectrumAnalyzer.spectrumFlow

    init {
        spectrumAnalyzer.start()

        AuraSoundService.start(getApplication())

        viewModelScope.launch {
            detector.currentDevice.collect { device ->
                engine.onDeviceChanged(device)
            }
        }
    }

    fun togglePower() {
        val newState = !engine.isEnabled.value
        engine.setEnabled(newState)
    }

    fun setBoost(percent: Int) {
        engine.setMasterBoost(percent)
    }

    fun setAntiDistortion(active: Boolean) {
        engine.setAntiDistortionActive(active)
    }

    fun setVocalClarity(percent: Int) {
        engine.setVocalClarity(percent)
    }

    fun setBass(percent: Int) {
        engine.setBassResonance(percent)
    }

    fun setSpatial(percent: Int) {
        engine.setSpatialVirtualizer(percent)
    }

    fun selectPreset(preset: DspPreset) {
        engine.selectPreset(preset)
    }

    fun refreshDevices() {
        detector.refreshAudioDevice()
    }

    fun onPermissionsGranted() {
        detector.refreshAudioDevice()
        spectrumAnalyzer.onPermissionGranted()
    }

    fun onResume() {
        detector.refreshAudioDevice()
        spectrumAnalyzer.ensureAttached()
    }

    override fun onCleared() {
        super.onCleared()
        spectrumAnalyzer.stop()
    }
}
