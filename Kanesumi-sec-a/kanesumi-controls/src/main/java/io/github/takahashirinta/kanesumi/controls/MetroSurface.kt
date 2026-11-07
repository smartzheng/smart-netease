package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors

/**
 * 直角矩形面片,Metro 的"承色"基本单元 —— 取代 M3 Surface 的圆角 / 阴影 /
 * elevation tonal shift。可点击时通过 LocalIndication 自动挂 MetroIndication,
 * 得到直角闪切反馈。
 *
 * 默认色 = LocalMetroColors.surface。想要 surfaceVariant / primary 底,直接
 * 传对应 color。
 */
@Composable
fun MetroSurface(
    modifier: Modifier = Modifier,
    color: Color = LocalMetroColors.current.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = modifier.background(color)
    val withClick = if (onClick != null) base.clickable(onClick = onClick) else base
    Box(withClick) { content() }
}
