package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DangerRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeedKmh: Double,
    maxSpeedKmh: Double,
    avgSpeedKmh: Double,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeedKmh.toFloat(),
        label = "speedGauge"
    )

    val maxGaugeSpeed = 160f
    val speedRatio = (animatedSpeed / maxGaugeSpeed).coerceIn(0f, 1f)

    val gaugeColor = when {
        animatedSpeed > 110 -> DangerRed
        animatedSpeed > 80 -> AmberGold
        else -> CyanAccent
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(240.dp, 135.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(135.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.width / 2f) - strokeWidth
                val topLeft = Offset(strokeWidth, strokeWidth)
                val arcSize = Size(size.width - strokeWidth * 2, size.width - strokeWidth * 2)

                // Track arc: 180 degrees (from 180 to 360)
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active speed sweep
                drawArc(
                    brush = Brush.sweepGradient(
                        0.5f to CyanAccent,
                        0.75f to AmberGold,
                        1.0f to DangerRed
                    ),
                    startAngle = 180f,
                    sweepAngle = 180f * speedRatio,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Tick marks
                val totalTicks = 8
                for (i in 0..totalTicks) {
                    val angleDeg = 180f + (180f * (i.toFloat() / totalTicks))
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val innerR = radius - 16.dp.toPx()
                    val outerR = radius - 6.dp.toPx()
                    val cx = size.width / 2f
                    val cy = size.height

                    val startX = (cx + innerR * cos(angleRad)).toFloat()
                    val startY = (cy + innerR * sin(angleRad)).toFloat()
                    val endX = (cx + outerR * cos(angleRad)).toFloat()
                    val endY = (cy + outerR * sin(angleRad)).toFloat()

                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Digital readout in the center of the arc
            Column(
                modifier = Modifier.padding(top = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.0f", animatedSpeed),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = gaugeColor
                )
                Text(
                    text = "KM / H",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
            }
        }

        // Sub telemetry row
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "AVG SPEED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${String.format("%.1f", avgSpeedKmh)} km/h",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TOP SPEED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${String.format("%.1f", maxSpeedKmh)} km/h",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold
                )
            }
        }
    }
}
