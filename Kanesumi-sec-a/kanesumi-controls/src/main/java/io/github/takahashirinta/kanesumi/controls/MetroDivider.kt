package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors

/**
 * 全宽水平分隔线。直角、无装饰 -- 取代 M3 HorizontalDivider 的默认圆角 /
 * tonal elevation。默认 1dp 厚,色 = LocalMetroColors.divider。
 *
 * 想要垂直分隔,目前不提供 -- 没有真实用例驱动,避免预抽象。
 */
@Composable
fun MetroDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = LocalMetroColors.current.divider,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}
