package com.smart.neteast.ui.components
import androidx.compose.material.icons.rounded.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PlayArrow
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
import io.github.takahashirinta.kanesumi.core.theme.LocalMetroColors
import com.smart.neteast.ui.design.SaltIcon
import com.moriafly.salt.ui.Text as SaltText
import com.moriafly.salt.ui.SaltTheme

/**
 * 整列表操作对话框：替换队列并播放 / 插到下一首 / 追加到队尾。
 *
 * [onAppendToQueue] 默认 null = 不显示第三项——老调用点（专辑、歌单详情）没有「追加整列表」
 * 的入口，传 null 才能保证它们的行为一字不变。
 */
@Composable
fun PlayAllDialog(
    songCount: Int,
    onDismiss: () -> Unit,
    onReplaceAndPlay: () -> Unit,
    onInsertNext: () -> Unit,
    onAppendToQueue: (() -> Unit)? = null
) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
      Column(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .ncrustDialogSurface()
      ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            SaltText(
                text = strings.songCountFormat(songCount),
                color = LocalMetroColors.current.onSurfaceVariant,
                style = TextStyle(fontSize = 13.sp)
            )
        }
        SaltDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onReplaceAndPlay(); onDismiss() }
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SaltIcon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = LocalMetroColors.current.primary,
                sizeDp = 26.dp
            )
            Spacer(Modifier.width(16.dp))
            Column {
                SaltText(
                    text = strings.playNowTitle,
                    color = LocalMetroColors.current.onBackground,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                SaltText(
                    text = strings.playNowDesc,
                    color = LocalMetroColors.current.onSurfaceVariant,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        }
        SaltDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onInsertNext(); onDismiss() }
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SaltIcon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                contentDescription = null,
                tint = LocalMetroColors.current.onBackground,
                sizeDp = 26.dp
            )
            Spacer(Modifier.width(16.dp))
            Column {
                SaltText(
                    text = strings.insertNextTitle,
                    color = LocalMetroColors.current.onBackground,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                SaltText(
                    text = strings.insertNextDesc,
                    color = LocalMetroColors.current.onSurfaceVariant,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        }
        SaltDivider()

        if (onAppendToQueue != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppendToQueue(); onDismiss() }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaltIcon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = null,
                    tint = LocalMetroColors.current.onBackground,
                    sizeDp = 26.dp
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    SaltText(
                        text = strings.appendQueueTitle,
                        color = LocalMetroColors.current.onBackground,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                    SaltText(
                        text = strings.appendQueueDesc,
                        color = LocalMetroColors.current.onSurfaceVariant,
                        style = TextStyle(fontSize = 12.sp)
                    )
                }
            }
            SaltDivider()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            SaltText(
                text = strings.cancel,
                color = LocalMetroColors.current.onSurfaceVariant,
                style = TextStyle(fontSize = 15.sp)
            )
        }
      }
    }
}

@Composable
private fun SaltDivider() {
    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxWidth().height(1.dp).background(SaltTheme.colors.stroke)
    )
}
