package com.kyrnforge.dsp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyrnforge.dsp.audio.FrequencyBands
import com.kyrnforge.dsp.ui.theme.BorderSubtle
import com.kyrnforge.dsp.ui.theme.CyberAmber
import com.kyrnforge.dsp.ui.theme.ElectricCyan
import com.kyrnforge.dsp.ui.theme.LimiterRed
import com.kyrnforge.dsp.ui.theme.NeonBlue
import com.kyrnforge.dsp.ui.theme.SurfaceCard
import com.kyrnforge.dsp.ui.theme.TextMuted
import com.kyrnforge.dsp.ui.theme.TextSecondary

@Composable
fun SpectrumVisualizer(
    frequencyBands: FrequencyBands,
    modifier: Modifier = Modifier,
    height: Dp = 130.dp,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME FFT SPECTRUM",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (enabled) "LIVE 48 kHz" else "OFFLINE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) ElectricCyan else TextMuted
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                val totalWidth = size.width
                val totalHeight = size.height
                val barCount = FrequencyBands.BAND_COUNT
                val gap = 6.dp.toPx()
                val totalGaps = gap * (barCount - 1)
                val barWidth = (totalWidth - totalGaps) / barCount

                for (g in 1..3) {
                    val y = totalHeight * (g / 4f)
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(totalWidth, y),
                        strokeWidth = 1f
                    )
                }

                for (i in 0 until barCount) {
                    val x = i * (barWidth + gap)
                    val rawMagnitude = if (enabled) frequencyBands.bars[i] else 0.05f
                    val barHeight = (rawMagnitude * totalHeight).coerceIn(4.dp.toPx(), totalHeight)
                    val barTop = totalHeight - barHeight

                    val barBrush = Brush.verticalGradient(
                        colors = listOf(
                            if (rawMagnitude > 0.85f) LimiterRed else if (rawMagnitude > 0.65f) CyberAmber else NeonBlue,
                            ElectricCyan
                        ),
                        startY = barTop,
                        endY = totalHeight
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    val peakMagnitude = if (enabled) frequencyBands.peakDots[i] else 0.05f
                    val peakTop = (totalHeight - (peakMagnitude * totalHeight)).coerceIn(0f, totalHeight - 3.dp.toPx())
                    val peakColor = if (peakMagnitude > 0.85f) LimiterRed else CyberAmber

                    drawRoundRect(
                        color = peakColor,
                        topLeft = Offset(x, peakTop),
                        size = Size(barWidth, 2.5.dp.toPx()),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FrequencyBands.BAND_LABELS.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
