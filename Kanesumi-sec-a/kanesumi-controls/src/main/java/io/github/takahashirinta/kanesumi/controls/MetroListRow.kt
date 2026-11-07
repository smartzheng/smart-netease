package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroText

/**
 * Metro 风列表行。leading + title/subtitle + trailing 三段式。
 *
 * 默认 contentPadding start = 0dp —— 遵循 Metro "borderless / 贴边" 惯例,让
 * leading 元件(通常是封面或图标)直接接屏幕左边。想要对称边距,自己传
 * PaddingValues(horizontal = 16.dp) 覆盖。
 *
 * onClick != null 时挂 clickable,自动走 LocalIndication (MetroIndication) 反馈。
 */
@Composable
fun MetroListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    titleColor: Color = LocalMetroColors.current.onSurface,
    subtitleColor: Color = LocalMetroColors.current.onSurfaceVariant,
    titleStyle: TextStyle = LocalMetroTypography.current.body,
    subtitleStyle: TextStyle = LocalMetroTypography.current.caption,
    contentPadding: PaddingValues = PaddingValues(start = 0.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
) {
    val base = modifier.fillMaxWidth()
    val withClick = if (onClick != null) base.clickable(onClick = onClick) else base
    Row(
        modifier = withClick.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f, fill = true)) {
            MetroText(text = title, color = titleColor, style = titleStyle)
            if (subtitle != null) {
                MetroText(text = subtitle, color = subtitleColor, style = subtitleStyle)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}
