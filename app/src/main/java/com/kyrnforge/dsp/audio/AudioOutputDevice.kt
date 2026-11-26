package com.kyrnforge.dsp.audio

import android.media.AudioDeviceInfo

enum class DeviceCategory {
    BLUETOOTH_SPEAKER,
    BLUETOOTH_HEADPHONES,
    WIRED_HEADPHONES,
    USB_DAC,
    INTERNAL_SPEAKER,
    UNKNOWN
}

data class AudioOutputDevice(
    val id: Int,
    val name: String,
    val category: DeviceCategory,
    val sampleRate: Int = 48000,
    val channelCount: Int = 2,
    val isWireless: Boolean = false,
    val description: String = ""
) {
    companion object {
        fun defaultSpeaker(): AudioOutputDevice {
            return AudioOutputDevice(
                id = -1,
                name = "Altavoz del Teléfono",
                category = DeviceCategory.INTERNAL_SPEAKER,
                sampleRate = 48000,
                channelCount = 2,
                isWireless = false,
                description = "Filtro acústico de protección activo (corte subsónico <80Hz)"
            )
        }

        fun fromAudioDeviceInfo(info: AudioDeviceInfo, customName: String? = null): AudioOutputDevice {
            val (category, isWireless, desc) = when (info.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> Triple(
                    DeviceCategory.BLUETOOTH_HEADPHONES,
                    true,
                    "Conexión Bluetooth Hi-Fi A2DP (Headroom abierto)"
                )
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> Triple(
                    DeviceCategory.BLUETOOTH_SPEAKER,
                    true,
                    "Altavoz Bluetooth (Perfil de alta dinámica)"
                )
                AudioDeviceInfo.TYPE_BLE_HEADSET -> Triple(
                    DeviceCategory.BLUETOOTH_HEADPHONES,
                    true,
                    "Auriculares BLE de baja latencia"
                )
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> Triple(
                    DeviceCategory.WIRED_HEADPHONES,
                    false,
                    "Jack 3.5mm Analógico (Modo Audiophile & 3D Virtualizer)"
                )
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_USB_DEVICE -> Triple(
                    DeviceCategory.USB_DAC,
                    false,
                    "DAC USB / Auriculares USB-C (Audio Digital Lossless)"
                )
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> Triple(
                    DeviceCategory.INTERNAL_SPEAKER,
                    false,
                    "Altavoz Integrado (Protección contra vibración y chicharreo)"
                )
                else -> Triple(
                    DeviceCategory.UNKNOWN,
                    false,
                    "Salida de audio estándar"
                )
            }

            val displayName = customName?.takeIf { it.isNotBlank() }
                ?: info.productName.toString().takeIf { it.isNotBlank() }
                ?: when (category) {
                    DeviceCategory.BLUETOOTH_SPEAKER -> "Parlante Bluetooth"
                    DeviceCategory.BLUETOOTH_HEADPHONES -> "Auriculares Bluetooth"
                    DeviceCategory.WIRED_HEADPHONES -> "Auriculares con Cable"
                    DeviceCategory.USB_DAC -> "DAC USB-C"
                    DeviceCategory.INTERNAL_SPEAKER -> "Altavoz del Móvil"
                    DeviceCategory.UNKNOWN -> "Dispositivo de Audio"
                }

            val sampleRates = info.sampleRates
            val rate = if (sampleRates.isNotEmpty()) sampleRates[0] else 48000

            return AudioOutputDevice(
                id = info.id,
                name = displayName,
                category = category,
                sampleRate = rate,
                channelCount = 2,
                isWireless = isWireless,
                description = desc
            )
        }
    }
}
