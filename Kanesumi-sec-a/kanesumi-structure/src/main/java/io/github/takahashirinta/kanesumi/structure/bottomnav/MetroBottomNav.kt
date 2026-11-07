package io.github.takahashirinta.kanesumi.structure.bottomnav

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.anim.sokuou.MetroCubic
import io.github.takahashirinta.kanesumi.anim.sokuou.SokuouTweens
import io.github.takahashirinta.kanesumi.core.insets.rememberBottomStackReservation
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroIcon
import io.github.takahashirinta.kanesumi.core.theme.MetroText
import kotlin.math.abs

@Immutable
data class MetroBottomNavItem(
    val icon: ImageVector,
    val label: String,
)

/** 标签策略;[Always] 保持旧版图标 + 文字的默认布局。 */
enum class MetroBottomNavLabelMode {
    Always,
    SelectedOnly,
    Never,
}

/**
 * Kanesumi 风格底部导航。
 *
 * 三个动效全部通过 graphicsLayer 读取 Animatable,零重组:
 * - 顶部指示条从旧位滑到新位 (MetroCubic 200ms),不闪切
 * - 选中/未选中颜色从 indicatorProgress 派生 alpha,与 indicator 同源 --
 *   连续 crossfade,不是"旧淡出新淡入"两段式
 * - 按压反馈由 LocalIndication (MetroTheme 默认注入 MetroIndication) 提供,
 *   直角矩形闪切,无 ripple 无圆角
 *
 * **动画启动时机**:走 LaunchedEffect(selectedIndex),在父组件重组完成后
 * 才启动 animateTo。曾尝试用 `launch(UNDISPATCHED)` 在 tap 回调里同帧启动,
 * 结果反而更糟 -- 低端机上父组件 tab-content 重组会紧接着占用 UI 线程
 * 好几帧,而动画计时不等人:等到 UI 线程重新有空能画一帧时,动画计时
 * 已经走到 40%+,视觉就是从旧位置直接"跳"到 40% 位置再往终点滑。
 * LaunchedEffect 等重组完成再启动,虽然起步晚一拍,但每一帧动画都能
 * 拿到 UI 线程,完整的 slide 可见 -- 这才是低端机的正解。
 *
 * 残留的"顿"来自调用方屏切开销 (Ncrust 里 when(selectedTab) 每次 destroy
 * + mount 整屏),那需要调用方自己压 -- 库层帮不上。
 *
 * 默认颜色从 LocalMetroColors 取。默认自动登记进 MetroBottomStack。
 * 系统导航栏 padding 不代管 —— 调用方外层套 .metroNavigationBarsPadding()。
 *
 * [labelMode] 默认 Always,保留原布局;Never 只布局居中的 22dp 图标,可配 48dp 高度。
 * SelectedOnly 的标签以 QuickSwitch 叠层 alpha 切换,未选中图标回到垂直中心。
 * 隐藏文字不进入语义树,tab 始终以 item.label 命名。左右键按视觉相邻顺序移焦
 * (含 RTL),确认键才触发 onSelected;焦点框由 LocalIndication 提供。
 */
@Composable
fun MetroBottomNav(
    items: List<MetroBottomNavItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = LocalMetroColors.current.onSurface,
    inactiveColor: Color = LocalMetroColors.current.onSurfaceVariant,
    indicatorColor: Color = LocalMetroColors.current.primary,
    indicatorSize: DpSize = DpSize(24.dp, 2.dp),
    heightDp: Dp = 56.dp,
    autoReserveBottomStack: Boolean = true,
    reservationKey: Any = "kanesumi.metroBottomNav",
    labelMode: MetroBottomNavLabelMode = MetroBottomNavLabelMode.Always,
) {
    if (autoReserveBottomStack) {
        rememberBottomStackReservation(reservationKey, heightDp)
    }

    val indicatorProgress = remember { Animatable(selectedIndex.toFloat()) }
    val focusRequesters = remember(items.size) { List(items.size) { FocusRequester() } }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(selectedIndex) {
        indicatorProgress.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = tween(200, easing = MetroCubic),
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp),
    ) {
        val density = LocalDensity.current
        val itemCount = items.size.coerceAtLeast(1)
        val tabWidthPx = with(density) { maxWidth.toPx() } / itemCount
        val indicatorWidthPx = with(density) { indicatorSize.width.toPx() }
        val indicatorLeftInTabPx = (tabWidthPx - indicatorWidthPx) / 2f

        Row(Modifier.fillMaxSize().focusGroup()) {
            items.forEachIndexed { index, item ->
                MetroBottomNavTab(
                    item = item,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    selected = selectedIndex == index,
                    labelMode = labelMode,
                    modifier = Modifier
                        .focusRequester(focusRequesters[index])
                        .focusProperties {
                            left = focusRequesters.getOrNull(index + if (isRtl) 1 else -1) ?: FocusRequester.Default
                            right = focusRequesters.getOrNull(index + if (isRtl) -1 else 1) ?: FocusRequester.Default
                        },
                    // 每个 tab 的 active alpha = 1 - |indicator - index|,clamp [0,1]。
                    // indicator 恰好停在 index 时该 tab alpha=1;滑到中间时相邻两 tab
                    // 各 0.5。图标/文字颜色与 indicator 同步流动,没有"我先动它后动"的错位。
                    activeAlpha = {
                        (1f - abs(indicatorProgress.value - index.toFloat())).coerceIn(0f, 1f)
                    },
                    onClick = { onSelected(index) },
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(indicatorSize)
                .graphicsLayer {
                    translationX = indicatorProgress.value * tabWidthPx + indicatorLeftInTabPx
                }
                .background(indicatorColor),
        )
    }
}

@Composable
private fun RowScope.MetroBottomNavTab(
    item: MetroBottomNavItem,
    activeColor: Color,
    inactiveColor: Color,
    selected: Boolean,
    labelMode: MetroBottomNavLabelMode,
    modifier: Modifier,
    activeAlpha: () -> Float,
    onClick: () -> Unit,
) {
    val labelStyle = LocalMetroTypography.current.label
    val labelAlpha = remember { Animatable(if (selected) 1f else 0f) }
    val labelHeightPx = remember { mutableIntStateOf(0) }
    LaunchedEffect(selected, labelMode) {
        if (labelMode == MetroBottomNavLabelMode.SelectedOnly) {
            labelAlpha.animateTo(if (selected) 1f else 0f, SokuouTweens.QuickSwitch)
        } else {
            labelAlpha.snapTo(if (selected) 1f else 0f)
        }
    }

    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(role = Role.Tab, onClick = onClick)  // LocalIndication → MetroIndication
            .semantics {
                contentDescription = item.label
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.graphicsLayer {
                // 只在 layer 读动画 / 测量值,不逐帧重组或重测。
                translationY = if (labelMode == MetroBottomNavLabelMode.SelectedOnly) {
                    (labelHeightPx.intValue + 2.dp.toPx()) / 2f * (1f - labelAlpha.value)
                } else 0f
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                MetroIcon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = inactiveColor,
                    sizeDp = 22.dp,
                )
                MetroIcon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = activeColor,
                    sizeDp = 22.dp,
                    modifier = Modifier.graphicsLayer { alpha = activeAlpha() },
                )
            }
            if (labelMode != MetroBottomNavLabelMode.Never) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .onSizeChanged { labelHeightPx.intValue = it.height }
                        .clearAndSetSemantics { }
                        .graphicsLayer {
                            alpha = if (labelMode == MetroBottomNavLabelMode.SelectedOnly) labelAlpha.value else 1f
                        },
                ) {
                    MetroText(
                        text = item.label,
                        color = inactiveColor,
                        style = labelStyle,
                    )
                    MetroText(
                        text = item.label,
                        color = activeColor,
                        style = labelStyle,
                        modifier = Modifier.graphicsLayer { alpha = activeAlpha() },
                    )
                }
            }
        }
    }
}
