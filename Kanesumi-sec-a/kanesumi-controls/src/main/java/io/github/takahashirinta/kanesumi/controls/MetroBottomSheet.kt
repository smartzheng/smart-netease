package io.github.takahashirinta.kanesumi.controls

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.anim.sokuou.SokuouTweens
import io.github.takahashirinta.kanesumi.core.insets.metroNavigationBarsPadding
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import kotlinx.coroutines.launch

/**
 * Metro 风底部 sheet。直角、无圆角、无 elevation -- 取代 M3 ModalBottomSheet
 * 的圆角 + drag handle 丸 + tonal scrim 渐变。
 *
 * 动画模型:单一 `progress: Animatable<Float>` (0=藏,1=显) 驱动全部视觉,
 * 严格零重组:
 * - 入场 0->1 走 SokuouTweens.SheetAppear (300ms CubicBezier(0.2,0,0,1));
 * - 收起 1->0 走 SokuouTweens.SheetDismiss (260ms FastOutSlowIn);
 * - scrim alpha = progress * scrimAlpha (默认 0.6),点击 scrim 收起;
 * - sheet 主体 graphicsLayer { translationY = size.height * (1 - progress) }
 *   从屏幕下方滑入。
 *
 * dragHandle slot 是唯一挂 detectVerticalDragGestures 的区域 -- 调用方填
 * 视觉(标题行 / 封面信息行 / grabber),库负责手势:
 * - onVerticalDrag: progress.snapTo((progress - drag/sheetHeight).coerceIn(0,1));
 * - onDragEnd: progress < dismissThreshold (默认 0.6) -> 收起,否则弹回 1。
 *
 * content slot 在 dragHandle 下方,通常是动作列表。系统导航栏 inset 不代管
 * -- 调用方在 content 末尾加 Spacer(Modifier.metroNavigationBarsPadding())
 * 或给 content 包一层 padding,与 Ncrust SongMenuSheet 现有用法一致。
 */
@Composable
fun MetroBottomSheet(
    onDismiss: () -> Unit,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.6f,
    sheetColor: Color = LocalMetroColors.current.surface,
    dismissThreshold: Float = 0.6f,
    applyNavigationBarsPadding: Boolean = false,
    topCornerRadiusDp: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val progress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var sheetHeightPx by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, SokuouTweens.SheetAppear)
    }

    fun animateDismiss() {
        coroutineScope.launch {
            progress.animateTo(0f, SokuouTweens.SheetDismiss)
            onDismiss()
        }
    }

    val sheetShape = RoundedCornerShape(topStart = topCornerRadiusDp, topEnd = topCornerRadiusDp)
    val baseSheetModifier = Modifier
        .fillMaxWidth()
        .onSizeChanged { sheetHeightPx = it.height.toFloat() }
        .graphicsLayer { translationY = size.height * (1f - progress.value) }
        .then(if (topCornerRadiusDp > 0.dp) Modifier.clip(sheetShape) else Modifier)
        .background(sheetColor)
        .then(if (applyNavigationBarsPadding) Modifier.metroNavigationBarsPadding() else Modifier)

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value * scrimAlpha }
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { animateDismiss() },
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(baseSheetModifier),
        ) {
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (progress.value < dismissThreshold) {
                                    progress.animateTo(0f, SokuouTweens.SheetDismiss)
                                    onDismiss()
                                } else {
                                    progress.animateTo(1f, SokuouTweens.SheetAppear)
                                }
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            if (sheetHeightPx > 0f) {
                                coroutineScope.launch {
                                    progress.snapTo(
                                        (progress.value - dragAmount / sheetHeightPx).coerceIn(0f, 1f),
                                    )
                                }
                            }
                        },
                    )
                },
            ) {
                dragHandle()
            }
            content()
        }
    }
}
