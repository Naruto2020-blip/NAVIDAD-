package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Snowflake(
    var xNorm: Float,
    var yNorm: Float,
    val radiusDp: Float,
    val speedNormPerSec: Float,
    val swayAmplitudeNorm: Float,
    val swayFrequency: Float,
    val phaseOffset: Float,
    val alpha: Float,
)

private data class TwinkleStar(
    val xNorm: Float,
    val yNorm: Float,
    val baseRadiusDp: Float,
    val speed: Float,
    val phase: Float,
)

@Composable
fun SnowfallOverlay(
    enabled: Boolean,
    darkAmbiance: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return

    val flakes = remember {
        val rng = Random(20261225)
        List(58) {
            Snowflake(
                xNorm = rng.nextFloat(),
                yNorm = rng.nextFloat(),
                radiusDp = 1.6f + rng.nextFloat() * 3.4f,
                speedNormPerSec = 0.035f + rng.nextFloat() * 0.065f,
                swayAmplitudeNorm = 0.008f + rng.nextFloat() * 0.018f,
                swayFrequency = 0.6f + rng.nextFloat() * 1.4f,
                phaseOffset = rng.nextFloat() * (2f * PI.toFloat()),
                alpha = 0.28f + rng.nextFloat() * 0.58f,
            )
        }
    }

    val stars = remember {
        val rng = Random(1225)
        List(18) {
            TwinkleStar(
                xNorm = rng.nextFloat(),
                yNorm = rng.nextFloat() * 0.42f,
                baseRadiusDp = 1.2f + rng.nextFloat() * 1.8f,
                speed = 1.2f + rng.nextFloat() * 2.2f,
                phase = rng.nextFloat() * (2f * PI.toFloat()),
            )
        }
    }

    var frameTimeNanos by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(enabled) {
        var lastNanos = 0L
        var accumSec = 0.0
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000.0).coerceIn(0.0, 0.05)
                    accumSec += dt
                    for (flake in flakes) {
                        flake.yNorm += (flake.speedNormPerSec * dt).toFloat()
                        if (flake.yNorm > 1.04f) {
                            flake.yNorm = -0.04f
                        }
                    }
                    elapsedSeconds = (accumSec * 1000.0).toLong()
                }
                lastNanos = now
                frameTimeNanos = now
            }
        }
    }

    val flakeTint = if (darkAmbiance) Color.White else MaterialTheme.colorScheme.secondary
    val starTint = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        // Trigger redraw from frameTimeNanos
        val tSec = (frameTimeNanos % 1_000_000_000_000L) / 1_000_000_000f
        val animSec = elapsedSeconds / 1000f

        if (darkAmbiance) {
            for (star in stars) {
                val pulse = 0.35f + 0.65f * ((sin(animSec * star.speed + star.phase) + 1f) * 0.5f)
                val cx = star.xNorm * size.width
                val cy = star.yNorm * size.height
                drawCircle(
                    color = starTint.copy(alpha = 0.45f * pulse),
                    radius = star.baseRadiusDp.dp.toPx() * (0.8f + 0.4f * pulse),
                    center = Offset(cx, cy),
                )
            }
        }

        for (flake in flakes) {
            val sway = sin(animSec * flake.swayFrequency + flake.phaseOffset) * flake.swayAmplitudeNorm
            val cx = ((flake.xNorm + sway).coerceIn(0f, 1f)) * size.width
            val cy = flake.yNorm * size.height
            val alphaFactor = if (darkAmbiance) flake.alpha else flake.alpha * 0.32f
            drawCircle(
                color = flakeTint.copy(alpha = alphaFactor),
                radius = flake.radiusDp.dp.toPx(),
                center = Offset(cx, cy),
            )
        }
        // Keep tSec referenced cleanly
        if (tSec < -1f) {
            drawCircle(Color.Transparent, 0f)
        }
    }
}
