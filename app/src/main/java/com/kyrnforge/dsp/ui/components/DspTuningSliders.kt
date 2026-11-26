package com.kyrnforge.dsp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyrnforge.dsp.ui.theme.BorderSubtle
import com.kyrnforge.dsp.ui.theme.CyberAmber
import com.kyrnforge.dsp.ui.theme.ElectricCyan
import com.kyrnforge.dsp.ui.theme.NeonBlue
import com.kyrnforge.dsp.ui.theme.SurfaceCard
import com.kyrnforge.dsp.ui.theme.SurfaceElevated
import com.kyrnforge.dsp.ui.theme.TextMuted
import com.kyrnforge.dsp.ui.theme.TextPrimary
import com.kyrnforge.dsp.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun DspTuningSliders(
    antiDistortionActive: Boolean,
    onAntiDistortionChange: (Boolean) -> Unit,
    vocalClarityPercent: Int,
    onVocalClarityChange: (Int) -> Unit,
    bassResonancePercent: Int,
    onBassResonanceChange: (Int) -> Unit,
    spatialPercent: Int,
    onSpatialChange: (Int) -> Unit,
    isSpeaker: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (antiDistortionActive) SurfaceElevated else SurfaceCard)
                .border(
                    1.dp,
                    if (antiDistortionActive) ElectricCyan.copy(alpha = 0.5f) else BorderSubtle,
                    RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (antiDistortionActive) ElectricCyan.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (antiDistortionActive) ElectricCyan else BorderSubtle,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (antiDistortionActive) ElectricCyan else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "ANTI-DISTORSIÓN DRC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (antiDistortionActive) "Limitador de picos activo (-1.5 dBFS)" else "Protección bypass",
                        fontSize = 11.sp,
                        color = if (antiDistortionActive) ElectricCyan else TextSecondary
                    )
                }

                Switch(
                    checked = antiDistortionActive,
                    onCheckedChange = onAntiDistortionChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElectricCyan,
                        checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceCard
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StudioSliderRow(
                    icon = Icons.Default.RecordVoiceOver,
                    label = "PureVoice™ Claridad",
                    description = "Realza frecuencias vocales (1.5k - 3.5k)",
                    valuePercent = vocalClarityPercent,
                    onValueChange = onVocalClarityChange,
                    accentColor = ElectricCyan,
                    enabled = enabled
                )

                StudioSliderRow(
                    icon = Icons.Default.GraphicEq,
                    label = "Resonancia de Graves",
                    description = if (isSpeaker) "Limitado a 35% por protección del altavoz" else "Extensión pura de subgraves",
                    valuePercent = bassResonancePercent,
                    onValueChange = onBassResonanceChange,
                    accentColor = if (isSpeaker) CyberAmber else NeonBlue,
                    enabled = enabled
                )

                StudioSliderRow(
                    icon = Icons.Default.SurroundSound,
                    label = "Espacializador 3D",
                    description = if (isSpeaker) "No disponible en altavoz interno mono" else "Amplitud de campo estéreo inmersivo",
                    valuePercent = spatialPercent,
                    onValueChange = onSpatialChange,
                    accentColor = NeonBlue,
                    enabled = enabled && !isSpeaker
                )
            }
        }
    }
}

@Composable
private fun StudioSliderRow(
    icon: ImageVector,
    label: String,
    description: String,
    valuePercent: Int,
    onValueChange: (Int) -> Unit,
    accentColor: Color,
    enabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) accentColor else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) TextPrimary else TextMuted
                )
            }

            Text(
                text = "$valuePercent%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (enabled) accentColor else TextMuted
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = description,
            fontSize = 10.sp,
            color = TextMuted
        )

        Slider(
            value = valuePercent.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 0f..100f,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = BorderSubtle
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
