package io.github.takahashirinta.kanesumi.core.theme

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Theme-aware text primitive. Reads [LocalMetroColors] for default color and
 * [LocalMetroTypography] for default style. Explicit [color] wins if [style] leaves
 * `color = Color.Unspecified` (which is the default in Kanesumi's typography presets).
 *
 * 直接透传 BasicText 的 [maxLines] / [overflow] / [softWrap] / [minLines] --
 * 需要省略号 / 单行截断的调用点直接传参,不必绕回 BasicText。textAlign 走 [style]
 * (TextStyle.textAlign)。
 */
@Composable
fun MetroText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalMetroColors.current.onSurface,
    style: TextStyle = LocalMetroTypography.current.body,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {
    val merged = if (style.color == Color.Unspecified) style.copy(color = color) else style
    BasicText(
        text = text,
        modifier = modifier,
        style = merged,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
    )
}
