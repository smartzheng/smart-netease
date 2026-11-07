package io.github.takahashirinta.kanesumi.controls

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.takahashirinta.kanesumi.anim.sokuou.MetroCubic
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroText

/**
 * Metro 风下拉菜单。包一层 androidx.compose.ui.window.Popup (foundation,
 * 非 M3),直角矩形 surfaceVariant 容器,无圆角无 elevation。
 *
 * 定位:通过 [alignment] 控制菜单相对锚点 Box 的位置,默认 BottomStart --
 * 即锚点 Box 左下角下方。若锚点 Box 是整宽 Row(常见的"标签靠左 + 值/箭头
 * 靠右"设置行),BottomStart 会让菜单出现在屏幕左边而不是箭头正下方 --
 * 这种场景传 [Alignment.BottomEnd] 让菜单右对齐箭头。上方展开(锚点位于
 * 屏幕下半)用 TopStart / TopEnd。
 *
 * focusable = true 让 Popup 抢焦点,触屏在菜单外点击 / 返回键都会触发
 * onDismissRequest。这是 M3 DropdownMenu 默认行为的核心。
 *
 * 不做 M3 那套"到边缘就自动翻转"的自适应定位 -- 那需要 LayoutCoordinates
 * 回调 + 尺寸测量,复杂度高。让调用方按锚点在屏幕上的位置显式选 alignment,
 * 更可预测。
 *
 * 展开动画:180ms alpha 淡入 + scaleY 0.92 -> 1,transformOrigin 由 alignment
 * 的垂直边推 -- 下开菜单从顶边展开 (origin.y = 0),上开菜单从底边展开
 * (origin.y = 1)。dismiss 保持瞬时:选中项 / 点外面这类"确认关闭"操作,
 * 瞬时反馈比淡出更符合预期。
 *
 * 取代 M3 DropdownMenu (默认圆角 + tonal elevation + ripple menu item)。
 */
@Composable
fun MetroDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalMetroColors.current.surfaceVariant,
    maxWidthDp: Dp = 220.dp,
    alignment: Alignment = Alignment.BottomStart,
    cornerRadiusDp: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    val density = LocalDensity.current
    val appearProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appearProgress.animateTo(1f, tween(durationMillis = 180, easing = MetroCubic))
    }
    // 下开 (BottomStart/BottomEnd):transformOrigin.y = 0,菜单从顶边(挨着锚点)展开。
    // 上开 (TopStart/TopEnd):transformOrigin.y = 1,菜单从底边展开。默认按下开处理。
    val transformOriginY = if (alignment == Alignment.TopStart || alignment == Alignment.TopEnd) 1f else 0f
    val shape = RoundedCornerShape(cornerRadiusDp)
    Popup(
        alignment = alignment,
        offset = IntOffset(0, with(density) { 8.dp.roundToPx() }),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = modifier
                .widthIn(max = maxWidthDp)
                .graphicsLayer {
                    alpha = appearProgress.value
                    scaleY = 0.92f + 0.08f * appearProgress.value
                    transformOrigin = TransformOrigin(0.5f, transformOriginY)
                }
                .then(if (cornerRadiusDp > 0.dp) Modifier.clip(shape) else Modifier)
                .background(containerColor)
                .then(
                    if (cornerRadiusDp > 0.dp) Modifier
                        .border(1.dp, LocalMetroColors.current.divider.copy(alpha = 0.55f), shape)
                        .padding(vertical = 6.dp)
                    else Modifier
                ),
        ) {
            content()
        }
    }
}

/**
 * Metro 风菜单项。直角、无 ripple 槽位(点击走 LocalIndication ->
 * MetroIndication 直角闪切),默认水平 16dp / 垂直 12dp padding。
 *
 * leading / trailing 是 slot,与 MetroListRow 同构 -- 想要图标 + 文字
 * 菜单项就传 leading,想要快捷键标签就传 trailing。
 */
@Composable
fun MetroDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = LocalMetroColors.current.onSurface,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
) {
    val base = modifier.fillMaxWidth()
    val withClick = if (enabled) base.clickable(onClick = onClick) else base
    Row(
        modifier = withClick.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.padding(end = 12.dp))
        }
        Column(Modifier.weight(1f)) {
            MetroText(
                text = text,
                color = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                style = LocalMetroTypography.current.body,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.padding(start = 12.dp))
            trailing()
        }
    }
}
