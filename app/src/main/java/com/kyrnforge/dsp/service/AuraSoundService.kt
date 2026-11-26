package com.kyrnforge.dsp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kyrnforge.dsp.AuraSoundApp
import com.kyrnforge.dsp.MainActivity
import com.kyrnforge.dsp.R
import com.kyrnforge.dsp.audio.DspPreset

class AuraSoundService : Service() {

    companion object {
        const val ACTION_START = "com.kyrnforge.dsp.action.START"
        const val ACTION_STOP = "com.kyrnforge.dsp.action.STOP"
        const val ACTION_TOGGLE_ENABLE = "com.kyrnforge.dsp.action.TOGGLE_ENABLE"
        const val ACTION_CYCLE_PRESET = "com.kyrnforge.dsp.action.CYCLE_PRESET"
        const val ACTION_BOOST_UP = "com.kyrnforge.dsp.action.BOOST_UP"

        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, AuraSoundService::class.java).apply {
                action = ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AuraSoundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = AuraSoundApp.instance
        val engine = app.audioEngine

        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_ENABLE -> {
                engine.setEnabled(!engine.isEnabled.value)
                updateNotification()
            }
            ACTION_CYCLE_PRESET -> {
                val presets = DspPreset.ALL_PRESETS
                val currentIndex = presets.indexOfFirst { it.id == engine.currentPreset.value.id }
                val nextPreset = presets[(currentIndex + 1) % presets.size]
                engine.selectPreset(nextPreset)
                updateNotification()
            }
            ACTION_BOOST_UP -> {
                val current = engine.boostLevelPercent.value
                val next = (current + 15).coerceAtMost(100)
                engine.setMasterBoost(next)
                updateNotification()
            }
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        }

        return START_STICKY
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val app = AuraSoundApp.instance
        val engine = app.audioEngine
        val device = app.deviceDetector.currentDevice.value
        val isEnabled = engine.isEnabled.value
        val boost = engine.boostLevelPercent.value
        val preset = engine.currentPreset.value

        val openAppIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val toggleIntent = Intent(this, AuraSoundService::class.java).apply { action = ACTION_TOGGLE_ENABLE }.let {
            PendingIntent.getService(this, 1, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val presetIntent = Intent(this, AuraSoundService::class.java).apply { action = ACTION_CYCLE_PRESET }.let {
            PendingIntent.getService(this, 2, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val boostUpIntent = Intent(this, AuraSoundService::class.java).apply { action = ACTION_BOOST_UP }.let {
            PendingIntent.getService(this, 3, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val title = if (isEnabled) {
            "AuraSound DSP: +$boost% (${preset.displayName})"
        } else {
            "AuraSound DSP: En Pausa"
        }

        val subtitle = if (isEnabled) {
            "${device.name} • Cero Distorsión (Limiter Activo)"
        } else {
            "Toca para reactivar el procesador dinámico"
        }

        return NotificationCompat.Builder(this, AuraSoundApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                0,
                if (isEnabled) "Pausar" else "Activar",
                toggleIntent
            )
            .addAction(0, "Preset", presetIntent)
            .addAction(0, "+ Boost", boostUpIntent)
            .build()
    }
}
