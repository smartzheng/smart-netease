package io.github.takahashirinta.kanesumi.structure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import io.github.takahashirinta.kanesumi.core.insets.metroStatusBarsPadding
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroTypography
import io.github.takahashirinta.kanesumi.core.theme.MetroText

/**
 * Metro 风顶栏。34sp Regular 页面标题(pageHeading typography),取代 M3
 * TopAppBar 的 Surface + elevation + 缩合动画等重型语义。
 *
 * 默认 color = background —— 与页面底色同色,看不见栏但吃掉 status bar 的
 * padding。想要"显式栏"传 surface / primary 等。navigationIcon 和 actions
 * 都是 slot,不填就没有,不占位。applyStatusBarsPadding = true 时内层自动
 * .metroStatusBarsPadding() —— 内容侧调用方就不用手写。
 */
@Composable
fun MetroAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    titleStyle: TextStyle = LocalMetroTypography.current.pageHeading,
    titleColor: Color = LocalMetroColors.current.onBackground,
    color: Color = LocalMetroColors.current.background,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    applyStatusBarsPadding: Boolean = true,
) {
    val base = modifier.fillMaxWidth().background(color)
    val withInset = if (applyStatusBarsPadding) base.metroStatusBarsPadding() else base
    Row(
        modifier = withInset.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (navigationIcon != null) {
            navigationIcon()
            Spacer(Modifier.width(12.dp))
        }
        Box(Modifier.weight(1f)) {
            MetroText(text = title, color = titleColor, style = titleStyle)
        }
        actions()
    }
}
