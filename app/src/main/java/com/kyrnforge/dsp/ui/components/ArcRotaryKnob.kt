package com.kyrnforge.dsp.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyrnforge.dsp.ui.theme.CyberAmber
import com.kyrnforge.dsp.ui.theme.ElectricCyan
import com.kyrnforge.dsp.ui.theme.KnobTrack
import com.kyrnforge.dsp.ui.theme.LimiterRed
import com.kyrnforge.dsp.ui.theme.NeonBlue
import com.kyrnforge.dsp.ui.theme.TextMuted
import com.kyrnforge.dsp.ui.theme.TextPrimary
import com.kyrnforge.dsp.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ArcRotaryKnob(
    valuePercent: Int,
    gainDb: Float,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    enabled: Boolean = true
) {
    val view = LocalView.current
    var lastHapticMilestone by remember { mutableFloatStateOf(valuePercent / 10f) }

    val startAngle = 135f
    val sweepMaxAngle = 270f
    val fraction = (valuePercent / 100f).coerceIn(0f, 1f)
    val currentSweep = fraction * sweepMaxAngle

    val activeColor = when {
        !enabled -> TextMuted
        valuePercent > 80 -> CyberAmber
        valuePercent > 95 -> LimiterRed
        else -> ElectricCyan
    }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures { change, _ ->
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val touchVector = change.position - center
                    var angleRad = atan2(touchVector.y.toDouble(), touchVector.x.toDouble())
                    var angleDeg = Math.toDegrees(angleRad).toFloat()
                    if (angleDeg < 0) angleDeg += 360f

                    var normalizedAngle = angleDeg - 135f
                    if (normalizedAngle < 0) normalizedAngle += 360f

                    if (normalizedAngle <= sweepMaxAngle) {
                        val newFraction = (normalizedAngle / sweepMaxAngle).coerceIn(0f, 1f)
                        val newPercent = (newFraction * 100).roundToInt()
                        onValueChange(newPercent)

                        val currentMilestone = (newPercent / 10f)
                        if (currentMilestone.toInt() != lastHapticMilestone.toInt()) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            lastHapticMilestone = currentMilestone
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val radius = (this.size.minDimension - strokeWidth * 2) / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)

            drawArc(
                color = KnobTrack,
                startAngle = startAngle,
                sweepAngle = sweepMaxAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (currentSweep > 1f && enabled) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to ElectricCyan,
                        0.7f to NeonBlue,
                        0.85f to CyberAmber,
                        1.0f to LimiterRed
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            val tickRadius = radius + 18.dp.toPx()
            val tickCount = 28
            for (i in 0..tickCount) {
                val tickFraction = i.toFloat() / tickCount
                val tickAngleDeg = startAngle + tickFraction * sweepMaxAngle
                val tickRad = Math.toRadians(tickAngleDeg.toDouble())

                val p1 = Offset(
                    center.x + (tickRadius - 5.dp.toPx()) * cos(tickRad).toFloat(),
                    center.y + (tickRadius - 5.dp.toPx()) * sin(tickRad).toFloat()
                )
                val p2 = Offset(
                    center.x + tickRadius * cos(tickRad).toFloat(),
                    center.y + tickRadius * sin(tickRad).toFloat()
                )

                val isLit = enabled && tickFraction <= fraction
                drawLine(
                    color = if (isLit) activeColor.copy(alpha = 0.85f) else TextMuted.copy(alpha = 0.3f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 7 == 0) 2.5.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val innerDiscRadius = radius - 16.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1C2436), Color(0xFF101522)),
                    center = center,
                    radius = innerDiscRadius
                ),
                radius = innerDiscRadius,
                center = center
            )
            drawCircle(
                color = if (enabled) activeColor.copy(alpha = 0.35f) else Color.Transparent,
                radius = innerDiscRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BOOST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (enabled) "+$valuePercent%" else "OFF",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = if (enabled) TextPrimary else TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (enabled) String.format("+%.1f dB", gainDb) else "BYPASS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = activeColor
            )
        }
    }
}
