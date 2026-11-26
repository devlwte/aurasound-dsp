package com.kyrnforge.dsp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyrnforge.dsp.audio.AudioOutputDevice
import com.kyrnforge.dsp.audio.DeviceCategory
import com.kyrnforge.dsp.ui.theme.BorderSubtle
import com.kyrnforge.dsp.ui.theme.CyberAmber
import com.kyrnforge.dsp.ui.theme.ElectricCyan
import com.kyrnforge.dsp.ui.theme.NeonBlue
import com.kyrnforge.dsp.ui.theme.SurfaceCard
import com.kyrnforge.dsp.ui.theme.TextMuted
import com.kyrnforge.dsp.ui.theme.TextPrimary
import com.kyrnforge.dsp.ui.theme.TextSecondary

@Composable
fun DeviceStatusCard(
    device: AudioOutputDevice,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (icon, badgeColor, typeLabel) = when (device.category) {
        DeviceCategory.BLUETOOTH_SPEAKER,
        DeviceCategory.BLUETOOTH_HEADPHONES -> Triple(
            Icons.Default.Bluetooth,
            ElectricCyan,
            "BLUETOOTH A2DP"
        )
        DeviceCategory.WIRED_HEADPHONES -> Triple(
            Icons.Default.Headphones,
            NeonBlue,
            "AURICULARES CABLEADOS"
        )
        DeviceCategory.USB_DAC -> Triple(
            Icons.Default.Usb,
            ElectricCyan,
            "USB-C DAC LOSSLESS"
        )
        DeviceCategory.INTERNAL_SPEAKER -> Triple(
            Icons.Default.Speaker,
            CyberAmber,
            "ALTAVOZ INTEGRADO"
        )
        DeviceCategory.UNKNOWN -> Triple(
            Icons.Default.Speaker,
            TextMuted,
            "SALIDA ESTÁNDAR"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.12f))
                    .border(1.5.dp, badgeColor.copy(alpha = pulseAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = typeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = badgeColor,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = device.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = device.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Escanear salida de audio",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
