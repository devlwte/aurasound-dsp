package com.kyrnforge.dsp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kyrnforge.dsp.audio.AudioDeviceDetector
import com.kyrnforge.dsp.audio.DspAudioEngine
import com.kyrnforge.dsp.audio.SpectrumAnalyzer

class AuraSoundApp : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "aurasound_dsp_channel"
        lateinit var instance: AuraSoundApp
            private set
    }

    lateinit var audioEngine: DspAudioEngine
        private set
    lateinit var deviceDetector: AudioDeviceDetector
        private set
    lateinit var spectrumAnalyzer: SpectrumAnalyzer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()

        audioEngine = DspAudioEngine(this).apply {
            initialize()
        }

        deviceDetector = AudioDeviceDetector(this).apply {
            startListening()
        }

        spectrumAnalyzer = SpectrumAnalyzer(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
