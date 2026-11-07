package io.github.takahashirinta.kanesumi.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors

/**
 * Metro 风加载指示器。细线圆环 (3dp stroke),缺 90 度开口,线性旋转 1s/圈。
 *
 * rotation 在 graphicsLayer lambda 内读取 (deferred read),零重组 -- 每帧
 * 只 invalidateDraw,不触发组合。默认 36dp,调用方可通过 modifier.size()
 * 覆盖。色 = LocalMetroColors.primary。
 *
 * 取代 M3 CircularProgressIndicator (默认带 indeterminate 动画 + 较粗 stroke
 * + 圆角端帽这里保留是因为细线圆角端在旋转时视觉更顺,与 Metro 不冲突)。
 */
@Composable
fun MetroProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalMetroColors.current.primary,
    sizeDp: Dp = 36.dp,
    strokeDp: Dp = 3.dp,
) {
    val transition = rememberInfiniteTransition(label = "metroProgress")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    Canvas(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer { rotationZ = rotation.value },
    ) {
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeDp.toPx(), cap = StrokeCap.Round),
        )
    }
}
