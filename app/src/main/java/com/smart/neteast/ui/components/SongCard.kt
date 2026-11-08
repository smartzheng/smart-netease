package com.smart.neteast.ui.components

import com.smart.neteast.ui.theme.LocalNcrustTypography

import com.smart.neteast.ui.theme.LocalNcrustColors
import com.smart.neteast.ui.theme.NcrustShape
import com.smart.neteast.ui.theme.NcrustSpacing



import com.smart.neteast.ui.design.*
import androidx.compose.material.icons.rounded.*

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smart.neteast.network.SongItem
import com.smart.neteast.network.CoverUrls
import com.smart.neteast.ui.design.SaltIcon
import com.moriafly.salt.ui.Text as SaltText
import com.smart.neteast.ui.i18n.LocalStrings
import kotlinx.coroutines.launch

enum class SongCardStyle {
    LIST,
    COMPACT,
    GRID
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SongCard(
    song: SongItem,
    style: SongCardStyle = SongCardStyle.LIST,
    modifier: Modifier = Modifier,
    coverSize: Dp = Dp.Unspecified,
    contentHorizontalPadding: Dp = 16.dp,
    contentVerticalPadding: Dp = 8.dp,
    onClick: () -> Unit = {},
    onShowMenu: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    isCurrentPlaying: Boolean = false,
    showCover: Boolean = true
) {
    val strings = LocalStrings.current
    val artistStr = song.artists?.joinToString("/") { it.name } ?: strings.unknownArtist
    val albumName = song.album?.name ?: ""
    val durationStr = song.duration?.let { formatDuration(it) } ?: ""

    when (style) {
        SongCardStyle.LIST, SongCardStyle.COMPACT -> {
            val actualCoverSize = when {
                coverSize != Dp.Unspecified -> coverSize
                else -> 72.dp
            }

            // 统一横向边距，避免封面和文字贴着屏幕边缘。
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = { onShowMenu?.invoke() }
                    )
                    .padding(horizontal = contentHorizontalPadding, vertical = contentVerticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCover) {
                    AsyncImage(
                        model = CoverUrls.small(song.album?.picUrl),
                        contentDescription = strings.coverDesc,
                        // 纯色占位:避免低端机解码完成前出现"空方块"闪变(保持静态占位,解码完成后直接落图)
                        placeholder = ColorPainter(LocalNcrustColors.current.surfaceVariant),
                        modifier = Modifier.size(actualCoverSize).clip(NcrustShape.thumbnail),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                } else {
                    Spacer(Modifier.width(16.dp))
                }

                Column(Modifier.weight(1f)) {
                    SaltText(
                        song.name,
                        color = if (isCurrentPlaying) LocalNcrustColors.current.primary else LocalNcrustColors.current.onBackground,
                        style = LocalNcrustTypography.current.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SaltText(
                        buildString {
                            append(artistStr)
                            if (albumName.isNotEmpty()) append(" · $albumName")
                            if (durationStr.isNotEmpty()) append("  $durationStr")
                        },
                        color = LocalNcrustColors.current.onSurfaceVariant,
                        style = LocalNcrustTypography.current.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (actions != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        actions.invoke(this)
                    }
                }
            }
        }

        SongCardStyle.GRID -> {
            // 按压缩放动画：Animatable + graphicsLayer，动画帧仅在 draw 阶段消费。
            // 只给 GRID 创建——LIST 是首页高频路径，每项省一个 Animatable + 协程作用域分配。
            val scaleAnim = remember { Animatable(1f) }
            val scaleScope = rememberCoroutineScope()
            Column(
                modifier = modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                scaleScope.launch { scaleAnim.animateTo(1.03f, NcrustMotion.Interaction) }
                                tryAwaitRelease()
                                scaleScope.launch { scaleAnim.animateTo(1f, NcrustMotion.Interaction) }
                            },
                            onTap = { onClick() },
                            onLongPress = { onShowMenu?.invoke() }
                        )
                    }
                    .graphicsLayer {
                        val s = scaleAnim.value
                        scaleX = s
                        scaleY = s
                    }
            ) {
                AsyncImage(
                    model = CoverUrls.small(song.album?.picUrl),
                    contentDescription = strings.coverDesc,
                    placeholder = ColorPainter(LocalNcrustColors.current.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(NcrustShape.artwork),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
                SaltText(
                    song.name,
                    color = LocalNcrustColors.current.onBackground,
                    style = LocalNcrustTypography.current.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SaltText(
                    "$artistStr · $albumName",
                    color = LocalNcrustColors.current.onSurfaceVariant,
                    style = LocalNcrustTypography.current.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PlayAllButton(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "PlayAllButtonPress"
    )
    // Keep a 48dp touch target while the play symbol stays visually light.
    Box(
        modifier = modifier
        .sizeIn(minWidth = NcrustSpacing.touchTarget, minHeight = NcrustSpacing.touchTarget)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { scaleX = scale; scaleY = scale },
            contentAlignment = Alignment.Center
        ) {
            SaltIcon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = LocalStrings.current.playAllButton,
                tint = LocalNcrustColors.current.primary,
                sizeDp = if (size >= 40.dp) 28.dp else 22.dp
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
