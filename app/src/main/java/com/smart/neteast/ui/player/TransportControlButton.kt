package com.smart.neteast.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smart.neteast.ui.theme.NcrustSpacing

enum class TransportGlyph {
    Previous, Play, Pause, Next
}

/** Flat Salt-style transport control with a quiet press-scale response and a 48dp hit target. */
@Composable
fun TransportControlButton(
    glyph: TransportGlyph,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    touchSize: Dp = NcrustSpacing.touchTarget,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "transportPressScale",
    )

    Box(
        modifier = modifier
            .size(touchSize.coerceAtLeast(NcrustSpacing.touchTarget))
            .scale(scale)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(iconSize)) {
            drawTransportGlyph(glyph, tint)
        }
    }
}

private fun DrawScope.drawTransportGlyph(glyph: TransportGlyph, color: Color) {
    val w = size.width
    val h = size.height
    val barRadius = CornerRadius(w * 0.035f, w * 0.035f)
    when (glyph) {
        TransportGlyph.Play -> drawPath(
            path = Path().apply {
                moveTo(w * 0.24f, h * 0.12f)
                lineTo(w * 0.84f, h * 0.50f)
                lineTo(w * 0.24f, h * 0.88f)
                close()
            },
            color = color,
        )
        TransportGlyph.Pause -> {
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.12f),
                size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.76f),
                cornerRadius = barRadius,
            )
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.12f),
                size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.76f),
                cornerRadius = barRadius,
            )
        }
        TransportGlyph.Previous -> {
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.16f),
                size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.68f),
                cornerRadius = barRadius,
            )
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.82f, h * 0.12f)
                    lineTo(w * 0.32f, h * 0.50f)
                    lineTo(w * 0.82f, h * 0.88f)
                    close()
                },
                color = color,
            )
        }
        TransportGlyph.Next -> {
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.18f, h * 0.12f)
                    lineTo(w * 0.68f, h * 0.50f)
                    lineTo(w * 0.18f, h * 0.88f)
                    close()
                },
                color = color,
            )
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.16f),
                size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.68f),
                cornerRadius = barRadius,
            )
        }
    }
}
