package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 内容最大宽度限制。在更宽屏幕(平板 / 折叠展开态 / 分屏)上居中显示,保留
 * 窄屏视觉比例;在窄屏上撑满,完全不影响原有体验。
 *
 * 默认 360dp 上限对应 21:9 竖屏基准 —— 与 Ncrust 长期实践值一致。想要更宽
 * 的移动 UI(如 iPad 竖屏应用惯用的 414dp),覆盖 maxWidthDp 即可。
 */
@Composable
fun MetroResponsiveContent(
    modifier: Modifier = Modifier,
    maxWidthDp: Dp = 360.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidthDp)
                .fillMaxHeight(),
        ) {
            content()
        }
    }
}
