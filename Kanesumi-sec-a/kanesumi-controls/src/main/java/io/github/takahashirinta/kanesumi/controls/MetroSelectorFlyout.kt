package io.github.takahashirinta.kanesumi.controls

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.takahashirinta.kanesumi.anim.sokuou.MetroCubic
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroText
import io.github.takahashirinta.kanesumi.core.theme.MetroIcon

/**
 * Metro 风选择器浮层 —— UWP ComboBox 移植版。选中项在打开的瞬间落在锚点
 * 原位置,菜单从选中项周围双向展开,用户不用挪视线找当前值,微小手指移动
 * 就能选相邻项。
 *
 * 与 [MetroDropdownMenu] 的分工:
 * - MetroDropdownMenu = 动作菜单(邻位弹出),用于"更多操作"这类无当前选中态的场景
 * - MetroSelectorFlyout = 选择器(覆盖锚点),用于"从 N 选 1 且有当前选中值"的场景
 *
 * 实现关键:
 * 1. 自定义 [PopupPositionProvider] 把弹层垂直中心对齐锚点中心,水平按
 *    [horizontalAlignment] 贴齐锚点 start/end
 * 2. 内部 LazyColumn 打开时 scrollToItem(selectedIndex) 并设 positive
 *    scrollOffset 把选中项推到 viewport 中间 —— 这样弹层中心 = 选中项 =
 *    锚点中心,视觉连续
 * 3. 弹层高度 heightIn(max = maxHeightDp),长列表(语言 15 项)自动 clamp +
 *    保持滚动;短列表(音质 5 项)按内容高
 * 4. transformOrigin(0.5, 0.5) 让展开动画从中心(即选中项/锚点)向外放大,
 *    强化"从锚点长出"的视觉隐喻
 *
 * 边界处理:锚点靠近屏幕顶/底时,position provider clamp 弹层,选中项与
 * 锚点会略微错开(不再精确对齐),但仍在锚点附近可见 —— UWP 也是这个策略。
 */
@Composable
fun MetroSelectorFlyout(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeightDp: Dp = 44.dp,
    maxWidthDp: Dp = 320.dp,
    maxHeightDp: Dp = 400.dp,
    containerColor: Color = LocalMetroColors.current.surfaceVariant,
    activeColor: Color = LocalMetroColors.current.primary,
    cornerRadiusDp: Dp = 0.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
) {
    if (!expanded) return
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.roundToPx() }
    val maxHeightPx = with(density) { maxHeightDp.roundToPx() }

    val appearProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appearProgress.animateTo(1f, tween(durationMillis = 180, easing = MetroCubic))
    }

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        // scrollOffset > 0 让选中项的顶部落在 viewport 内偏下的位置 -- 这里设为
        // (viewportH - itemH) / 2 = 选中项顶正好在 viewport 垂直中心减半项高,
        // 效果 = 选中项中线落在 viewport 中线。列表边界会自动 clamp,选中项仍可见。
        val centerOffset = (maxHeightPx - itemHeightPx) / 2
        listState.scrollToItem(index = selectedIndex, scrollOffset = -centerOffset)
    }

    val positionProvider = remember(horizontalAlignment) {
        SelectorPositionProvider(horizontalAlignment)
    }
    val shape = RoundedCornerShape(cornerRadiusDp)

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .widthIn(max = maxWidthDp)
                .heightIn(max = maxHeightDp)
                .graphicsLayer {
                    alpha = appearProgress.value
                    scaleY = 0.92f + 0.08f * appearProgress.value
                    // origin 中心 = 选中项 = 锚点位置,从这里向外放大
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
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
            itemsIndexed(options) { index, option ->
                SelectorItem(
                    text = option,
                    isSelected = index == selectedIndex,
                    itemHeightDp = itemHeightDp,
                    activeColor = activeColor,
                    cornerRadiusDp = cornerRadiusDp,
                    onClick = {
                        onSelect(index)
                        onDismissRequest()
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectorItem(
    text: String,
    isSelected: Boolean,
    itemHeightDp: Dp,
    activeColor: Color,
    cornerRadiusDp: Dp,
    onClick: () -> Unit,
) {
    val rounded = cornerRadiusDp > 0.dp
    val shape = RoundedCornerShape(if (rounded) 12.dp else 0.dp)
    val selectedFill = activeColor.copy(alpha = 0.14f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeightDp)
            .then(
                if (rounded) Modifier
                    .padding(horizontal = 6.dp)
                    .clip(shape)
                    .background(if (isSelected) selectedFill else Color.Transparent)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp)
                else Modifier.clickable(onClick = onClick)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!rounded) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(if (isSelected) activeColor else Color.Transparent),
            )
            Spacer(Modifier.width(13.dp))
        }
        MetroText(
            text = text,
            color = if (isSelected) activeColor else LocalMetroColors.current.onSurface,
            style = LocalMetroTypography.current.body,
            modifier = Modifier.weight(1f).then(if (rounded) Modifier else Modifier.padding(end = 16.dp)),
        )
        if (isSelected && cornerRadiusDp > 0.dp) {
            MetroText(
                text = "✓",
                color = activeColor,
                style = LocalMetroTypography.current.body,
            )
        }
    }
}

/**
 * 弹层位置:垂直中心对齐锚点中心;水平按 [horizontalAlignment] 贴齐锚点边缘。
 * 屏幕边界 clamp 保证不出界 —— 极端情况下选中项与锚点会略错开,但可预测。
 *
 * 传给 Popup 后每次 popupContentSize 变化都会重算(打开首帧内容尺寸从 0 长到
 * 最终值,期间 provider 反复调用),graphicsLayer 的 scaleY 动画同时进行,
 * 两者结合视觉是"从锚点位置长出",不是先跳到某处再缩放。
 */
private class SelectorPositionProvider(
    private val horizontalAlignment: Alignment.Horizontal,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val anchorCenterY = anchorBounds.top + anchorBounds.height / 2
        val popupY = anchorCenterY - popupContentSize.height / 2
        val clampedY = popupY.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))

        val popupX = when (horizontalAlignment) {
            Alignment.Start -> anchorBounds.left
            Alignment.End -> anchorBounds.right - popupContentSize.width
            else -> anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        }
        val clampedX = popupX.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))

        return IntOffset(clampedX, clampedY)
    }
}
