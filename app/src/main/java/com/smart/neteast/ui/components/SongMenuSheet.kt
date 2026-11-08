package com.smart.neteast.ui.components

import com.smart.neteast.ui.theme.LocalNcrustTypography


import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smart.neteast.network.SongItem
import com.smart.neteast.network.CoverUrls
import com.smart.neteast.ui.design.SaltIcon
import com.moriafly.salt.ui.Text as SaltText
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.dialog.BottomSheetDialog

data class SongMenuAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun SongMenuSheet(
    song: SongItem,
    actions: List<SongMenuAction>,
    onDismiss: () -> Unit
) {
    BottomSheetDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SaltTheme.shapes.large)
                .background(SaltTheme.colors.popup)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 封面与迷你播放栏保持同一圆角语言，缩小后给歌曲信息留出空间。
                AsyncImage(
                    model = CoverUrls.small(song.album?.picUrl),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(SaltTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    SaltText(
                        song.name,
                        color = SaltTheme.colors.text,
                        style = LocalNcrustTypography.current.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    SaltText(
                        song.artists?.joinToString("/") { it.name } ?: "",
                        color = SaltTheme.colors.highlight,
                        style = LocalNcrustTypography.current.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val albumName = song.album?.name
                    if (!albumName.isNullOrEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        SaltText(
                            albumName,
                            color = SaltTheme.colors.subText,
                            style = LocalNcrustTypography.current.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(SaltTheme.colors.stroke))

        // 可扩展操作列表
        actions.forEach { action ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        action.onClick()
                        onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SaltIcon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = SaltTheme.colors.text,
                    sizeDp = 24.dp
                )
                Spacer(Modifier.width(16.dp))
                SaltText(
                    action.label,
                    color = SaltTheme.colors.text,
                    style = LocalNcrustTypography.current.bodyLarge
                )
            }
        }

            Spacer(Modifier.height(16.dp))
        }
    }
}
