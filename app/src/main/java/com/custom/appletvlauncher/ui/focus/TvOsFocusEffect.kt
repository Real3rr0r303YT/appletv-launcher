package com.custom.appletvlauncher.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive

private const val FOCUSED_SCALE = 1.12f
private const val MAX_TILT_DEGREES = 2.5f
private const val TILT_CYCLE_MS = 1800

private val FocusSpring = spring<Float>(dampingRatio = 0.6f, stiffness = 300f)

/**
 * The tvOS focus "lift": the tile grows, floats above the grid with a deep shadow and gently sways
 * while it holds focus. Everything is applied in a single graphics layer, so animating it only redraws.
 */
@Composable
fun Modifier.tvOsFocusEffect(focused: Boolean, shape: Shape): Modifier {
    val scale by animateFloatAsState(if (focused) FOCUSED_SCALE else 1f, FocusSpring, label = "focusScale")
    val elevation by animateDpAsState(
        targetValue = if (focused) 28.dp else 0.dp,
        animationSpec = spring(stiffness = 300f),
        label = "focusElevation",
    )
    val tilt by rememberFocusTilt(focused)
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationX = tilt.y
        rotationY = tilt.x
        cameraDistance = 14f * density
        shadowElevation = elevation.toPx()
        ambientShadowColor = Color.Black
        spotShadowColor = Color.Black
        this.shape = shape
        clip = true
    }
}

/** Slow idle sway of the focused tile, mimicking the tvOS parallax wiggle. Unfocused tiles run no animation. */
@Composable
private fun rememberFocusTilt(focused: Boolean): State<Offset> {
    val tilt = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    LaunchedEffect(focused) {
        if (!focused) {
            tilt.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessLow))
            return@LaunchedEffect
        }
        val swing = tween<Offset>(TILT_CYCLE_MS, easing = FastOutSlowInEasing)
        while (isActive) {
            tilt.animateTo(Offset(MAX_TILT_DEGREES, -MAX_TILT_DEGREES / 2), swing)
            tilt.animateTo(Offset(-MAX_TILT_DEGREES, MAX_TILT_DEGREES / 2), swing)
        }
    }
    return tilt.asState()
}

/**
 * Glossy overlay for a focused tile: a soft top highlight, a light streak that sweeps across once
 * when focus arrives, and a thin bright rim.
 */
@Composable
fun FocusSheen(focused: Boolean, shape: Shape, modifier: Modifier = Modifier) {
    val highlight by animateFloatAsState(if (focused) 1f else 0f, tween(250), label = "sheenHighlight")
    val sweep = remember { Animatable(-1f) }
    LaunchedEffect(focused) {
        if (focused) {
            sweep.snapTo(-1f)
            sweep.animateTo(2f, tween(1100, easing = FastOutSlowInEasing))
        }
    }
    Spacer(
        modifier
            .drawBehind {
                if (highlight <= 0f) return@drawBehind
                drawRect(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.20f * highlight),
                        0.5f to Color.Transparent,
                    ),
                )
                val center = size.width * sweep.value
                val reach = size.width * 0.6f
                drawRect(
                    Brush.linearGradient(
                        0.40f to Color.Transparent,
                        0.50f to Color.White.copy(alpha = 0.30f * highlight),
                        0.60f to Color.Transparent,
                        start = Offset(center - reach, 0f),
                        end = Offset(center + reach, size.height),
                    ),
                )
            }
            .border(1.5.dp, Color.White.copy(alpha = 0.35f * highlight), shape),
    )
}
