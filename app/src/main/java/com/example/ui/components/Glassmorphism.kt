package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier that creates a realistic glassmorphism (liquid glass) panel.
 * Gives translucent reflection, dual-border gloss gradient and soft custom shadow glow.
 */
fun Modifier.glassCard(
    isDark: Boolean,
    cornerRadius: Dp = 24.dp,
    opacity: Float = 0.15f,
    borderWidth: Dp = 1.5.dp
): Modifier = this
    .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(cornerRadius),
        clip = false,
        ambientColor = Color(0xFFCA72FF).copy(alpha = 0.08f),
        spotColor = Color(0xFFCA72FF).copy(alpha = 0.25f)
    )
    .background(
        brush = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF23173D).copy(alpha = opacity + 0.12f),
                    Color(0xFF130A24).copy(alpha = opacity - 0.05f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.82f),
                    Color(0xFFFAF8FF).copy(alpha = 0.52f)
                )
            )
        },
        shape = RoundedCornerShape(cornerRadius)
    )
    .clip(RoundedCornerShape(cornerRadius))
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = if (isDark) {
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color(0xFFCA72FF).copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.05f)
                )
            } else {
                listOf(
                    Color(0xFFCA72FF).copy(alpha = 0.50f),
                    Color.White.copy(alpha = 0.85f),
                    Color(0xFFFFD84D).copy(alpha = 0.40f)
                )
            }
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Renders an animated backplane of liquid neon purple and accent yellow circles
 * under a subtle glass overlay to simulate "Liquid Glass" depth.
 */
@Composable
fun LiquidBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Dynamic looping transitions for natural fluid motion without CPU blockages
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidBG")

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )

    val color1 = Color(0xFFCA72FF).copy(alpha = if (isDark) 0.35f else 0.22f)
    val color2 = Color(0xFFFFD84D).copy(alpha = if (isDark) 0.20f else 0.15f)
    val color3 = Color(0xFFE2AFFF).copy(alpha = if (isDark) 0.15f else 0.12f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0C0714) else Color(0xFFFAF8FF))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width > 0f && height > 0f) {
                val r1 = (width * 0.5f * scale1).coerceAtLeast(1f)
                val r2 = (width * 0.45f * scale2).coerceAtLeast(1f)
                val r3 = (width * 0.4f * ((scale1 + scale2) / 2)).coerceAtLeast(1f)

                // Neon Purple blob top-left-center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color1, Color.Transparent),
                        center = Offset(width * 0.25f, height * 0.3f),
                        radius = r1
                    ),
                    radius = r1,
                    center = Offset(width * 0.25f, height * 0.3f)
                )

                // Yellow Accent blob center-bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color2, Color.Transparent),
                        center = Offset(width * 0.75f, height * 0.75f),
                        radius = r2
                    ),
                    radius = r2,
                    center = Offset(width * 0.75f, height * 0.75f)
                )

                // Neon Pinkish Lavender secondary blob middle-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color3, Color.Transparent),
                        center = Offset(width * 0.1f, height * 0.8f),
                        radius = r3
                    ),
                    radius = r3,
                    center = Offset(width * 0.1f, height * 0.8f)
                )
            }
        }

        // Slight screen-wide vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color.Transparent, Color(0xFF0C0714).copy(alpha = 0.5f))
                        } else {
                            listOf(Color.Transparent, Color(0xFFF1EEF8).copy(alpha = 0.4f))
                        }
                    )
                )
        )

        content()
    }
}
