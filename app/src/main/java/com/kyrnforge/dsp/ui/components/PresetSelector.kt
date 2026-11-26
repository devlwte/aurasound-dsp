package com.kyrnforge.dsp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyrnforge.dsp.audio.DspPreset
import com.kyrnforge.dsp.audio.DspPresetId
import com.kyrnforge.dsp.ui.theme.BorderSubtle
import com.kyrnforge.dsp.ui.theme.ElectricCyan
import com.kyrnforge.dsp.ui.theme.SurfaceCard
import com.kyrnforge.dsp.ui.theme.SurfaceElevated
import com.kyrnforge.dsp.ui.theme.TextMuted
import com.kyrnforge.dsp.ui.theme.TextPrimary
import com.kyrnforge.dsp.ui.theme.TextSecondary

@Composable
fun PresetSelector(
    selectedPreset: DspPreset,
    onPresetSelect: (DspPreset) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "PERFILES DSP DE ESTUDIO",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = selectedPreset.subtitle,
                fontSize = 11.sp,
                color = ElectricCyan,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DspPreset.ALL_PRESETS.forEach { preset ->
                val isSelected = preset.id == selectedPreset.id
                val icon = when (preset.id) {
                    DspPresetId.STUDIO_REFERENCE -> Icons.Default.Equalizer
                    DspPresetId.BASS_IMPACT -> Icons.Default.SpeakerGroup
                    DspPresetId.PURE_VOICE -> Icons.Default.RecordVoiceOver
                    DspPresetId.PARTY_CLUB -> Icons.Default.Radio
                    DspPresetId.NIGHT_PROTECTION -> Icons.Default.Nightlight
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) SurfaceElevated else SurfaceCard)
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan else BorderSubtle,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = enabled) { onPresetSelect(preset) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) ElectricCyan else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = preset.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
