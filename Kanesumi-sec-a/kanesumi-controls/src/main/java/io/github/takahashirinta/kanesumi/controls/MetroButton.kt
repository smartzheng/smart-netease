package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroIcon
import io.github.takahashirinta.kanesumi.core.theme.MetroText

/**
 * Metro 风矩形按钮。
 *
 * 默认 containerColor = primary,contentColor = onPrimary —— 一个显著、蓝底
 * 白字的 accent 动作按钮。想要次要动作,传 containerColor = surface + contentColor
 * = onSurface;想要 ghost,传 containerColor = Color.Transparent。
 *
 * enabled = false 时容器色 alpha 降到 40%,并断开 clickable —— 点了没反应也
 * 没 indication。
 */
@Composable
fun MetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = LocalMetroColors.current.primary,
    contentColor: Color = LocalMetroColors.current.onPrimary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    leadingIcon: ImageVector? = null,
    iconSize: Dp = 18.dp,
    textStyle: TextStyle = LocalMetroTypography.current.body,
    cornerRadiusDp: Dp = 0.dp,
) {
    val effectiveContainer =
        if (enabled) containerColor else containerColor.copy(alpha = containerColor.alpha * 0.4f)
    val effectiveContent =
        if (enabled) contentColor else contentColor.copy(alpha = contentColor.alpha * 0.4f)

    val shape = RoundedCornerShape(cornerRadiusDp)
    val base = modifier.clip(shape).background(effectiveContainer)
    val withClick = if (enabled) base.clickable(onClick = onClick) else base

    Row(
        modifier = withClick.padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            MetroIcon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = effectiveContent,
                sizeDp = iconSize,
            )
            Spacer(Modifier.width(8.dp))
        }
        MetroText(
            text = text,
            color = effectiveContent,
            style = textStyle,
        )
    }
}
