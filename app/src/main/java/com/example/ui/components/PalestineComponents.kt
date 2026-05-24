package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun PalestineFlag(
    modifier: Modifier = Modifier,
    amplitude: Float = 12f,
    frequency: Float = 0.05f,
    waveSpeedMs: Int = 1800
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flagWave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(waveSpeedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Draw vertical lines to simulate a woven waving fabric with step performance optimization
        val step = 4
        val strokeW = 4.5f
        for (x in 0 until w.toInt() step step) {
            val progress = x.toFloat() / w
            // Wavy displacement: 0 at flagpole (left) and maximum at the fly (right)
            val displacement = sin(x * frequency - phase) * amplitude * progress

            // Stripe Heights
            val stripeHeight = h / 3f

            // 1. Black Stripe (Top)
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(x.toFloat(), displacement),
                end = androidx.compose.ui.geometry.Offset(x.toFloat(), displacement + stripeHeight),
                strokeWidth = strokeW
            )

            // 2. White Stripe (Middle)
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(x.toFloat(), displacement + stripeHeight),
                end = androidx.compose.ui.geometry.Offset(x.toFloat(), displacement + 2 * stripeHeight),
                strokeWidth = strokeW
            )

            // 3. Green Stripe (Bottom)
            drawLine(
                color = Color(0xFF009639), // Palestine Green
                start = androidx.compose.ui.geometry.Offset(x.toFloat(), displacement + 2 * stripeHeight),
                end = androidx.compose.ui.geometry.Offset(x.toFloat(), displacement + h),
                strokeWidth = strokeW
            )
        }

        // 4. Red Triangle (Hoist)
        // Adjust the triangle peak along the sine wave displacement to keep it aligned with the fabric
        val triangleWidth = w * 0.33f
        val topY = 0f
        val bottomY = h
        val middleY = h / 2f

        val displacementZero = 0f
        val displacementPeak = sin(triangleWidth * frequency - phase) * amplitude * (triangleWidth / w)

        val path = Path().apply {
            moveTo(0f, displacementZero)
            lineTo(triangleWidth, middleY + displacementPeak)
            lineTo(0f, bottomY + displacementZero)
            close()
        }
        drawPath(path, color = Color(0xFFE4312B)) // Palestine Red
    }
}

@Composable
fun PalestinianFlagBanner(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PalestineFlag(
            modifier = Modifier
                .width(100.dp)
                .height(60.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "فلسطين حرة",
            color = Color(0xFFE4312B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
