package io.github.takahashirinta.kanesumi.structure

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Indication
import io.github.takahashirinta.kanesumi.core.insets.metroStatusBarsPadding
import io.github.takahashirinta.kanesumi.core.theme.MetroIcon

/**
 * 顶部黑色渐隐 scrim + 悬浮裸图标。所有覆盖满屏图片的二级页面(专辑详情 /
 * 艺人详情 / WebView 登录 / About)共用此组件,保证"任何底图上白色图标可读"
 * 的一致性 —— scrim 顶部 alpha 0.55 向下渐隐到透明,图标本身无背板。
 *
 * 48dp 触控区符合无障碍最低触控尺寸。indication 默认 null: 跳转页的返回
 * 箭头不做按动反馈(直角闪切在导航动作上显得多余); 需要反馈的调用方
 * 显式传 LocalIndication.current。
 */
@Composable
fun MetroTopScrim(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    scrimHeightDp: Dp = 120.dp,
    scrimAlphaTop: Float = 0.55f,
    iconColor: Color = Color.White,
    iconSizeDp: Dp = 24.dp,
    indication: Indication? = null,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Gradient scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(scrimHeightDp)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = scrimAlphaTop),
                        1f to Color.Transparent,
                    )
                )
        )
        // Floating icon
        Box(
            modifier = Modifier
                .align(alignment)
                .metroStatusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = indication,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            MetroIcon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                sizeDp = iconSizeDp,
            )
        }
    }
}
