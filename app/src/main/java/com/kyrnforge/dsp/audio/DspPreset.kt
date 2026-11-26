package com.kyrnforge.dsp.audio

enum class DspPresetId {
    STUDIO_REFERENCE,
    BASS_IMPACT,
    PURE_VOICE,
    PARTY_CLUB,
    NIGHT_PROTECTION
}

data class DspPreset(
    val id: DspPresetId,
    val displayName: String,
    val subtitle: String,
    val iconName: String,
    val targetGainMb: Int,          // Millibels (e.g. 350 mB = +3.5 dB)
    val limiterThresholdDb: Float,   // dBFS (-1.5 dBFS prevents digital clipping)
    val limiterAttackMs: Float,     // Fast attack (1.0 ms)
    val limiterReleaseMs: Float,    // Smooth release (50.0 ms)
    val bassBoostStrength: Short,    // 0 to 1000
    val virtualizerStrength: Short,  // 0 to 1000
    val vocalClarityGainDb: Float,  // Boost in 1.5kHz - 3.5kHz band
    val dynamicRangeRatio: Float    // Compressor ratio (e.g. 4.0 = 4:1)
) {
    companion object {
        val ALL_PRESETS = listOf(
            DspPreset(
                id = DspPresetId.STUDIO_REFERENCE,
                displayName = "Studio Reference",
                subtitle = "Fidelidad pura y balance tonal plano",
                iconName = "studio",
                targetGainMb = 350,
                limiterThresholdDb = -1.5f,
                limiterAttackMs = 1.0f,
                limiterReleaseMs = 50.0f,
                bassBoostStrength = 200,
                virtualizerStrength = 150,
                vocalClarityGainDb = 1.0f,
                dynamicRangeRatio = 3.0f
            ),
            DspPreset(
                id = DspPresetId.BASS_IMPACT,
                displayName = "Bass Impact",
                subtitle = "Graves profundos sin saturar transitorios",
                iconName = "bass",
                targetGainMb = 450,
                limiterThresholdDb = -1.8f,
                limiterAttackMs = 1.5f,
                limiterReleaseMs = 80.0f,
                bassBoostStrength = 650,
                virtualizerStrength = 100,
                vocalClarityGainDb = 0.5f,
                dynamicRangeRatio = 4.5f
            ),
            DspPreset(
                id = DspPresetId.PURE_VOICE,
                displayName = "Pure Voice",
                subtitle = "Inteligibilidad de diálogos y podcasts",
                iconName = "voice",
                targetGainMb = 400,
                limiterThresholdDb = -1.2f,
                limiterAttackMs = 1.0f,
                limiterReleaseMs = 45.0f,
                bassBoostStrength = 80,
                virtualizerStrength = 50,
                vocalClarityGainDb = 4.5f,
                dynamicRangeRatio = 3.5f
            ),
            DspPreset(
                id = DspPresetId.PARTY_CLUB,
                displayName = "Party / Club",
                subtitle = "Máximo volumen RMS con compresor brickwall",
                iconName = "party",
                targetGainMb = 750,
                limiterThresholdDb = -0.8f,
                limiterAttackMs = 0.8f,
                limiterReleaseMs = 35.0f,
                bassBoostStrength = 500,
                virtualizerStrength = 300,
                vocalClarityGainDb = 2.0f,
                dynamicRangeRatio = 6.0f
            ),
            DspPreset(
                id = DspPresetId.NIGHT_PROTECTION,
                displayName = "Night Protection",
                subtitle = "Control dinámico contra picos sorpresivos",
                iconName = "night",
                targetGainMb = 200,
                limiterThresholdDb = -3.0f,
                limiterAttackMs = 0.5f,
                limiterReleaseMs = 100.0f,
                bassBoostStrength = 100,
                virtualizerStrength = 0,
                vocalClarityGainDb = 2.0f,
                dynamicRangeRatio = 8.0f
            )
        )
    }
}
