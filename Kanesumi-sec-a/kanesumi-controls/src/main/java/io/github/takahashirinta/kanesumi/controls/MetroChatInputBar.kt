package io.github.takahashirinta.kanesumi.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.takahashirinta.kanesumi.core.insets.metroNavigationBarsPadding
import io.github.takahashirinta.kanesumi.core.insets.rememberBottomStackReservation
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import io.github.takahashirinta.kanesumi.core.theme.MetroIcon
import io.github.takahashirinta.kanesumi.core.theme.MetroText

/**
 * Metro 风底部输入栏。直角、无圆角,挂 MetroBottomStack 自适应键盘/底部导航
 * 高度,读 navigationBars 安全区。取代 M3 的 BottomAppBar + TextField 组合。
 *
 * send 按钮:传入 [sendIcon] 时画图标(MetroIconButton),否则用文字 "发送"
 * MetroButton。图标不内置 -- controls 层零 M3,不依赖 material-icons-core,
 * icon 由调用方从自己的图标资产提供(Ncrust / Sakichan 各自有)。
 *
 * [rememberBottomStackReservation] 登记整条高度,内容侧 bottomOverlayPadding()
 * 自动留白。key 用 [reservationKey] 区分,避免多实例互相覆盖。
 */
@Composable
fun MetroChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    sendIcon: ImageVector? = null,
    sendContentDescription: String? = null,
    sendText: String = "发送",
    reservationKey: Any = "kanesumi.chatInputBar",
    measuredHeightDp: Dp = 72.dp,
) {
    val colors = LocalMetroColors.current
    rememberBottomStackReservation(key = reservationKey, heightDp = measuredHeightDp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .metroNavigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MetroTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp),
            placeholder = placeholder,
            enabled = enabled,
            singleLine = false,
            maxLines = 5,
        )
        Spacer(Modifier.width(4.dp))
        val active = enabled && text.isNotBlank()
        if (sendIcon != null) {
            MetroIconButton(onClick = onSend, enabled = active) {
                MetroIcon(
                    imageVector = sendIcon,
                    contentDescription = sendContentDescription,
                    tint = if (active) colors.primary else colors.onSurfaceVariant,
                    sizeDp = 22.dp,
                )
            }
        } else {
            MetroButton(
                text = sendText,
                onClick = onSend,
                enabled = active,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
            )
        }
    }
}
