package com.kyrnforge.dsp.audio

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioDeviceDetector(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _currentDevice = MutableStateFlow(AudioOutputDevice.defaultSpeaker())
    val currentDevice: StateFlow<AudioOutputDevice> = _currentDevice.asStateFlow()

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            refreshAudioDevice()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            refreshAudioDevice()
        }
    }

    private val legacyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mainHandler.postDelayed({ refreshAudioDevice() }, 300)
        }
    }

    private var isListening = false

    fun startListening() {
        if (isListening) return
        isListening = true

        audioManager.registerAudioDeviceCallback(audioDeviceCallback, mainHandler)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(legacyReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(legacyReceiver, filter)
            }
        } catch (_: Exception) {}

        refreshAudioDevice()
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        try {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (_: Exception) {}
        try {
            context.unregisterReceiver(legacyReceiver)
        } catch (_: Exception) {}
    }

    fun refreshAudioDevice() {
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        
        // Priority order: Bluetooth > Wired / USB > Built-in Speaker
        val bestDevice = outputs.firstOrNull { info ->
            info.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            info.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
            info.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
            info.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        } ?: outputs.firstOrNull { info ->
            info.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            info.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            info.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
            info.type == AudioDeviceInfo.TYPE_USB_DEVICE
        } ?: outputs.firstOrNull { info ->
            info.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        }

        if (bestDevice != null) {
            val friendlyName = resolveBluetoothDeviceName(bestDevice)
            _currentDevice.value = AudioOutputDevice.fromAudioDeviceInfo(bestDevice, friendlyName)
        } else {
            _currentDevice.value = AudioOutputDevice.defaultSpeaker()
        }
    }

    private fun resolveBluetoothDeviceName(info: AudioDeviceInfo): String? {
        if (info.type != AudioDeviceInfo.TYPE_BLUETOOTH_A2DP &&
            info.type != AudioDeviceInfo.TYPE_BLE_HEADSET &&
            info.type != AudioDeviceInfo.TYPE_BLE_SPEAKER) {
            return null
        }

        try {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bm?.adapter ?: return null
            if (adapter.isEnabled) {
                val bonded = adapter.bondedDevices
                bonded?.firstOrNull { it.name?.isNotBlank() == true }?.let {
                    return it.name
                }
            }
        } catch (_: SecurityException) {
            // Handled when BLUETOOTH_CONNECT permission is missing
        } catch (_: Exception) {}

        return null
    }
}
