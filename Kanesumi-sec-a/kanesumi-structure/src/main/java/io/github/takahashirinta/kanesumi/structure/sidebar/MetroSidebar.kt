package io.github.takahashirinta.kanesumi.structure.sidebar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.anim.sokuou.MetroCubic
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroIcon
import io.github.takahashirinta.kanesumi.core.theme.MetroText
import kotlin.math.abs

@Immutable
data class MetroSidebarItem(
    val icon: ImageVector,
    val label: String,
)

/**
 * Metro 风左侧导航栏（平板 / 折叠展开 / 车机等宽屏的常驻主导航），
 * 对应窄屏的 [io.github.takahashirinta.kanesumi.structure.bottomnav.MetroBottomNav]。
 *
 * 直角、无圆角、无 elevation。选中态是**左侧直角竖条**（indicatorColor），
 * 从旧项滑到新项；图标/文字颜色由同一条 Animatable 派生 alpha 连续 crossfade，
 * 不是"旧淡出新淡入"两段式。动画全部在 graphicsLayer 里读 Animatable，零重组。
 *
 * header / footer 为可选 slot（放品牌标题、用户信息、正在播放等）。系统栏 inset
 * 不代管 —— 调用方按需在 modifier 上套 padding。
 *
 * @param width 整栏宽度，默认 240dp（Apple Music iPad 侧栏量级）。
 * @param rowHeight 单项固定高度，指示条按此值做等距滑动。
 */
@Composable
fun MetroSidebar(
    items: List<MetroSidebarItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 240.dp,
    backgroundColor: Color = LocalMetroColors.current.surface,
    activeColor: Color = LocalMetroColors.current.onSurface,
    inactiveColor: Color = LocalMetroColors.current.onSurfaceVariant,
    indicatorColor: Color = LocalMetroColors.current.primary,
    rowHeight: Dp = 48.dp,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val indicatorProgress = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        indicatorProgress.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = tween(200, easing = MetroCubic),
        )
    }

    val rowHeightPx = with(LocalDensity.current) { rowHeight.toPx() }

    Column(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(backgroundColor),
    ) {
        header?.invoke(this)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    MetroSidebarRow(
                        item = item,
                        height = rowHeight,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        // 每个项的 active alpha = 1 - |indicator - index|，clamp [0,1]。
                        // 指示条停在 index 时该项 alpha=1，滑动途中相邻两项各 0.5。
                        activeAlpha = {
                            (1f - abs(indicatorProgress.value - index.toFloat()))
                                .coerceIn(0f, 1f)
                        },
                        onClick = { onSelected(index) },
                    )
                }
            }

            // 选中指示条：左侧直角竖条，translationY 随 indicator 滑动。
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(3.dp)
                    .height(rowHeight)
                    .graphicsLayer {
                        translationY = indicatorProgress.value * rowHeightPx
                    }
                    .background(indicatorColor),
            )
        }

        footer?.invoke(this)
    }
}

@Composable
private fun MetroSidebarRow(
    item: MetroSidebarItem,
    height: Dp,
    activeColor: Color,
    inactiveColor: Color,
    activeAlpha: () -> Float,
    onClick: () -> Unit,
) {
    val labelStyle = LocalMetroTypography.current.body

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable(onClick = onClick)  // uses LocalIndication → MetroIndication
            .semantics { contentDescription = item.label }
            .padding(start = 18.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
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
        Spacer(Modifier.width(14.dp))
        Box {
            MetroText(
                text = item.label,
                color = inactiveColor,
                style = labelStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MetroText(
                text = item.label,
                color = activeColor,
                style = labelStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer { alpha = activeAlpha() },
            )
        }
    }
}
