package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Metro 风图标按钮。48dp 触控区(无障碍最低尺寸),无背板、无圆角。
 * 点击走 LocalIndication -> MetroIndication 直角闪切 -- 取代 M3 IconButton
 * 的 ripple。content slot 放 MetroIcon 或任意内容。
 *
 * enabled = false 时 clickable 自动断开,视觉衰减由调用方在 content 里处理
 * (图标 tint 自己降 alpha),库不强加语义。
 */
@Composable
fun MetroIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    touchTargetDp: Dp = 48.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(touchTargetDp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
