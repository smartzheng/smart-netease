package com.smart.neteast.ui.components

import com.smart.neteast.ui.theme.LocalNcrustTypography

import com.smart.neteast.ui.theme.LocalNcrustColors
import com.smart.neteast.ui.theme.NcrustShape



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smart.neteast.ui.i18n.LocalStrings
import com.moriafly.salt.ui.Text as SaltText

/**
 * 后台活动授权弹窗（首次启动、且尚未加入电池优化白名单时弹出）。
 * 与应用内其他选择对话框共享圆角表面和明确的前后层级。
 */
@Composable
fun BackgroundActivityDialog(
    onAllow: () -> Unit,
    onLater: () -> Unit
) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onLater) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .ncrustDialogSurface()
                .padding(24.dp)
        ) {
            SaltText(
                strings.batteryTitle,
                color = LocalNcrustColors.current.onBackground,
                style = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(12.dp))
            SaltText(
                strings.batteryMessage,
                color = LocalNcrustColors.current.onSurfaceVariant,
                style = LocalNcrustTypography.current.bodyMedium,
            )
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(NcrustShape.control)
                    .background(LocalNcrustColors.current.primary)
                    .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onAllow)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                SaltText(
                    strings.batteryAllow,
                    color = LocalNcrustColors.current.onPrimary,
                    style = TextStyle(fontSize = 14.sp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(NcrustShape.control)
                    .border(1.dp, LocalNcrustColors.current.onSurfaceVariant.copy(alpha = 0.4f), NcrustShape.control)
                    .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onLater)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                SaltText(
                    strings.batteryLater,
                    color = LocalNcrustColors.current.onSurfaceVariant,
                    style = TextStyle(fontSize = 14.sp),
                )
            }
        }
    }
}
